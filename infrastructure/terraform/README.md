# Terraform AWS pour DIGITRANS-CM

Ce dossier contient la configuration Terraform pour déployer une VPC, une instance EC2, une base RDS et un bucket S3.

## Fichiers
- main.tf
- variables.tf
- outputs.tf
- terraform.tfvars.example

## Commandes rapides
```powershell
cd infrastructure\terraform
gerald init
terraform validate
terraform plan -out=tfplan
terraform apply tfplan
```
