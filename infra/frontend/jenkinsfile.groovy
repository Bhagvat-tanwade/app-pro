pipeline {

    agent any

    parameters {

        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'stage', 'prod'],
            description: 'Target environment for deployment'
        )

        choice(
            name: 'ACTION',
            choices: ['create', 'delete'],
            description: 'Choose create or delete infrastructure'
        )

        booleanParam(
            name: 'AUTO_APPROVE',
            defaultValue: false,
            description: 'Automatically approve Terraform apply'
        )
    }

    environment {
        AWS_DEFAULT_REGION = 'ap-south-1'
    }

    stages {

        stage('Checkout') {
            steps {

                checkout scm

                script {
                    env.WORKSPACE_PATH =
                        "${WORKSPACE}/infra/frontend/env/${params.ENVIRONMENT}"

                    echo "Environment: ${params.ENVIRONMENT}"
                    echo "Terraform directory: ${env.WORKSPACE_PATH}"
                }
            }
        }


        stage('Verify Terraform') {
            steps {

                sh """
                    cd ${env.WORKSPACE_PATH}

                    terraform version
                """
            }
        }


        stage('Terraform Init') {
            steps {

                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {

                    sh """
                        cd ${env.WORKSPACE_PATH}

                        terraform init
                    """
                }
            }
        }


        stage('Terraform Plan') {
            steps {

                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {

                    script {

                        if (params.ACTION == 'delete') {

                            sh """
                                cd ${env.WORKSPACE_PATH}

                                terraform plan -destroy
                            """

                        } else {

                            sh """
                                cd ${env.WORKSPACE_PATH}

                                terraform plan
                            """
                        }
                    }
                }
            }
        }


        stage('Terraform Apply') {
            steps {

                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {

                    script {

                        if (params.AUTO_APPROVE) {

                            if (params.ACTION == 'delete') {

                                sh """
                                    cd ${env.WORKSPACE_PATH}

                                    terraform destroy -auto-approve
                                """

                            } else {

                                sh """
                                    cd ${env.WORKSPACE_PATH}

                                    terraform apply -auto-approve
                                """
                            }

                        } else {

                            if (params.ACTION == 'delete') {

                                input(
                                    message: "⚠️ DELETE Frontend/S3 infrastructure for ${params.ENVIRONMENT}?",
                                    ok: 'Yes, Delete'
                                )

                                sh """
                                    cd ${env.WORKSPACE_PATH}

                                    terraform destroy -auto-approve
                                """

                            } else {

                                input(
                                    message: "Apply Frontend/S3 changes for ${params.ENVIRONMENT}?",
                                    ok: 'Apply'
                                )

                                sh """
                                    cd ${env.WORKSPACE_PATH}

                                    terraform apply -auto-approve
                                """
                            }
                        }
                    }
                }
            }
        }


        stage('Output Values') {

            when {
                expression {
                    params.ACTION == 'create'
                }
            }

            steps {

                sh """
                    cd ${env.WORKSPACE_PATH}

                    echo "=========================================="
                    echo " Frontend S3 Infrastructure Created"
                    echo "=========================================="

                    terraform output
                """
            }
        }


        stage('Delete Confirmation') {

            when {
                expression {
                    params.ACTION == 'delete'
                }
            }

            steps {

                echo "=========================================="
                echo " Frontend S3 Infrastructure Deleted"
                echo "=========================================="

                echo "Environment: ${params.ENVIRONMENT}"
            }
        }
    }


    post {

        success {

            script {

                if (params.ACTION == 'delete') {

                    echo "✅ Frontend S3 infrastructure deleted successfully for ${params.ENVIRONMENT}!"

                } else {

                    echo "✅ Frontend S3 infrastructure created successfully for ${params.ENVIRONMENT}!"
                }
            }
        }


        failure {

            script {

                if (params.ACTION == 'delete') {

                    echo "❌ Frontend S3 infrastructure deletion failed for ${params.ENVIRONMENT}"

                } else {

                    echo "❌ Frontend S3 infrastructure creation failed for ${params.ENVIRONMENT}"
                }
            }

            echo "Check the Jenkins build logs for more details."
        }
    }
}

