/*====
ECS task definitions, this resource is only useful when building a service. It looks for a cluster or service (container)
to resgister task to.
======*/

data aws_vpc "vpc" {
  tags = {
    Name = "vpc-${var.environment}"
    Enviorment = var.environment
  }
}

/*====
Cloudwatch Log Group
======*/
resource "aws_cloudwatch_log_group" "kafka_log_group" {
  name = "kafka-msk-${var.environment}"
  tags = {
    Environment = var.environment
    Application = "MSK Kafka Cluster"
  }
  retention_in_days = 14

}

data aws_subnet_ids "private_subnets" {
  vpc_id = data.aws_vpc.vpc.id
  tags = {
    Environment = var.environment
    Tier = "Private"
  }
}

data aws_security_group "msk_sg" {
  tags = {
    Name = "bd-security-group-${var.environment}"
    Tier = "Default"
  }
}

data aws_kms_key "kms" {
  key_id = "arn:aws:kms:us-west-2:712639424220:alias/poly-key-${var.environment}"
}

# kafka
resource "aws_security_group" "msk-security-group" {
  name = "msk-sg-${var.environment}"
  description = "Kafka security group to allow inbound/outbound from the VPC"
  vpc_id = data.aws_vpc.vpc.id
  depends_on = [
    data.aws_vpc.vpc]

  ingress {
    from_port = "2181"
    to_port = "2181"
    protocol = "TCP"
    self = true
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  ingress {
    from_port = "9092"
    to_port = "9092"
    protocol = "TCP"
    self = true
    cidr_blocks = [
      "0.0.0.0/0"]
    description = "plaintext traffic"
  }

  ingress {
    from_port = "9094"
    to_port = "9094"
    protocol = "TCP"
    self = true
    cidr_blocks = [
      "0.0.0.0/0"]
    description = "TLS-encrypted traffic"
  }

  egress {
    from_port = "0"
    to_port = "0"
    protocol = "-1"
    self = "true"
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  tags = {
    Environment = var.environment
    Name = "bd-security-group-${var.environment}"
    Tier = "Default"
  }
}

/* https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/FilterAndPatternSyntax.html */
resource "aws_cloudwatch_log_metric_filter" "kafka_error" {
  log_group_name = aws_cloudwatch_log_group.kafka_log_group.name
  name = "MSK-ERR-${var.environment}"
  pattern = "ERROR"
  metric_transformation {
    name = "MSKErrorCount"
    namespace = "MSKCluster"
    value = "1"
  }
}

resource "aws_msk_cluster" "msk_poc" {
  cluster_name = "poly-kafka-${var.environment}"
  kafka_version = "2.2.1"
  number_of_broker_nodes = 3
  broker_node_group_info {
    // https://www.terraform.io/docs/configuration/functions/slice.html
    client_subnets = slice(tolist(data.aws_subnet_ids.private_subnets.ids), 0, 3)
    ebs_volume_size = 1000
    instance_type = "kafka.t3.small"
    //security_groups = flatten([split(",", var.sg_security_groups[var.environment])])
    security_groups = [
      data.aws_security_group.msk_sg.name,
      aws_security_group.msk-security-group.name]
  }

  encryption_info {
    encryption_at_rest_kms_key_arn = data.aws_kms_key.kms.arn
  }

  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled = true
        log_group = aws_cloudwatch_log_group.kafka_log_group.name
      }
    }
  }

  tags = {
    Name = "poly-kafka-${var.environment}"
  }
}

resource "aws_msk_configuration" "msk_config" {
  kafka_versions = [
    "2.2.1"]
  name = "poly-kafka-${var.environment}"

  server_properties = <<PROPERTIES
auto.create.topics.enable = true
delete.topic.enable = true
PROPERTIES
}