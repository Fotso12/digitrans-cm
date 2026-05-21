# PoC Blockchain - DIGITRANS-CM

Ce répertoire contient le Proof of Concept (PoC) pour le système de traçabilité basé sur la blockchain Ethereum (Smart Contract Solidity).

## Fichiers inclus
- `Traceability.sol` : Le Smart Contract de traçabilité.

## Déploiement rapide avec Remix Desktop (Recommandé pour démo)

1. Ouvrez [Remix IDE](https://remix.ethereum.org/) (Web ou Desktop).
2. Créez un nouveau fichier `Traceability.sol` dans l'espace de travail Remix et copiez-y le contenu du fichier local.
3. Allez dans l'onglet **Solidity Compiler** :
   - Choisissez la version de compilateur `0.8.17` (ou plus récente).
   - Cliquez sur **Compile Traceability.sol**.
4. Allez dans l'onglet **Deploy & Run Transactions** :
   - **Environment** : Choisissez `Remix VM (London)` pour un test instantané sans configuration.
   - Cliquez sur **Deploy**.
5. Sous **Deployed Contracts**, vous pouvez maintenant interagir avec les fonctions `recordMovement` et `getHistory`.

## Simulation avec le Backend (Ganache)

Si vous souhaitez lier ce contrat à votre backend Spring Boot :
1. Installez et lancez [Ganache](https://trufflesuite.com/ganache/) pour avoir un nœud local Ethereum (exposé par défaut sur `http://localhost:8545`).
2. Dans Remix, choisissez comme **Environment** l'option `Dev - Ganache Provider` ou `Injected Provider - MetaMask` (en configurant MetaMask sur localhost:8545).
3. Déployez le contrat.
4. Récupérez l'adresse du contrat déployé et renseignez-la dans le fichier `application.properties` de votre backend (ainsi que l'URL du nœud local).
