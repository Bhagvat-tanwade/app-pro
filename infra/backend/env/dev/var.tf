# Backend Infrastructure Variables - Development Environment

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-south-1"
}

variable "vpc_name" {
  description = "Name prefix for VPC resources"
  type        = string
  default     = "backend-dev-vpc"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.2.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "Map of availability zones to public subnet CIDR blocks"
  type        = map(string)
  default = {
    "ap-south-1a" = "10.2.1.0/24"
    "ap-south-1b" = "10.2.2.0/24"
    "ap-south-1c" = "10.2.3.0/24"
  }
}

variable "private_subnet_cidrs" {
  description = "Map of availability zones to private subnet CIDR blocks"
  type        = map(string)
  default = {
    "ap-south-1a" = "10.2.10.0/24"
    "ap-south-1b" = "10.2.20.0/24"
    "ap-south-1c" = "10.2.30.0/24"
  }
}

variable "cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "backend-dev-cluster"
}

variable "cluster_version" {
  description = "Kubernetes version for the EKS cluster"
  type        = string
  default     = "1.35"
}

variable "cluster_endpoint_public_access_cidrs" {
  description = "List of CIDR blocks which can access the Amazon EKS public API server endpoint"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "log_retention_days" {
  description = "Number of days to retain log events in CloudWatch"
  type        = number
  default     = 7
}

variable "node_groups" {
  description = "Map of EKS managed node group definitions"
  type = map(object({
    instance_types = list(string)
    ami_type       = string
    capacity_type  = string
    scaling_config = object({
      desired_size = number
      max_size     = number
      min_size     = number
    })
    update_config = object({
      max_unavailable_percentage = number
    })
    tags = map(string)
  }))
  default = {
    main = {
      instance_types = ["c7i-flex.large"]
      ami_type       = "AL2023_x86_64_STANDARD"
      capacity_type  = "ON_DEMAND"
      scaling_config = {
        desired_size = 2
        max_size     = 4
        min_size     = 1
      }
      update_config = {
        max_unavailable_percentage = 25
      }
      tags = {
        NodeGroup = "main"
        Component = "backend"
      }
    }
  }
}

variable "tags" {
  description = "A map of tags to assign to the resources"
  type        = map(string)
  default = {
    Environment = "dev"
    Project     = "backend-infrastructure"
    ManagedBy   = "terraform"
    Component   = "backend"
  }
}
