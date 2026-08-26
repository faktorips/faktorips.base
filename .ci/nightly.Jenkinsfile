library 'f10-jenkins-library@1.1_patches'
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

    stages {
        stage('Setup') {
            matrix {
                agent any

                axes {
                    axis {
                        name 'TARGET_PLATFORM'
                        values '2022-12', '2023-03', '2023-06', '2023-09', '2023-12', '2024-03'
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

                            // the build runs on JDK 21 for Tycho 5, while the bundles keep BREE JavaSE-17
                            // and tycho-surefire resolves the test JVM from this toolchain;
                            // JDK11/JDK8 are needed for runtime modules whose java.version is still 11/1.8
                            createToolchain jdk: ['AdoptiumJDK17', 'JDK11', 'JDK8'], outputFile: 'toolchains.xml'
                            withMaven(publisherStrategy: 'EXPLICIT') {
                                sh "mvn -U -V -T 8 clean verify -Dtarget-platform=eclipse-${TARGET_PLATFORM} -Drevapi.skip=true -t toolchains.xml"
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
