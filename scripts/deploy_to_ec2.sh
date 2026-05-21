#!/usr/bin/env bash
set -euo pipefail

# Script de déploiement exécuté sur l'EC2
# Arguments: DOCKERHUB_USERNAME DOCKERHUB_TOKEN BACKEND_IMAGE
DOCKERHUB_USERNAME=${1}
DOCKERHUB_TOKEN=${2}
BACKEND_IMAGE=${3}

echo "Démarrage du script de déploiement sur $(hostname)"

# Mise à jour et installation des outils de base
sudo yum update -y || true
sudo yum install -y curl tar gzip iproute || true

# Installer Docker si absent
if ! command -v docker >/dev/null 2>&1; then
  sudo amazon-linux-extras install -y docker || sudo yum install -y docker
  sudo systemctl enable --now docker
  sudo usermod -aG docker ec2-user || true
fi

# Installer k0s (cluster Kubernetes léger) si absent
if ! command -v k0s >/dev/null 2>&1; then
  echo "Installation de k0s (controller single-node)"
  curl -sSLf https://get.k0s.sh | sudo sh
  sudo k0s install controller --single
  sudo systemctl enable --now k0scontroller
  sleep 10
fi

# Installer kubectl si absent
if ! command -v kubectl >/dev/null 2>&1; then
  echo "Installation de kubectl"
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
  sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
  rm kubectl || true
fi

# Configurer kubeconfig pour l'utilisateur ec2-user
K0S_KUBECONFIG=/var/lib/k0s/pki/admin.conf
sudo chmod +r ${K0S_KUBECONFIG}
mkdir -p /home/ec2-user/.kube
sudo cp ${K0S_KUBECONFIG} /home/ec2-user/.kube/config
sudo chown ec2-user:ec2-user /home/ec2-user/.kube/config

# Login DockerHub pour tirer l'image privée
echo "Connexion à DockerHub en tant que ${DOCKERHUB_USERNAME}"
echo "${DOCKERHUB_TOKEN}" | docker login --username "${DOCKERHUB_USERNAME}" --password-stdin

# Tirer l'image backend
docker pull ${BACKEND_IMAGE}

# Remplacer le placeholder dans le manifest k8s si nécessaire
if [ -f /home/ec2-user/k8s/backend-deployment.yaml ]; then
  sed -i "s|REPLACE_WITH_IMAGE|${BACKEND_IMAGE}|g" /home/ec2-user/k8s/backend-deployment.yaml || true
fi

# Appliquer les manifests Kubernetes
kubectl apply -f /home/ec2-user/k8s/backend-deployment.yaml
kubectl apply -f /home/ec2-user/k8s/backend-service.yaml

# Attendre le déploiement
kubectl rollout status deployment/digitrans-backend --timeout=120s || true
kubectl get pods -o wide

echo "Déploiement terminé"
