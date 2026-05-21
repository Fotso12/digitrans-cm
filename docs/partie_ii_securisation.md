# Partie II : Rapport de sécurisation DIGITRANS-CM

## Introduction
Cette Partie II de l'épreuve couvre : identification des risques, responsabilités selon le modèle partagé, politique IAM, procédure de départ d'un développeur, politique de rotation des clés, plan de réponse aux incidents, chiffrement des données, bonnes pratiques, et la conception technique de la solution blockchain (choix, structure de bloc, consensus, smart contract, interactions avec le SI).

## II.2 Stratégie de sécurité

### 1) Quatre risques de sécurité clés
- Fuite de données sensibles (données RH, financières, clients) exfiltration de la base de données ou sauvegardes non chiffrées.
- Compromission d'identifiants et de clés (clé privée d'un nœud blockchain, credentials DB, secrets CI/CD).
- Vulnérabilités applicatives (injection SQL, vulnérabilités dans les API REST, erreurs dans les smart contracts).
- Disponibilité et résilience (coupures d'électricité, partitions réseau, attaques DDoS ciblant les endpoints publics).

### 2) Modèle de responsabilité partagée (exemples)
Pour chaque risque, responsabilité du fournisseur (Cloud) vs responsabilité de l'entreprise :

| Risque | Responsabilité du fournisseur (Cloud) | Responsabilité de l'entreprise |
|--------|---------------------------------------|--------------------------------|
| **Fuite de données au repos** | Isolation physique, chiffrement matériel, durabilité des volumes, mises à jour physiques. | Chiffrement applicatif des champs sensibles, gestion des clefs (KMS), configuration correcte des ACL et sauvegardes chiffrées. |
| **Compromission d'identifiants / clés** | Sécurité des hyperviseurs, contrôle des accès physique, services KMS managés. | Génération/rotation régulière des clés, stockage sécurisé (Secrets Manager/Key Vault), MFA, révocation rapide. |
| **Attaques applicatives** | Patching des infrastructures managées, protections réseau de base. | Sécurisation du code (SAST/DAST), WAF, revue de sécurité, tests d'intrusion. |
| **Disponibilité / DDoS** | Capacité réseau, mécanismes DDoS (ex: AWS Shield), redondance infra. | Architecture multi-zone/région si besoin, autoscaling, stratégie offline-first pour les agents terrain. |

### 3) Politique IAM (rôles et droits)
- **ADMIN** : droits complets (utilisateurs, déploiements, IAM, secrets). Accès limités et traçables. Utiliser MFA obligatoire et sessions courtes.
- **DEVOPS** : accès aux pipelines CI/CD, déploiements, gestion des secrets d'infra mais pas accès aux données RH/financières en clair.
- **MANAGER** : accès lecture/rapports, gestion paramètres métier (pas d'accès aux secrets infra).
- **AGENT_TERRAIN** : création/mise à jour des marchandises, accès uniquement aux fonctions nécessaires ; pas de lecture des données sensibles.
- **AUDIT** : accès en lecture aux logs et à l'historique de la blockchain.

Implémentation : RBAC via Azure AD / AWS IAM + mapping des rôles applicatifs (JWT claim `role` utilisé par `SecurityConfig` dans l'application). Principe du moindre privilège appliqué strictement.

### 4) Procédure de gestion des droits d'accès en cas de départ d'un développeur
1. Notification RH → déclenchement du processus de révocation.
2. Révoquer l'accès à l'IdP / Azure AD immédiatement (désactiver le compte).
3. Révoquer toutes les sessions actives (invalider tokens si possible).
4. Révoquer/roter tous les secrets et clés possédés (secrets CI, accès DB, clés de déploiement). Priorité: clés de production.
5. Lancer une revue des actions récentes (logs) et conserver preuves pour audit.
6. Réassigner accès et tâches au personnel restant.
7. Documenter l'opération et faire un post mortem si anomalies.

### 5) Politique de rotation des clés (base de données)
- **Fréquence** : 
  - Accès applications (user/password) : rotation automatique tous les 30 jours.
  - Clés de chiffrement (master keys KMS) : rotation tous les 90 jours (ou selon politique entreprise/reglementaire).
- **Étapes** :
  a. Générer nouvelle clé dans `AWS KMS` / `Azure Key Vault` (ou dispositif on prem si requis).
  b. Déployer clé en mode "cohabitation" (alias pointant vers nouvelle clé) dans environnements non-prod.
  c. Mettre à jour secrets dans `Secrets Manager` pour dev/test puis valider.
  d. Planifier bascule en production hors heures de pointe ; effectuer tests de connexion.
  e. Retirer ancienne clé après période de cohabitation (7 jours) et archivage.
- **Automatisation** : CI/CD pipeline pour rollback en cas d'échec.

### 6) Plan de réponse aux incidents 
- **Détection** : centraliser logs (CloudWatch/Azure Monitor/ELK), SIEM, alerting (Slack/Email/SMS).
- **Contenance**: isoler ressources compromises, révoquer accès, bloquer IPs si nécessaire.
- **Eradication** : patch, replace compromised credentials, rebuild images (containers/VM) depuis immutables images signées.
- **Recovery** : restauration via backups testés, vérification intégrité, remise en prod progressive.
- **Communication** : notifier DSI, client AGROCAM, autorités compétentes si données personnelles impactées (conformément à la loi n°2010/012). 
- **Post incident** : rapport, lessons learned, mise à jour des procédures.

### 7) Chiffrement
- **En transit** : L'objectif ici est d'empêcher les attaques de type Man-in-the-Middle (MITM) et d'assurer l'intégrité des échanges.
  - Standard TLS & HSTS : On ne se contente pas du HTTPS. On configure le serveur (Nginx, Apache ou Load Balancer) pour refuser tout protocole inférieur à TLS 1.2. Le HSTS (HTTP Strict Transport Security) est crucial : c'est une directive qui ordonne au navigateur de ne communiquer avec le serveur qu'en HTTPS, même si l'utilisateur tape http://.
  - mTLS (Mutual TLS) : Dans une architecture microservices ou avec un nœud blockchain, le TLS classique authentifie le serveur. Le mTLS force le client (le service A) à présenter aussi un certificat au serveur (le service B). C'est une double vérification d'identité indispensable pour les flux critiques.
- **Au repos** : Ici, on protège les données contre le vol physique de disques ou l'accès non autorisé aux bases de données. Stratégie à deux niveaux :
  - Niveau Infrastructure (Transparent) : On active le chiffrement des volumes (AES-256) au niveau du fournisseur Cloud (AWS EBS, Azure Disk). C'est transparent pour l'application, mais cela protège les snapshots et les disques bruts.
  - Niveau Applicatif (Field Level Encryption) : Pour les données ultra-sensibles (ex: un Hash d'identité ou un montant), on chiffre la donnée avant de l'envoyer en base. Même un administrateur ayant accès au SQL ne verra que du texte chiffré.

### 8) Guide de bonnes pratiques (liste courte et prioritaire)
**Gestion des Identités et Accès (IAM)**
- MFA Systématique : Activer l'authentification multi-facteur (MFA) sur tous les comptes, avec une priorité critique sur les accès root, administrateurs et accès distants (VPN/SSH).
- Moindre Privilège (Zero Trust) : Par défaut, aucun accès. Accorder uniquement les permissions strictement nécessaires à une tâche via des rôles IAM temporaires plutôt que des clés permanentes.
- Isolation des Environnements : Séparation stricte (comptes cloud distincts) entre Dev, Test et Prod. Aucune donnée réelle de production ne doit transiter en environnement de développement.
- Gouvernance : Revue des droits d'accès tous les 90 jours (minimum) et suppression immédiate des comptes inactifs ou lors d'un départ.

**Protection du Code et des Données**
- Zéro Secret dans le Code : Interdiction stricte de stocker des mots de passe ou clés API dans Git. Utilisation impérative d'un coffre-fort (AWS Secrets Manager, HashiCorp Vault) avec injection dynamique des secrets à l'exécution.
- Scan des Dépendances (SCA) : Automatiser la détection de vulnérabilités dans les bibliothèques tierces (ex: Dependabot, Snyk) pour parer aux attaques sur la supply chain.

**Sécurité du Cycle de Vie (DevSecOps)**
- Analyse Continue (Pipeline CI) :
  - SAST : Analyse statique du code (ex: SonarQube) à chaque commit.
  - DAST : Analyse dynamique sur l'application déployée (ex: OWASP ZAP) pour détecter les failles d'exécution (Injections, XSS).
- Immuabilité de l'Infrastructure : Privilégier l'Infrastructure as Code (Terraform, Ansible) pour garantir que la configuration de sécurité est reproductible et versionnée.

**Résilience et Réponse aux Incidents**
- Plan de Continuité (DRP) : Tester la restauration des sauvegardes (Backups) au moins une fois par trimestre. Une sauvegarde non testée est une sauvegarde inexistante.
- Simulation de Crise : Organiser des exercices "Table-top" (scénarios fictifs) pour valider la chaîne de décision et de communication en cas de compromission de données.

## II.3 Sécurité des transactions et des données (Blockchain)

### 1. Choix de la plateforme
- **Recommandation principale (entreprise)** : Hyperledger Fabric (permissioned) justifications :
  - Permet déploiement on premise (respect souveraineté) ou chez un datacenter local.
  - Gestion fine des identités (MSP), canaux privés pour compartimenter données.
  - Consensus léger (Raft) adapté à entreprises, faible consommation de ressources.
- **Alternative pour PoC rapide (étudiants / démonstration)** : Ethereum (private testnet / Ganache) via Remix Desktop. Avantage : rapidité de prototypage, large écosystème d'outils (Remix, Truffle, Hardhat). Inconvénient : pour production en entreprise, les blockchains permissioned sont préférables.

### 2. Structure d'un bloc (implémentation proposée)
La structure de mon implémentation repose sur une séparation stricte entre l'identification du bloc, son contenu transactionnel et les preuves de consensus.

**A. Anatomie du Bloc**
Chaque bloc est divisé en trois segments logiques :
1. Le Header (En-tête) : C'est la "carte d'identité" du bloc.
   - index : Position du bloc dans la chaîne.
   - timestamp : Date et heure de création.
   - prevHash : Empreinte cryptographique du bloc précédent.
   - merkleRoot : Empreinte unique résumant l'intégralité des transactions du bloc.
   - proposerId : Identifiant du nœud ayant forgé le bloc.
2. Le Corps (Transactions) : Le registre des faits.
   - Chaque transaction enregistre : txId, timestamp, et un payload métier (ex: changement de statut de la marchandise, localisation, identité de l'agent).
   - Sécurité : Chaque transaction est accompagnée de la signature numérique de l'émetteur et de son propre hash.
3. Les Metadata (Preuves) :
   - Contient les endorsements (approbations) et les signatures collectées durant la phase de consensus, prouvant que le bloc a été validé par les pairs.

**B. Garantie de l'intégrité entre deux blocs**
L'intégrité n'est pas seulement un état, c'est un mécanisme de dépendance en cascade garanti par deux piliers :
Le Chainage par Hash (prevHash)
C'est le principe fondamental de la blockchain. Le Header du bloc N contient le hash complet du Header du bloc N-1.
Conséquence : Si une seule donnée du bloc N-1 est modifiée (même un espace), son hash change totalement. Cela brise immédiatement le lien avec le bloc N, rendant toute la chaîne suivante invalide aux yeux des nœuds du réseau.

**C. L'Immuabilité Interne (Arbre de Merkle)**
Le merkleRoot présent dans le header lie l'identité du bloc à son contenu.
- Toutes les transactions sont hachées par paires jusqu'à obtenir une seule racine.
- Si un fraudeur tente de modifier une transaction à l'intérieur d'un bloc déjà validé, la racine de Merkle recalculée ne correspondra plus à celle inscrite dans le header.
- Comme le header a déjà été haché pour le bloc suivant, toute modification interne corrompt la continuité de la chaîne.

### 3. Mécanisme de consensus
Le choix du consensus pour AGROCAM S.A. est dicté par le besoin de débit élevé et de finalité immédiate.
Le choix retenu est : Raft (Consensus basé sur le Leader)
Dans le cadre de ce réseau d'entreprise, j'ai sélectionné l'algorithme Raft. Contrairement au Proof of Work (PoW) des réseaux publics, Raft repose sur l'élection d'un leader qui ordonne les transactions et les réplique sur les autres nœuds.

Pourquoi ce choix est-il adapté à AGROCAM S.A. ?
1. Performance et Faible Latence : Dans un réseau privé, le temps de validation doit être quasi instantané. Raft offre une latence extrêmement faible car il ne nécessite pas de calculs cryptographiques coûteux (minage).
2. Consommation de Ressources : Le "overhead" (surpoids) réseau et CPU est minimal, permettant de faire tourner les nœuds sur des infrastructures standards sans coût énergétique majeur.
3. Finalité Immédiate : Une fois qu'une transaction est confirmée par le quorum, elle est définitive. Il n'y a pas de risque de "fork" ou d'annulation, ce qui est crucial pour la traçabilité logistique.
4. Tolérance aux pannes (Crash Fault Tolerance) : Le système continue de fonctionner tant qu'une majorité de nœuds est opérationnelle, ce qui est idéal pour la haute disponibilité interne.

### 4. Traçabilité et conformité (loi n°2010/012)
- La blockchain enregistre : horodatage, auteur (identité signée), opération réalisée. Ces preuves immuables permettent d'attester des accès et des écritures. Les logs d'accès peuvent être indexés et cross référencés aux transactions blockchain pour audits.

### 5. Smart contract proposé
Voir les fichiers dans `poC-blockchain/Traceability.sol`.

Commentaires sur ce contrat :
- Il stocke uniquement des métadonnées (éviter d'enregistrer des informations personnelles sensibles en clair sur la blockchain).
- Les agents sont identifiés par leur adresse Ethereum ; pour la conformité, il faut mapper ces adresses à des identités réelles dans l'Off chain (ex : DB d'utilisateurs) et conserver cette corrélation en logs chiffrés.

### 6. Flux technique (exécution d'un mouvement)
1. L'agent terrain appelle l'API `PUT /api/supply-chain/marchandises/{id}/statut` avec JWT.
2. Le backend (votre `MarchandiseService`) vérifie autorisations et met à jour la DB locale.
3. `BlockchainService.enregistrerMouvement(...)` est appelé : il envoie la transaction au nœud blockchain (ou génère un hash fallback si nœud indisponible).
4. Le backend récupère le `txHash` et le sauvegarde dans la table `Marchandise` et dans `MouvementStock`.
5. Un composant off chain (listener) peut indexer les events blockchain pour le reporting et BI.

### 7. Bonnes pratiques de sécurité smart contracts
- Limiter logique on chain ; privilégier preuve d'événement et stockage off chain des données lourdes.
- Tests unitaires, fuzzing, audit SAST (Slither, MythX), et revue manuelle.
- Prévenir reentrancy (pattern checks effects interactions, ReentrancyGuard), prévenir overflow (Solidity >=0.8 int checks).

### 8. Procédure si clé privée d'un nœud est compromise (procédure opérable)
1. Isolation immédiate : retirer le nœud compromis du réseau (firewall + arrêt service).
2. Révocation : marquer la clé/certificat comme révoqué dans le système d'identité (MSP / CA). Mettre à jour la configuration du consortium pour ne plus accepter le nœud.
3. Rotation : générer nouvelle paire de clefs pour le nœud de remplacement, provisionner via canal sécurisé.
4. Rebuild : déployer un nouveau nœud à partir d'images signées, synchroniser l'état blockchain.
5. Vérification intégrité : analyser les transactions produites pendant la compromission ; si fraude évidente → inscrire des transactions de correction ou revocation auditable.
6. Communication : notifier parties prenantes internes, client AGROCAM, et autorités si nécessaire.
7. Post mortem : rapport d'incident, mesures correctives, tests d'intrusion pour valider correctifs.

### 9. Passage privé → Consortium (adaptations)
- Mettre en place gouvernance (accords, SLA) et MSP multi organisation pour gérer identités.
- Utiliser channels privés pour données sensibles (reste sur nœuds camerounais), partager uniquement les données nécessaires sur channels transfrontaliers.
- Mettre en place processus de onboarding/outboarding des partenaires (certificats, KYC technique).
