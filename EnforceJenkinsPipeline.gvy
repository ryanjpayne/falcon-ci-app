pipeline {
    agent any
    environment {        
        ecrRepo = "<ECR Repository>"
        gitRepoUrl = "<CodeCommit Repo HTTPS Clone URL>"
        //
        region = "us-west-2"
        gitCredentials = "codecommit"
        myImageName = "falconcilab"
        myImageTag = "1.1"
        enforcePolicy = "true"
        scanTimeout = 120
    }
   
    stages {
        stage('Clone CodeCommit Repo') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: gitCredentials, url: gitRepoUrl]]])     
            }
        }
  
        stage('Build Docker Image') {
        steps{
            script {
            dockerImage = docker.build "${myImageName}:${myImageTag}"
            }
        }
        }

        stage('Scan Image with CrowdStrike Security') {
            steps{
                crowdStrikeSecurity imageName: myImageName, imageTag: myImageTag, enforce: enforcePolicy, timeout: scanTimeout
            }
        }

        stage('Push Image to ECR') {
        steps{  
            script {
                    sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecrRepo}"
                    sh "docker tag ${myImageName}:${myImageTag} ${ecrRepo}:${myImageTag}"
                    sh "docker push ${ecrRepo}:${myImageTag}"
            }
            }
        }
    }
}
