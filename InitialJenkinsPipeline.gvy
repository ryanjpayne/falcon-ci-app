pipeline {
    agent any
    environment {
        gitCredentials = "<CodeCommit Credentials ID>"
        gitRepoUrl = "<CodeCommit Repo HTTPS Clone URL>"
        //
        myImageName = "falconcilab"
        myImageTag = "latest"
        enforcePolicy = "false"
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
    }
}