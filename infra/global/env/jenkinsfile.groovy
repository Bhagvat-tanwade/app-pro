pipeline {

    agent any

    parameters {

        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'stage', 'prod'],
            description: 'Select environment'
        )

        choice(
            name: 'ACTION',
            choices: ['create', 'delete'],
            description: 'Select Terraform action'
        )
    }

    stages {

        stage('PULL') {
            steps {
                git branch: 'dev',
                    url: 'https://github.com/Bhagvat-tanwade/app-pro.git'
            }
        }

        stage('Terraform Init') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {

                    sh """
                        cd infra/global/env/${params.ENVIRONMENT}

                        cat > backend.tf <<EOF
terraform {
  backend "s3" {
    bucket         = "bhagvat-terraform-state-2026-09 "
    key            = "global/${params.ENVIRONMENT}/terraform-global.tfstate"
    region         = "ap-south-1"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
    use_lockfile   = true
  }
}
EOF

                        rm -rf .terraform
                        rm -f terraform.tfstate*
                        rm -f .terraform.lock.hcl

                        terraform init
                    """
                }
            }
        }

        stage('TERRAFORM PLAN') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {

                    sh """
                        cd infra/global/env/${params.ENVIRONMENT}

                        if [ "${params.ACTION}" = "delete" ]; then
                            terraform plan -destroy
                        else
                            terraform plan
                        fi
                    """
                }
            }
        }

        stage('APPROVAL') {
            steps {
                input(
                    message: "Do you want to continue with ${params.ACTION} for ${params.ENVIRONMENT}?",
                    ok: 'APPROVE'
                )
            }
        }

        stage('TERRAFORM APPLY') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {

                    sh """
                        cd infra/global/env/${params.ENVIRONMENT}

                        if [ "${params.ACTION}" = "delete" ]; then
                            terraform destroy -auto-approve
                        else
                            terraform apply -auto-approve
                        fi
                    """
                }
            }
        }
    }

    post {

        success {
            echo "✅ Terraform ${params.ACTION} completed successfully for ${params.ENVIRONMENT}"
        }

        failure {
            echo "❌ Terraform deployment failed"
        }
    }
}




