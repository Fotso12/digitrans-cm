% Slides — Partie II : Sécurisation

# Slide 1
## Contexte & Objectif
- Projet DIGITRANS-CM pour AGROCAM S.A.
- Objectif: sécuriser la traçabilité blockchain, conformité souveraineté, disponibilité.

# Slide 2
## Architecture globale
- On‑prem (données sensibles) + Cloud (services à forte charge)
- Backend Spring Boot, DB MySQL (RDS ou on‑prem), Blockchain privée (Fabric) ou PoC Ethereum

# Slide 3
## Blockchain choisie
- Recommandé : Hyperledger Fabric (permissioned)
- Consensus : Raft
- Avantages : contrôle identités, canaux privés, hébergement local

# Slide 4
## Politique de sécurité (synthèse)
- IAM : roles (ADMIN, DEVOPS, MANAGER, AGENT_TERRAIN, AUDIT)
- Rotation clés : DB every 30d, master keys 90d
- Chiffrement : TLS + KMS / Key Vault

# Slide 5
## Plan d'incident (clé compromise)
1. Isoler nœud compromis
2. Révoquer certificat/clé
3. Provisionner nouveau nœud, synchroniser
4. Revue logs & communication

# Slide 6
## Smart contract (PoC)
- Fonctions : `recordMovement`, `getHistory`
- Langage PoC : Solidity (Remix) ; production : chaincode Fabric

# Slide 7
## Démonstration & preuves
- Montrer `BlockchainService.java` (fallback)
- Démonstration Remix / Ganache (txHash)
- Fichier `docs/partie_ii_securisation.md` remis

# Slide 8
## Next steps & recommandations
- Automatiser rotation clés
- Mettre en place CI/CD + SAST/DAST
- Préparer environnement Fabric/consortium
