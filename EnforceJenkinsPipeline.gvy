pipeline {
    agent any
    environment {
        region = "<Your AWS Region>"
        ecrRepo = "<ECR Repository>"
        gitRepoUrl = "<CodeCommit Repo HTTPS Clone URL>"
        //
        gitCredentials = "codecommit"
        myImageName = "falconcilab"
        myImageTag = "latest"
        enforcePolicy = "true"
        scanTimeout = 120        
    }
   
    stages {
        stage('Cloning Git') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/main']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: gitCredentials, url: gitRepoUrl]]])     
            }
        }
  
        // Building Docker images
        stage('Build') {
        steps{
            script {
            dockerImage = docker.build myImageName
            }
        }
        }

        // Scan Image with Falcon CI
        stage('Scanning Image with Falcon CI Security') {
            steps{
                falconCISecurity imageName: myImageName, imageTag: myImageTag, enforce: enforcePolicy, timeout: scanTimeout
            }
        }
        // Uploading Docker images into AWS ECR
        stage('Pushing to ECR') {
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