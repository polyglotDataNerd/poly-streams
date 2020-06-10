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
resource "aws_cloudwatch_log_group" "api-pr_log_group" {
  name = "api-pr-msk-${var.environment}"
  tags = {
    Environment = var.environment
    Application = "API Gateway Proxy MSK"
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

data aws_security_group "api_sg" {
  tags = {
    Name = "bd-security-group-${var.environment}"
    Tier = "Default"
  }
}

/* https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/FilterAndPatternSyntax.html */
resource "aws_cloudwatch_log_metric_filter" "api-pr_error" {
  log_group_name = aws_cloudwatch_log_group.api-pr_log_group.name
  name = "APIGW-ERR-${var.environment}"
  pattern = "ERROR"
  metric_transformation {
    name = "APIGWErrorCount"
    namespace = "APIGWProxy"
    value = "1"
  }
}

resource "aws_api_gateway_rest_api" "pr_api" {
  name = "api-pr-msk-${var.environment}"
  description = "webhook proxy for AWS MSK Kafka"
}

resource "aws_api_gateway_resource" "pr_proxy" {
  rest_api_id = aws_api_gateway_rest_api.pr_api.id
  parent_id = aws_api_gateway_rest_api.pr_api.root_resource_id
  path_part = "{proxy+}"
}

resource "aws_api_gateway_method" "pr_method" {
  rest_api_id = aws_api_gateway_rest_api.pr_api.id
  resource_id = aws_api_gateway_resource.pr_proxy.id
  http_method = "ANY"
  authorization = "NONE"
  //authorizer_id = "${aws_api_gateway_authorizer.basic_auth.id}"

  /*if resource path was set to a greedy variable {proxy+}*/
  request_parameters = {
    "method.request.path.proxy" = true
  }
}

resource "aws_api_gateway_integration" "pr_integration" {
  http_method = aws_api_gateway_method.pr_method.http_method
  resource_id = aws_api_gateway_resource.pr_proxy.id
  rest_api_id = aws_api_gateway_rest_api.pr_api.id
  type = "HTTP_PROXY"
  integration_http_method = "ANY"
}

/*====
Enable CORS for Cross-origin resource sharing
======*/

resource "aws_api_gateway_gateway_response" "gateway_response" {
  rest_api_id = aws_api_gateway_rest_api.pr_api.id
  status_code = "401"
  response_type = "UNAUTHORIZED"

  response_templates = {
    "application/json" = "{'message':$context.error.messageString}"
  }

  response_parameters = {
    "gatewayresponse.header.WWW-Authenticate" = "'Basic'"
  }
}


//resource "aws_api_gateway_authorizer" "basic_auth" {
//  name = "api-pr-msk-basicauth-${var.environment}"
//  rest_api_id = aws_api_gateway_rest_api.pr_api.id
//  authorizer_uri = "${aws_lambda_function.authorizer.invoke_arn}"
//  authorizer_credentials = "${var.lambda_role}"
//  identity_source = "method.request.header.authorization"
//  //identity_source = "method.request.header.Authorization"
//  type = "REQUEST"
//  authorizer_result_ttl_in_seconds = 300
//}