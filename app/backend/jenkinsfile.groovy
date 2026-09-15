pipeline {

    agent any

    environment {
        AWS_REGION = 'ap-south-1'
        EKS_CLUSTER_NAME = 'backend-dev-cluster'
        ECR_REGISTRY = '056176270848.dkr.ecr.ap-south-1.amazonaws.com/easy-backend'
        IMAGE_NAME = 'easy-backend'
    }

    stages {

        stage('PULL') {
            steps {
                git branch: 'dev',
                    url: 'https://github.com/Bhagvat-tanwade/app-pro.git'
            }
        }

        // stage('BUILD') {
        //     steps {
        //         sh '''
        //             cd app/backend/
        //              clean package -DskipTests
        //         '''
        //     }
        // }

        // stage('DOCKER BUILD') {
        //     steps {
        //         sh '''
        //             cd app/backend/

        //             docker build \
        //                 -t $ECR_REGISTRY/$IMAGE_NAME:$BUILD_NUMBER \
        //                 -t $ECR_REGISTRY/$IMAGE_NAME:latest .
        //         '''
        //     }
        // }

        stage ('FRONTEND-BUILD-DOCKERFILE') {
            steps {
                sh '''cd app/backend/
                    docker build -t 056176270848.dkr.ecr.ap-south-1.amazonaws.com/easy-backend:latest .'''
            }
        }

        stage('PUSH') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                        

            
                        aws ecr get-login-password --region ap-south-1 | 
                        docker login --username AWS --password-stdin 056176270848.dkr.ecr.ap-south-1.amazonaws.com

                        docker push 056176270848.dkr.ecr.ap-south-1.amazonaws.com/easy-backend:latest
                    '''
                }
            }
        }

        // stage('APPLY') {
        //     steps {
        //         withCredentials([
        //             [$class: 'AmazonWebServicesCredentialsBinding',
        //              credentialsId: 'aws-credentials']
        //         ]) {
        //             sh '''
        //                 aws eks update-kubeconfig \
        //                     --region $AWS_REGION \
        //                     --name $EKS_CLUSTER_NAME

        //                 kubectl create namespace cloudblitz \
        //                     --dry-run=client -o yaml | kubectl apply -f -

        //                 sed -i "s|image: cloudblitz/auth-service:latest|image: $ECR_REGISTRY/$IMAGE_NAME:$BUILD_NUMBER|g" \
        //                     app/backend/auth-service/k8s/deployment.yaml

        //                 kubectl apply -f app/backend/auth-service/k8s/
        //             '''
        //         }
        //     }
        // }

        stage('APPLY') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                        aws eks update-kubeconfig \
                            --region $AWS_REGION \
                            --name $EKS_CLUSTER_NAME

                        kubectl apply -f app/backend/k8s/
                    '''
                }
            }
        }
    }
}
