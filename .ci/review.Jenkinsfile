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

                osSpecificMaven commands: [
                    "mvn -U -V -fae -e clean install -f codequality-config -Dmaven.repo.local=$MAVEN_REPO",
                    "mvn -U -V -T 8 -fae -e clean install -DskipTests=true -Dmaven.skip.tests=true -pl :targets -am -Dtycho.localArtifacts=ignore -Dmaven.repo.local=$MAVEN_REPO",
                    "mvn -U -V -T 8 -fae -e clean install site -Dtycho.localArtifacts=ignore -Dmaven.repo.local=$MAVEN_REPO"
                ]
            }
        }
        stage('Dependency-Check') {
            steps {
                dir('runtime'){
                    withMaven(publisherStrategy: 'EXPLICIT', mavenLocalRepo: "${env.MAVEN_REPO}") {
                        dependencyCheck outputFile: 'dependency-check-runtime-report.html'
                    }
                }
                dir('devtools/common'){
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
            parentReference referenceJob: "${JOB_NAME}", mergeBranch: 'main',  targetBranch: '$GERRIT_BRANCH', latestBuildIfNotFound: false, latestCommitFallback: false, mergeOnlyJob: false, maxBuilds: 10, maxCommits: 500

            junit testResults: "**/target/surefire-reports/*.xml", allowEmptyResults: true
            
            recordIssues enabledForFailure: true, qualityGates: [[threshold: 1, type: 'NEW', unstable: true]], tools: [java(), javaDoc(), spotBugs(), checkStyle(), eclipse()]
            
            publishCoverage(
                adapters: [jacocoAdapter(mergeToOneReport: true, path: '**/target/**/jacoco.xml')],
                sourceFileResolver: sourceFiles('STORE_ALL_BUILD'),
                sourceCodeEncoding: 'UTF-8',
                sourceDirectories: [
                    [path: 'devtools/common/faktorips-abstraction/src/main/java'],
                    [path: 'devtools/common/faktorips-abstraction-plainjava/src/main/java'],
                    [path: 'devtools/common/faktorips-abstraction-testsetup/src/main/java'],
                    [path: 'devtools/common/faktorips-dtfl-common/src/main/java'],
                    [path: 'devtools/common/faktorips-fl/src/main/java'],
                    [path: 'devtools/common/faktorips-model-plainjava/src/main/java'],
                    [path: 'devtools/common/faktorips-util/src/main/java'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.abstraction.eclipse/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.ant/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.core/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.core.refactor/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.core.ui/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.htmlexport/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.htmlexport.ui/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.model/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.model.builder/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.model.decorators/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.model.eclipse/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.stdbuilder/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.stdbuilder.ui/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.devtools.tableconversion/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.eclipse.emf.codegen/src'],
                    [path: 'devtools/eclipse/plugins/org.faktorips.m2e/src'],
                    [path: 'faktorips-maven-plugin/src/main/java'],
                    [path: 'runtime/faktorips-runtime/src/main/java'],
                    [path: 'runtime/faktorips-runtime-groovy/src/main/java'],
                    [path: 'runtime/faktorips-runtime-jakarta-xml/src/main/java'],
                    [path: 'runtime/faktorips-runtime-javax-xml/src/main/java'],
                    [path: 'runtime/faktorips-testsupport/src/main/java'],
                    [path: 'runtime/faktorips-valuetypes/src/main/java'],
                    [path: 'runtime/faktorips-valuetypes-joda/src/main/java']
                ]
            )
            
            archiveArtifacts artifacts: '**/target/*.jar, **/target/*.pom', fingerprint: true, onlyIfSuccessful: true
        }

        regression {
            failedEmail to: '$GERRIT_PATCHSET_UPLOADER_EMAIL'
        }
    }
}
