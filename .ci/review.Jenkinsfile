library 'f10-jenkins-library@1.1_patches'
library 'fips-jenkins-library@main'

pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'maven 3.9'
    }

    environment {
        MAVEN_OPTS = '-Xmx4g'
        MAVEN_REPO = '/tmp/$JOB_NAME/$GERRIT_CHANGE_ID'
        DISPLAY = ':0'
        REFERENCE_JOB = 'FaktorIPS_CI'
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '14'))
    }

    stages {
        stage('Build and Test') {

            steps {
                script {
                    currentBuild.displayName = "#${env.BUILD_NUMBER}.${env.GIT_BRANCH}-${env.GERRIT_TOPIC}"
                }

                withMaven(mavenLocalRepo: "${env.MAVEN_REPO}", publisherStrategy: 'EXPLICIT') {
                    sh "mvn -U -V -fae -e clean install -f codequality-config"
                    sh "mvn -U -V -T 8 -fae -e clean install -DskipTests=true -Dmaven.skip.tests=true -pl :targets -am -Dtycho.localArtifacts=ignore"
                    sh "mvn -U -V -T 8 -fae -e clean install site checkstyle:checkstyle -Dtycho.localArtifacts=ignore"
                }
            }
        }
        stage('Dependency-Check') {
            steps {
                dir('runtime') {
                    withMaven(publisherStrategy: 'EXPLICIT', mavenLocalRepo: "${env.MAVEN_REPO}") {
                        dependencyCheck outputFile: 'dependency-check-runtime-report.html'
                    }
                }
                dir('devtools/common') {
                    withMaven(publisherStrategy: 'EXPLICIT', mavenLocalRepo: "${env.MAVEN_REPO}") {
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
        stage('Prepare Artifacts for Archiving') {
            steps {
                sh '''
                #!/bin/bash
                # for DependsOn to work we need to publish artifacts to jenkins in a certain way

                echo "Copy and rename pom files to match the jar artifacts"
                # copy repos zip to jars
                for i in $(find . -maxdepth 6 -name "*.zip" | grep -v 'sources\\|javadoc' | grep 'target/org.faktorips');
                do
                    to=$(echo $i | sed 's/.zip/.jar/g');
                    cp -v $i $to;
                done
                # copy all .flattened-pom.xml to jar-file-name.pom
                # finds all faktorips* (maven projects) and org.faktorips* (eclipse projects) jar-files
                # copies their project-name/.flattened-pom.xml to project-name/target/project-name-version.pom
                # archiving will be done by jenkins on all jar-/pom-files in the target folder
                for i in $(find . -name "*.jar" | grep -v 'sources\\|javadoc' | grep 'target/\\(org\\.\\)*faktorips' | grep -v '/bin/'); 
                do
                    to=$(echo $i | sed 's/.jar/.pom/g');
                    from=$(dirname $i)/../.flattened-pom.xml;
                    cp -v $from $to;
                done
                '''
            }
        }
    }

    post {
        always {
            script {
                try {
                    parentReference referenceJob: "${JOB_NAME}", mergeBranch: 'main', targetBranch: '$GERRIT_BRANCH', latestBuildIfNotFound: false, latestCommitFallback: false, mergeOnlyJob: false, maxBuilds: 10, maxCommits: 500
                } catch (Throwable e) {
                    discoverReferenceBuild referenceJob: "${REFERENCE_JOB}", requiredResult: "FAILURE"
                }

                junit testResults: "**/target/surefire-reports/*.xml", allowEmptyResults: true
                params = [
                    enabledForFailure: true,
                    qualityGates     : [[threshold: 1, type: 'NEW', unstable: true]],
                    tools            : [java(), javaDoc(), spotBugs(), checkStyle(), eclipse()],
                ]
                recordIssues params
                jacoco sourceInclusionPattern: '**/*.java'
                archiveArtifacts artifacts: '**/target/*.jar, **/target/*.pom', fingerprint: true, onlyIfSuccessful: true
            }
        }
    }
}
