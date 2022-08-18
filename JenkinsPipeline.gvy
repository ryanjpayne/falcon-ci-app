pipeline {
    agent any
    environment {
        gitCredentials = "cc-creds-iam"
        gitRepoUrl = "https://git-codecommit.us-west-2.amazonaws.com/v1/repos/enzy6qrg-repo"
        //
        myImageName = "hello-world"
        myImageTag = "latest"
        enforce = "fail" // to allow, enforce = "never-fail"
        scanTimeout = 120 
        //
        region = "us-west-2"
        ecrRepo = "708248541114.dkr.ecr.us-west-2.amazonaws.com/hello-world"
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
                falconCISecurity imageName: myImageName, imageTag: myImageTag, onNonCompliance: enforce, timeout: scanTimeout
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