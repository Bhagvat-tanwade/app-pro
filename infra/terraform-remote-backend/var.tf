# Terraform Backend Infrastructure Variables

variable "aws_region" {
  description = "AWS region for the backend infrastructure"
  type        = string
  default     = "ap-south-1"
}

variable "state_bucket_name" {
  description = "Name of the S3 bucket for Terraform state"
  type        = string
  default     = "terraform-state-bucket-cbz-kharadi-10"
}

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table for state locking"
  type        = string
  default     = "terraform-state-lock"
}

variable "tags" {
  description = "A map of tags to assign to the resources"
  type        = map(string)
  default = {
    Project     = "terraform-backend"
    ManagedBy   = "terraform"
    Environment = "shared"
  }
}
