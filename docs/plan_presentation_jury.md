# Plan de Présentation au Jury : Projet DIGITRANS-CM

Ce document propose un plan structuré de 10 à 15 minutes pour présenter et défendre votre infrastructure Cloud et Blockchain devant un jury. 

---

## Introduction (2 minutes)
**L'objectif : Capter l'attention et rappeler le besoin métier.**
- **Le contexte :** AGROCAM S.A a besoin de moderniser et sécuriser sa chaîne d'approvisionnement (Supply Chain) entre les plantations, les usines de transformation et les points de vente au Cameroun.
- **La problématique :** Comment assurer la haute disponibilité du système malgré les coupures réseau/courant fréquentes, garantir l'intégrité absolue des données de traçabilité, et sécuriser l'infrastructure ?
- **La solution proposée :** Une architecture hybride alliant la puissance et la flexibilité du **Cloud AWS** (pour le système central) et la sécurité de la **Blockchain** (pour la traçabilité immuable).

---

## 1. L'Architecture Cloud AWS : Comment ça marche et Pourquoi ces choix ? (4 minutes)
**L'objectif : Démontrer que vous n'avez pas cliqué au hasard, mais que chaque service répond à un besoin architectural strict.**

### A. Le Réseau : VPC (Virtual Private Cloud) et Sous-réseaux
- **Comment ça marche :** Nous avons créé un réseau privé virtuel dédié à AGROCAM, découpé en zones (sous-réseaux Publics et Privés).
- **Pourquoi au Jury :** *"Pour la sécurité. Nos serveurs d'application (API) et notre base de données ne sont pas tous exposés sur internet. Les flux sont filtrés par des Security Groups (pare-feu) stricts. Seuls les ports nécessaires (80/443/8080) sont ouverts."*

### B. Le Serveur Applicatif : Amazon EC2 (Elastic Compute Cloud)
- **Comment ça marche :** C'est la machine virtuelle qui exécute notre backend métier (Spring Boot) sous forme de conteneurs Docker.
- **Pourquoi au Jury :** *"Nous avons choisi EC2 pour avoir un contrôle total sur l'environnement d'exécution. Grâce à Docker, notre application est portable. Si le serveur tombe, nous pouvons relancer le conteneur instantanément sur une autre machine."*

### C. La Base de Données : Amazon RDS (Relational Database Service) - MySQL
- **Comment ça marche :** C'est notre base de données relationnelle.
- **Pourquoi au Jury :** *"Pourquoi RDS plutôt que d'installer MySQL sur un EC2 ? Parce que RDS est 'managé'. AWS s'occupe pour nous des mises à jour de sécurité, des sauvegardes automatiques quotidiennes (snapshots), et de la haute disponibilité. Cela libère l'équipe technique qui peut se concentrer sur le code métier."*

### D. Le Stockage : Amazon S3 (Simple Storage Service)
- **Comment ça marche :** Un espace de stockage objet ultra-résilient.
- **Pourquoi au Jury :** *"Nous utilisons S3 pour deux choses cruciales. Premièrement, pour stocker les fichiers de l'application (images, documents logistiques). Deuxièmement, pour stocker le fichier d'état (State) de Terraform de manière centralisée, sécurisée et chiffrée, ce qui permet le travail en équipe."*

---

## 2. Automatisation : L'Infrastructure as Code (IaC) et CI/CD (3 minutes)
**L'objectif : Montrer vos compétences DevOps et votre rigueur professionnelle.**

- **Terraform (L'infrastructure en tant que code) :**
  - *"Toute l'infrastructure que je viens de vous décrire n'a pas été cliquée à la main. Elle est entièrement codée via Terraform. Pourquoi ? Pour la **reproductibilité**. Si demain AGROCAM ouvre une filiale dans un autre pays, je peux déployer l'exacte même infrastructure sécurisée en 3 minutes grâce à une simple commande `terraform apply`."*

- **GitHub Actions (CI/CD) :**
  - *"Pour déployer les mises à jour, nous utilisons un pipeline automatisé. Dès qu'un développeur pousse du code : le code est testé automatiquement, l'image Docker est construite, poussée sur DockerHub, et Terraform vérifie que l'infrastructure est conforme, avant de déployer l'application sur EC2. L'erreur humaine est ainsi drastiquement réduite."*

---

## 3. L'Innovation : Blockchain et Traçabilité (3 minutes)
**L'objectif : Expliquer l'intégration du Web3 pour répondre aux contraintes réglementaires (Loi 2010/012) et de souveraineté.**

- **Pourquoi la Blockchain ?**
  - *"Une base de données MySQL (RDS) est performante mais modifiable par un administrateur système. Pour garantir à 100% que l'historique d'une marchandise n'a pas été falsifié, nous couplons RDS avec un Smart Contract Blockchain."*
- **Comment ça marche :**
  - *"À chaque mouvement de stock critique, notre backend enregistre les données de base dans RDS, mais génère aussi une empreinte cryptographique (hash) qu'il ancre dans la Blockchain. Le choix se porte sur une architecture de type Hyperledger ou Raft pour des questions de rapidité de validation et pour éviter la consommation énergétique du minage classique."*
- **Réponse aux risques locaux (Coupures réseau à Douala) :**
  - *"Si le réseau coupe, l'agent sur le terrain continue de travailler offline. Au retour du réseau, les données sont synchronisées avec le Cloud et horodatées dans la blockchain, garantissant la continuité de service."*

---

## Conclusion (1 minute)
- *"Pour conclure, le projet DIGITRANS-CM repose sur un triptyque solide : la puissance et la robustesse du **Cloud AWS** pour le cœur du SI, l'automatisation **DevOps** (Terraform/CI-CD) pour la fiabilité opérationnelle, et la **Blockchain** pour asseoir une confiance absolue dans la traçabilité des produits AGROCAM. Je suis maintenant à votre disposition pour répondre à vos questions."*

---

## Anticipation : Questions probables du Jury (Pour vous préparer)

1. **Jury : "Que se passe-t-il si la région AWS que vous utilisez tombe en panne (ex: us-east-1) ?"**
   *Réponse : "L'avantage de Terraform est que je peux changer la variable `aws_region` et redéployer l'intégralité de l'infrastructure sur une autre région (ex: Paris ou Francfort) en quelques minutes. Les données, elles, pourront être restaurées grâce aux snapshots RDS cross-région."*

2. **Jury : "Pourquoi utiliser Docker sur EC2 plutôt que des services comme ECS ou EKS (Kubernetes) ?"**
   *Réponse : "Pour une première phase de modernisation, EC2 avec Docker est le meilleur compromis coût/complexité. Kubernetes serait 'overkill' (trop lourd) et trop coûteux pour ce besoin initial. Si la charge augmente drastiquement, l'architecture étant déjà conteneurisée, la migration vers ECS/EKS sera très simple."*

3. **Jury : "N'est-il pas risqué de mettre le mot de passe de la DB dans les fichiers de code ?"**
   *Réponse : "Absolument. C'est pourquoi nous utilisons les GitHub Secrets. Le mot de passe n'est jamais stocké dans le code source Git, il est injecté à la volée comme variable d'environnement (TF_VAR_db_password) par le pipeline CI/CD."*
