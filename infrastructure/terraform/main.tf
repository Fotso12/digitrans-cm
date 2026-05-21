terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_vpc" "digitrans_vpc" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags = { Name = "digitrans-vpc" }
}

resource "aws_subnet" "public" {
  vpc_id            = aws_vpc.digitrans_vpc.id
  cidr_block        = var.public_subnet_cidr
  availability_zone = data.aws_availability_zones.available.names[0]
  map_public_ip_on_launch = true
  tags = { Name = "digitrans-public-subnet" }
}

resource "aws_subnet" "private" {
  vpc_id            = aws_vpc.digitrans_vpc.id
  cidr_block        = var.private_subnet_cidr
  availability_zone = data.aws_availability_zones.available.names[1]
  tags = { Name = "digitrans-private-subnet" }
}

data "aws_availability_zones" "available" { state = "available" }

resource "aws_security_group" "api_sg" {
  name        = "digitrans-api-sg"
  description = "Security group for Spring Boot API"
  vpc_id      = aws_vpc.digitrans_vpc.id
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.allowed_ssh_cidr
  }
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "digitrans-api-sg" }
}

resource "aws_security_group" "rds_sg" {
  name        = "digitrans-rds-sg"
  description = "Security group for RDS MySQL"
  vpc_id      = aws_vpc.digitrans_vpc.id
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.api_sg.id]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "digitrans-rds-sg" }
}

resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "digitrans-rds-subnet-group"
  subnet_ids = [aws_subnet.public.id, aws_subnet.private.id]
  tags = { Name = "digitrans-rds-subnet-group" }
}

resource "aws_db_instance" "mysql" {
  identifier             = "digitrans-mysql"
  engine                 = "mysql"
  engine_version         = var.db_engine_version
  instance_class         = var.db_instance_class
  allocated_storage      = var.db_allocated_storage
  name                   = var.db_name
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  backup_retention_period = 7
  skip_final_snapshot    = var.skip_final_snapshot
  publicly_accessible    = false
  tags = { Name = "digitrans-mysql-db" }
}

resource "aws_s3_bucket" "digitrans_bucket" {
  bucket = "digitrans-cm-bucket-${var.environment}-${data.aws_caller_identity.current.account_id}"
  tags = { Name = "digitrans-cm-bucket" }
}

data "aws_caller_identity" "current" {}
