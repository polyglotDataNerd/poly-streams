/*====
ECS task definitions, this resource is only useful when building a service. It looks for a cluster or service (container)
to resgister task to.
======*/

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

data aws_subnet "private_subnets" {
  tags = {
    Name = "test"
  }
}

data aws_kms_key "kms"  {
  tags {
    Name = "test"
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
    client_subnets = flatten([
      split(",", var.private_subnets[var.environment])])
    ebs_volume_size = 1000
    instance_type = "kafka.t3.small"
    security_groups = flatten([
      split(",", var.sg_security_groups[var.environment])])
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

  tags {
    Name = "poly-kafka-${var.environment}"
  }
}

resource "aws_msk_configuration" "example" {
  kafka_versions = [
    "2.2.1"]
  name = "poly-kafka-${var.environment}"

  server_properties = <<PROPERTIES
auto.create.topics.enable = true
delete.topic.enable = true
PROPERTIES
}