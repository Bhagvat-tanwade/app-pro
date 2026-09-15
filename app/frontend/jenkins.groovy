
pipeline {

    agent any

    environment {
        AWS_REGION = 'ap-south-1'
        S3_BUCKET = 'bhagvat.shop'
    }

    stages {

        stage('PULL') {
            steps {
                git branch: 'dev',
                    url: 'https://github.com/Bhagvat-tanwade/app-pro.git'
            }
        }

        stage('INSTALL') {
            steps {
                sh '''
                    cd app/frontend
                    npm install
                '''
            }
        }

        stage('BUILD') {
            steps {
                sh '''
                    cd app/frontend

                   
                    npm run build
                '''
            }
        }

        stage('UPLOAD') {
            steps {
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsBinding',
                     credentialsId: 'aws-credentials']
                ]) {
                    sh '''
                        cd app/frontend

                        aws s3 sync dist/ s3://$S3_BUCKET/ --delete
                    '''
                }
            }
        }

    }
}

