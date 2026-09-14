variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-south-1"
}


variable "domain_name" {
  description = "Domain name to use as the S3 bucket name"
  type        = string
  default = "bhagvat.shop"
}


variable "enable_versioning" {
  description = "Enable versioning on the S3 bucket"
  type        = bool
  default     = true
}


variable "index_document" {
  description = "Index document for the static website"
  type        = string
  default     = "index.html"
}


variable "error_document" {
  description = "Error document for the static website"
  type        = string
  default     = "error.html"
}


variable "tags" {
  description = "Tags for the S3 bucket"
  type        = map(string)

  default = {
    Environment = "dev"
    Project     = "frontend-infrastructure"
    ManagedBy   = "terraform"
    Component   = "frontend"
  }
}
