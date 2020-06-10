variable "environment" {
  description = "testing"
}

variable "availability_zones" {
  type = "list"
  description = "The azs to use"
}


variable "private_subnets" {
  type = "map"
  description = "The private subnets to use"
}

variable "public_subnets" {
  type = "map"
  description = "The private subnets to use"
}

variable "region" {
  description = "Region that the instances will be created"
}

variable "sg_security_groups" {
  type = "map"
  description = "sg security groups"
}

