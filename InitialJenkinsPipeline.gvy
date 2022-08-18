pipeline {
    agent any
    environment {
        gitCredentials = "cc-creds-iam"
        gitRepoUrl = "https://git-codecommit.us-west-2.amazonaws.com/v1/repos/enzy6qrg-repo"
        //
        myImageName = "hello-world"
        myImageTag = "latest"
        enforce = "never-fail"
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
                falconCISecurity imageName: myImageName, imageTag: myImageTag, onNonCompliance: enforce, timeout: scanTimeout
            }
        }
    }
}