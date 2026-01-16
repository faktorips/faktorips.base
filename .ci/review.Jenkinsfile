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
                // only use successful builds as a reference, since (randomly) failed jobs might have failed before all warnings have occurred
                discoverReferenceBuild referenceJob: "${REFERENCE_JOB}", requiredResult: 'SUCCESS'

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
