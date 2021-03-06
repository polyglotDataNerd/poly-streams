#!/usr/bin/env bash
AWS_ACCESS_KEY_ID=$(aws ssm get-parameters --names /s3/polyglotDataNerd/admin/AccessKey --query Parameters[0].Value --with-decryption --output text)
AWS_SECRET_ACCESS_KEY=$(aws ssm get-parameters --names /s3/polyglotDataNerd/admin/SecretKey --query Parameters[0].Value --with-decryption --output text)
GIT_TOKEN=$(aws ssm get-parameters --names /s3/polyglotDataNerd/admin/GitToken --query Parameters[0].Value --with-decryption --output text)
CURRENTDATE="$(date  +%Y)"
#shell parameter for env.
environment=$1


#copy tfstate files into dir
aws s3 cp s3://bigdata-utility/terraform/kafka/api/$environment/$CURRENTDATE ~/solutions/poly-streams/infrastructure/eventstream/api/kafka  --recursive --sse --quiet --include "*"

export TF_VAR_awsaccess=$AWS_ACCESS_KEY_ID
export TF_VAR_awssecret=$AWS_SECRET_ACCESS_KEY
export TF_VAR_environment=$environment
cd ~/solutions/poly-streams/infrastructure/eventstream/api/kafka
terraform init
terraform get
terraform plan
terraform destroy -auto-approve

#copy tfstate files to s3
aws s3 cp ~/solutions/poly-streams/infrastructure/eventstream/api/kafka s3://bigdata-utility/terraform/kafka/api/$environment/$CURRENTDATE/  --recursive --sse --quiet --exclude "*" --include "*terraform.tfstate*"

cd ~/solutions/poly-network-infrastructure/




