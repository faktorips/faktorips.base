library 'f10-jenkins-library@1.1_patches'
library 'fips-jenkins-library@main'

pipeline {
    agent none

    tools {
        jdk 'JDK21'
        maven 'maven 3.9'
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
                        values '2023-12', '2024-03', '2024-06', '2024-09', '2024-12'
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

                            osSpecificMaven commands: [
                                "mvn -U -V -T 8 clean verify"
                            ]
                        }
                        post {
                            always {
                                junit testResults: "**/target/surefire-reports/*.xml", allowEmptyResults: true
                            }

                            unstable {
                                failedEmail to: 'fips@faktorzehn.de'
                            }
                        }
                    }
                }
            }
        }
    }
}
