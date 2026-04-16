library 'f10-jenkins-library@1.1_patches'
library 'fips-jenkins-library@main'

pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'maven 3.9'
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '60', numToKeepStr: '10'))
    }

    stages {
        stage('Build') {
            steps {
                withMaven(publisherStrategy: 'EXPLICIT') {
                    sh "mvn -U -V -T 8 -fae clean install -DskipTests -f runtime"
                    sh "mvn -U -V -T 8 -fae clean install -DskipTests -f devtools/common"
                }
            }
        }

        stage('Dependency-Check') {
            steps {
                dir('runtime') {
                    withMaven(publisherStrategy: 'EXPLICIT') {
                        dependencyCheck outputFile: 'dependency-check-runtime-report.html'
                    }
                }
                dir('devtools/common') {
                    withMaven(publisherStrategy: 'EXPLICIT') {
                        dependencyCheck outputFile: 'dependency-check-devtools-report.html'
                    }
                }
                rtp parserName: 'HTML', nullAction: '1', stableText: """
                    <h2>Dependency-Check</h2>
                    <ul><li><a href='${env.BUILD_URL}artifact/dependency-check-runtime-report.html' target='_blank'>Dependency-Check Runtime Report</a></li>
                    <li><a href='${env.BUILD_URL}artifact/dependency-check-devtools-report.html' target='_blank'>Dependency-Check Devtools Common Report</a></li></ul>
                  """
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/dependency-check-*.html', allowEmptyArchive: true
        }

        regression {
            sendFailureEmail()
        }
    }
}
