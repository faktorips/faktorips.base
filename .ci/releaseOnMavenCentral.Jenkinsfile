library 'f10-jenkins-library@1.1_patches'
library 'maven-central-release-library@main'

pipeline {
    agent any

    parameters {
        string description: 'The UUID of the deployment request', name: 'DEPLOYMENT_ID'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30'))
        skipDefaultCheckout true
    }

    stages {
        stage('Release on Maven Central') {
            steps {
                script {
                    releaseOnMavenCentral(params.DEPLOYMENT_ID)
                }
            }
        }
    }

    post {
        unsuccessful {
            sendFailureEmail()
        }
    }
}
