#intializes variables from the ecs module to take variibles from the stage enviorment i.e. production, production
module "kafka" {
  source = "../../producer/kafka/modules"
  environment = var.environment
  public_subnets = var.public_subnets
  private_subnets = var.private_subnets
  sg_security_groups = var.sg_security_groups
  region = var.region
  availability_zones = var.availability_zone
}