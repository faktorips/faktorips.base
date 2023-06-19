pipeline {
    agent none

    tools {
        jdk 'AdoptiumJDK17'
        maven 'maven 3.8.6'
    }

    environment {
        PROJECT_NAME = 'FaktorIPS nightly'
        PROJECT_ID = "${PROJECT_NAME}"
    }

    stages {
        stage('Setup') {
            matrix {
                agent any

                axes {
                    axis {
                        name 'TARGET_PLATFORM'
                        values '2022-12', '2023-03'
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

                            withMaven() {
                                configFileProvider([configFile(fileId: '82515eae-efcb-4811-8495-ceddc084409c', variable: 'TOOLCHAINS'), configFile(fileId: 'a447dcf9-7a34-4521-834a-c2445838a7e4', variable: 'MAVEN_SETTINGS')]) {
                                    sh 'mvn -U -V clean verify -s "$MAVEN_SETTINGS" -t "$TOOLCHAINS"'
                                }
                            }
                        }
                        post {
                            always {
                                junit testResults: "**/target/surefire-reports/*.xml", allowEmptyResults: true
                            }

                            unstable {
                                    emailext to: 'fips@faktorzehn.de', mimeType: 'text/html', subject: 'Jenkins Build Failure - $PROJECT_NAME', body: '''
                                        <img src="https://jenkins.io/images/logos/fire/fire.png" style="max-width: 300px;" alt="Jenkins is not happy about it ...">
                                        <br>
                                        $BUILD_URL
                                    '''
                            }
                        }
                    }
                }
            }
        }
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '14', numToKeepStr: '100'))
    }
}
