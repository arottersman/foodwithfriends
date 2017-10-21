variable "project" {
  default = "Food With Friends"
}

variable "environment" {
    default = "Staging"
}

variable "aws_region" {
  default = "us-east-1"
}

variable "aws_key_name" {}

variable "bastion_ami" {}

variable "bastion_instance_type" {
  default = "t2.nano"
}

# variable "aws_certificate_arn" {}

# variable "r53_public_hosted_zone_name" {}

variable "aws_availability_zones" {
  default = ["us-east-1c", "us-east-1d"]
}

variable "aws_s3_policy_arn" {
  default = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

variable "aws_cloudwatch_logs_policy_arn" {
  default = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

variable "vpc_cidr_block" {
  default = "10.0.0.0/16"
}

variable "external_access_cidr_block" {
  default = "66.212.12.106/32"
}

variable "vpc_private_subnet_cidr_blocks" {
  default = ["10.0.1.0/24", "10.0.3.0/24"]
}

variable "vpc_public_subnet_cidr_blocks" {
  default = ["10.0.0.0/24", "10.0.2.0/24"]
}

variable "rds_allocated_storage" {
  default = "5"
}

variable "rds_engine_version" {
  default = "9.6.2"
}

variable "rds_parameter_group_family" {
  default = "postgres9.6"
}

variable "rds_instance_type" {
  default = "db.t2.micro"
}

variable "rds_storage_type" {
  default = "gp2"
}

variable "rds_database_identifier" {}

variable "rds_database_name" {}

variable "rds_database_username" {}

variable "rds_database_password" {}

variable "rds_backup_retention_period" {
  default = "30"
}

variable "rds_backup_window" {
  default = "04:00-04:30"
}

variable "rds_maintenance_window" {
  default = "sun:04:30-sun:05:30"
}

variable "rds_auto_minor_version_upgrade" {
  default = true
}

variable "rds_final_snapshot_identifier" {
  default = "food-with-friends-rds-snapshot"
}

variable "rds_skip_final_snapshot" {
  default = false
}

variable "rds_copy_tags_to_snapshot" {
  default = true
}

variable "rds_multi_az" {
  default = false
}

variable "rds_storage_encrypted" {
  default = false
}
