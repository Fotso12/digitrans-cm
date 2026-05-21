# Partie II — Rapport de sécurisation — DIGITRANS-CM

Date : Mai 2026
Auteur : Équipe DIGITRANS-CM

## Introduction
Ce document est le rapport de sécurisation demandé pour la Partie II de l'épreuve. Il couvre : identification des risques, responsabilités selon le modèle partagé, politique IAM, procédure de départ d'un développeur, politique de rotation des clés, plan de réponse aux incidents, chiffrement des données, bonnes pratiques, et la conception technique de la solution blockchain (choix, structure de bloc, consensus, smart contract, interactions avec le SI).

## II.2 Stratégie de sécurité

### 1) Quatre risques de sécurité clés
- Fuite de données sensibles (données RH, financières, clients) — exfiltration de la base de données ou sauvegardes non chiffrées.
- Compromission d'identifiants et de clés (clé privée d'un nœud blockchain, credentials DB, secrets CI/CD).
- Vulnérabilités applicatives (injection SQL, CSRF, vulnérabilités dans les API REST, erreurs dans les smart contracts comme reentrancy).
- Disponibilité et résilience (coupures d'électricité, partitions réseau, attaques DDoS ciblant les endpoints publics).

### 2) Modèle de responsabilité partagée (exemples)
Pour chaque risque, responsabilité du fournisseur (Cloud) vs responsabilité de l'entreprise :

- Fuite de données au repos
  - Fournisseur : isolation physique, chiffrement matériel, durabilité des volumes, mises à jour physiques.
  - Entreprise : chiffrement applicatif des champs sensibles, gestion des clefs (KMS), configuration correcte des ACL et sauvegardes chiffrées.

- Compromission d'identifiants / clés
  - Fournisseur : sécurité des hyperviseurs, contrôle des accès physique, services KMS managés.
  - Entreprise : génération/rotation régulière des clés, stockage sécurisé (Secrets Manager/Key Vault), MFA, révocation rapide.

- Attaques applicatives
  - Fournisseur : patching des infrastructures managées, protections réseau de base.
  - Entreprise : sécurisation du code (SAST/DAST), WAF, revue de sécurité, tests d'intrusion.

- Disponibilité / DDoS
  - Fournisseur : capacité réseau, mécanismes DDoS (ex: AWS Shield), redondance infra.
  - Entreprise : architecture multi-zone/région si besoin, autoscaling, stratégie offline-first pour les agents terrain.

### 3) Politique IAM (rôles et droits)
- **ADMIN** : droits complets (utilisateurs, déploiements, IAM, secrets). Accès limités et traceables. Utiliser MFA obligatoire et sessions courtes.
- **DEVOPS** : accès aux pipelines CI/CD, déploiements, gestion des secrets d'infra mais pas accès aux données RH/financières en clair.
- **MANAGER** : accès lecture/rapports, gestion paramètres métier (pas d'accès aux secrets infra).
- **AGENT_TERRAIN** : création/mise à jour des marchandises, accès uniquement aux fonctions nécessaires ; pas de lecture des données sensibles.
- **AUDIT** : accès en lecture aux logs et à l'historique de la blockchain.

Implémentation : RBAC via Azure AD / AWS IAM + mapping des rôles applicatifs (JWT claim `role` utilisé par `SecurityConfig` dans l'application). Principe du moindre privilège appliqué strictement.

### 4) Procédure de gestion des droits d'accès en cas de départ d'un développeur
1. Notification RH → déclenchement du processus de révocation.
2. Révoquer l'accès à l'IdP / Azure AD immédiatement (désactiver le compte).
3. Révoquer toutes les sessions actives (invalider tokens si possible).
4. Révoquer/rotater tous les secrets et clés possédés (secrets CI, accès DB, clés de déploiement). Priorité: clés de production.
5. Lancer une revue des actions récentes (logs) et conserver preuves pour audit.
6. Réassigner accès et tâches au personnel restant.
7. Documenter l'opération et faire un post‑mortem si anomalies.

### 5) Politique de rotation des clés (base de données)
- **Fréquence** : 
  - Accès applications (user/password) : rotation automatique tous les 30 jours.
  - Clés de chiffrement (master keys KMS) : rotation tous les 90 jours (ou selon politique entreprise/reglementaire).
- **Étapes** :
  1. Générer nouvelle clé dans `AWS KMS` / `Azure Key Vault` (ou dispositif on‑prem si requis).
  2. Déployer clé en mode "cohabitation" (alias pointant vers nouvelle clé) dans environnements non-prod.
  3. Mettre à jour secrets dans `Secrets Manager` pour dev/test puis valider.
  4. Planifier bascule en production hors heures de pointe ; effectuer tests de connexion.
  5. Retirer ancienne clé après période de cohabitation (7 jours) et archivage.
- **Automatisation** : CI/CD pipeline + runbook pour rollback en cas d'échec.

### 6) Plan de réponse aux incidents (synthèse)
- **Détection** : centraliser logs (CloudWatch/Azure Monitor/ELK), SIEM, alerting (Slack/Email/SMS).
- **Containment** : isoler ressources compromises, révoquer accès, bloquer IPs si nécessaire.
- **Eradication** : patch, replace compromised credentials, rebuild images (containers/VM) depuis immutables images signées.
- **Recovery** : restauration via backups testés, vérification intégrité, remise en prod progressive.
- **Communication** : notifier DSI, client AGROCAM, autorités compétentes si données personnelles impactées (conformément à la loi n°2010/012). 
- **Post‑incident** : rapport, lessons learned, mise à jour des procédures.

### 7) Chiffrement
- **En transit** : TLS 1.2+ (préférer 1.3 si disponible). APIs exposées via HTTPS. mTLS entre services critiques (backend ↔ nœud blockchain privé). Forcer HSTS sur endpoints publics.
- **Au repos** : chiffrement natif des volumes cloud + chiffrement applicatif des champs sensibles (numéro identifiant, données clients). Gérer clefs via service KMS/Key Vault. Ne pas stocker les clefs dans le code ou le repo.

### 8) Guide de bonnes pratiques (liste courte et prioritaire)
- Centraliser les secrets (AWS Secrets Manager / Azure Key Vault).
- Activer MFA pour tous les comptes administrateurs.
- Principe du moindre privilège pour IAM et séparation des environnements (dev/test/prod).
- Revue d'accès périodique (30–90 jours).
- Intégrer SAST et DAST dans la CI (ex : SonarQube, OWASP ZAP).
- Scanner dépendances (Dependabot, Snyk).
- Exercices réguliers de réponse aux incidents (table‑top), tests de restauration.

---

## II.3 Sécurité des transactions et des données (Blockchain)

### Choix de la plateforme
- **Recommandation principale (entreprise)** : **Hyperledger Fabric** (permissioned) — justifications :
  - Permet déploiement on‑premise (respect souveraineté) ou chez un datacenter local.
  - Gestion fine des identités (MSP), canaux privés pour compartimenter données.
  - Consensus léger (Raft) adapté à entreprises, faible consommation de ressources.

- **Alternative pour PoC rapide (étudiants / démonstration)** : **Ethereum (private testnet / Ganache)** via Remix Desktop. Avantage : rapidité de prototypage, large écosystème d'outils (Remix, Truffle, Hardhat). Inconvénient : pour production en entreprise, les blockchains permissioned sont préférables.

### Structure d'un bloc (implémentation proposée)
- **Header**: index, timestamp, prevHash, merkleRoot(transactions), proposerId.
- **Transactions**: chaque transaction contient : txId, timestamp, payload {codeMarchandise, ancienStatut, nouveauStatut, localisation, agent, remarques}, signature du submitter, hash.
- **Metadata**: endorsements, signatures du consensus.

Intégrité garantie par chaining (`prevHash`) et merkleRoot des transactions ; signatures d'endorsement attestent de la validité.

### Mécanisme de consensus
- **Raft** (leader-based) recommandé : faible overhead, latence faible, tolérance aux crashs, approprié pour nœuds sous le contrôle d'AGROCAM.
- **PBFT** à considérer si tolérance byzantine stricte requise (coût et complexité plus élevés).

### Traçabilité et conformité (loi n°2010/012)
- La blockchain enregistre : horodatage, auteur (identité signée), opération réalisée. Ces preuves immuables permettent d'attester des accès et des écritures. Les logs d'accès peuvent être indexés et cross‑référencés aux transactions blockchain pour audits.

### Smart contract proposé
Ci‑dessous un smart contract Solidity simple et commenté pour PoC. Pour production, convertir en chaincode (Go/Node) si Fabric est retenu.

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.17;

/**
 * @title Traceability
 * @dev Contrat simplifié pour la traçabilité des mouvements de marchandises
 */
contract Traceability {
    struct Movement {
        string code;           // code de traçabilité du lot
        string ancienStatut;
        string nouveauStatut;
        string localisation;
        address agent;         // adresse Ethereum de l'agent
        uint256 timestamp;
        string remarques;
    }

    // mapping du code de traçabilité vers l'historique des mouvements
    mapping(string => Movement[]) private history;

    // Events pour permettre aux services off-chain de réagir
    event MovementRecorded(string indexed code, uint256 index, address indexed agent, uint256 timestamp);

    /**
     * @dev Enregistrer un mouvement pour un code donné
     */
    function recordMovement(
        string memory code,
        string memory ancienStatut,
        string memory nouveauStatut,
        string memory localisation,
        string memory remarques
    ) public {
        Movement memory m = Movement({
            code: code,
            ancienStatut: ancienStatut,
            nouveauStatut: nouveauStatut,
            localisation: localisation,
            agent: msg.sender,
            timestamp: block.timestamp,
            remarques: remarques
        });

        history[code].push(m);
        emit MovementRecorded(code, history[code].length - 1, msg.sender, block.timestamp);
    }

    /**
     * @dev Récupérer l'historique pour un code
     */
    function getHistory(string memory code) public view returns (Movement[] memory) {
        return history[code];
    }
}
```

Commentaires sur ce contrat :
- Il stocke uniquement des métadonnées (éviter d'enregistrer des informations personnelles sensibles en clair sur la blockchain).
- Les agents sont identifiés par leur adresse Ethereum ; pour la conformité, il faut mapper ces adresses à des identités réelles dans l'Off‑chain (ex : DB d'utilisateurs) et conserver cette corrélation en logs chiffrés.

### Flux technique (exécution d'un mouvement)
1. L'agent terrain appelle l'API `PUT /api/supply-chain/marchandises/{id}/statut` avec JWT.
2. Le backend (votre `MarchandiseService`) vérifie autorisations et met à jour la DB locale.
3. `BlockchainService.enregistrerMouvement(...)` est appelé : il envoie la transaction au nœud blockchain (ou génère un hash fallback si nœud indisponible).
4. Le backend récupère le `txHash` et le sauvegarde dans la table `Marchandise` et dans `MouvementStock`.
5. Un composant off‑chain (listener) peut indexer les events blockchain pour le reporting et BI.

### Bonnes pratiques de sécurité smart contracts
- Limiter logique on‑chain ; privilégier preuve d'événement et stockage off‑chain des données lourdes.
- Tests unitaires, fuzzing, audit SAST (Slither, MythX), et revue manuelle.
- Prévenir reentrancy (pattern checks‑effects‑interactions, ReentrancyGuard), prévenir overflow (Solidity >=0.8 int checks).

### Procédure si clé privée d'un nœud est compromise (procédure opérable)
1. **Isolation immédiate** : retirer le nœud compromis du réseau (firewall + arrêt service).
2. **Révocation** : marquer la clé/certificat comme révoqué dans le système d'identité (MSP / CA). Mettre à jour la configuration du consortium pour ne plus accepter le nœud.
3. **Rotation** : générer nouvelle paire de clefs pour le nœud de remplacement, provisionner via canal sécurisé.
4. **Rebuild** : déployer un nouveau nœud à partir d'images signées, synchroniser l'état blockchain.
5. **Vérification intégrité** : analyser les transactions produites pendant la compromission ; si fraude évidente → inscrire des transactions de correction ou revocation auditable.
6. **Communication** : notifier parties prenantes internes, client AGROCAM, et autorités si nécessaire.
7. **Post‑mortem** : rapport d'incident, mesures correctives, tests d'intrusion pour valider correctifs.

### Passage privé → Consortium (adaptations)
- Mettre en place gouvernance (accords, SLA) et MSP multi‑organisation pour gérer identités.
- Utiliser channels privés pour données sensibles (reste sur nœuds camerounais), partager uniquement les données nécessaires sur channels transfrontaliers.
- Mettre en place processus de onboarding/outboarding des partenaires (certificats, KYC technique).

---

## Annexes — Démonstration PoC Ethereum (Remix)

Si vous préférez déployer un PoC rapide avec Remix Desktop : voici un guide court et les prérequis (Plan B).

### Smart contract fourni
Le contrat `Traceability` (ci‑dessus) est prêt pour être collé dans Remix et compilé (Solidity ^0.8.17).

### Étapes rapides pour déploiement via Remix Desktop
1. Ouvrir Remix Desktop.
2. Créer un nouveau fichier `Traceability.sol` et coller le code.
3. Compiler avec le compilateur `0.8.17`.
4. Déployer sur :
   - `JavaScript VM` (rapide, local, pas de persistance), ou
   - `Injected Web3` (Metamask connecté à Ganache / local node) pour test avec txHash réels.
5. Appeler `recordMovement(...)` depuis Remix pour simuler un mouvement ; récupérer event `MovementRecorded` ou txHash.
6. Relier ce PoC au backend : le backend peut envoyer une transaction via Web3j vers un nœud local (Ganache) ou vers un provider (Infura) selon configuration `blockchain.node.url`.

---

## Prérequis et checklist pour le Plan B (PoC avec Remix / Ganache)
- **Logiciels** :
  - `Remix Desktop` (installé), ou Remix Web + navigateur.
  - `Ganache` (local Ethereum RPC) ou un provider RPC (Infura/Alchemy) pour réseau test.
  - `Node.js` + `npm` si vous voulez utiliser `truffle` ou `hardhat` pour scripts.
  - `Metamask` (si Injected Web3) ou utiliser `Remix -> JavaScript VM`.
- **Versions** : Solidity compiler `>=0.8.0` (le code fourni utilise ^0.8.17).
- **Clés / accounts** : comptes Ganache (pré‑fundés) pour simuler envoi de tx.
- **Backend** : dépendance `web3j` (déjà utilisée dans `BlockchainService`) — config `blockchain.node.url` pointant vers Ganache (`http://localhost:8545`).
- **Sécurité** : ne pas exposer Ganache sur internet ; utiliser uniquement en local pour PoC.

## Commandes utiles (Plan B)
```bash
# Lancer Ganache (UI) ou CLI
ganache --deterministic --port 8545

# Si vous utilisez Hardhat pour déployer (optionnel)
npm init -y
npm install --save-dev hardhat
npx hardhat
# suivre les invites pour créer un projet sample
npx hardhat run scripts/deploy.js --network localhost
```

---

## Conclusion et livrables fournis
- Ce document couvre l'ensemble des questions de la Partie II (C25, C26) : risques, IAM, procédures, chiffrement, plan incident, blockchain et smart contract.
- Fichiers fournis dans le repo : `docs/partie_ii_securisation.md` et `docs/slides_partie_ii.md` (diapositives en Markdown) et un placeholder `docs/slides_partie_ii.pptx` (à générer depuis le markdown si souhaité).

Si tu veux, je peux maintenant :
- Générer une vraie `docs/slides_partie_ii.pptx` à partir du Markdown (j'utiliserai `pandoc` ou une librairie pour créer un PPTX), ou
- Créer un répertoire `poC-blockchain/` contenant le `Traceability.sol` et un script de déploiement Hardhat/Truffle et les instructions pour l'exécuter.

Dis‑moi ce que tu préfères et je l'ajoute immédiatement au dépôt.
