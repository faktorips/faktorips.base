library 'f10-jenkins-library@1.1_patches'
library 'release-library@1.0'
library 'fips-jenkins-library@main'

pipeline {
    agent none

    tools {
        jdk 'JDK21'
        maven 'maven 3.9'
    }

    environment {
        DISPLAY = ':0'
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '14', numToKeepStr: '100'))
    }

    stages {
        stage('Setup') {
            matrix {
                agent any

                axes {
                    axis {
                        name 'TARGET_PLATFORM'
                        values '2024-09', '2024-12', '2025-03', '2025-06', '2025-09'
                    }
                }
                stages {
                    stage('Build and Test') {
                        steps {
                            echo "Building ${TARGET_PLATFORM}"
                            script {
                                sh 'rm -rf $HOME/.m2/repository/.meta'
                                sh 'rm -rf $HOME/.m2/repository/.cache'
                                sh 'rm -rf $HOME/.m2/repository/p2'
                            }

                            withMaven(publisherStrategy: 'EXPLICIT') {
                                sh "mvn -U -V -T 8 clean verify"
                            }
                        }
                        post {
                            always {
                                junit testResults: "**/target/surefire-reports/*.xml", allowEmptyResults: true
                            }

                            unstable {
                                sendFailureEmail()
                            }
                        }
                    }
                }
            }
        }
    }
}
