library 'f10-jenkins-library@1.1_patches'

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
                    sh "mvn -U -V -fae -e clean install -f codequality-config"
                    sh "mvn -U -V -T 8 -fae -e clean install -DskipTests=true -Dmaven.skip.tests=true -pl :targets -am -Dtycho.localArtifacts=ignore"
                    sh "mvn -U -V -T 8 -fae -e -P!rdp clean install -Dmaven.test.failure.ignore=true -Dmaven.test.skip=true -DskipTests=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Dfindbugs.skip=true -Dmaven.javadoc.skip=true -Dtycho.localArtifacts=ignore"
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
