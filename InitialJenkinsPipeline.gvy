pipeline {
    agent any
    environment {
        gitRepoUrl = "<CodeCommit Repo HTTPS Clone URL>"
        //
        gitCredentials = "codecommit"
        myImageName = "falconcilab"
        myImageTag = "1.0"
        enforcePolicy = "false"
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
    }
}
