library 'fips-jenkins-library@main'

pipeline {
    agent any

    tools {
        jdk 'AdoptiumJDK17'
        maven 'maven 3.8.6'
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '14'))
    }

    stages {
        stage('Build and Test') {

            environment {
                MAVEN_OPTS = '-Xmx4g'
            }

            steps {
                script {
                    currentBuild.displayName = "#${env.BUILD_NUMBER}.${env.GIT_BRANCH}-${env.GERRIT_TOPIC}"
                    sh 'rm -rf $HOME/.m2/repository/.meta'
                    sh 'rm -rf $HOME/.m2/repository/.cache'
                    sh 'rm -rf $HOME/.m2/repository/p2'
                }

                osSpecificMaven commands: [
                    "mvn -U -V -fae -e clean install -f codequality-config",
                    "mvn -U -V -T 8 -fae -e clean install -DskipTests=true -Dmaven.skip.tests=true -pl :targets -am -Dtycho.localArtifacts=ignore",
                    "mvn -U -V -T 8 -fae -e clean verify site -Dtycho.localArtifacts=ignore"
                    // site:site is not called for the review, as it depends on base and runtime which are not installed when built with only verify
                ]
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
                # copy all poms to jar-file-name.pom
                for i in $(find . -maxdepth 4 -name "*.jar" | grep -v 'sources\\|javadoc' | grep 'target/faktorips');
                do
                    to=$(echo $i | sed 's/.jar/.pom/g');
                    from=$(dirname $i | sed 's|/target|/pom.xml|g');
                    cp -v $from $to;
                done
                # use flattened poms
                for i in $(find . -maxdepth 2 -name .flattened-pom.xml | sed 's/.flattened-pom.xml//g' | xargs -i% find % -name *.jar | grep -v 'sources\\|javadoc' | grep 'target/faktorips' | grep -v 'bin');
                do
                    to=$(echo $i | sed 's/.jar/.pom/g');
                    from=$(dirname $i | sed 's|/target|/.flattened-pom.xml|g');
                    cp -v $from $to;
                done
                for i in $(find devtools/eclipse/sites -mindepth 2 -name .flattened-pom.xml | sed 's/.flattened-pom.xml//g' | xargs -i% find % -name *.jar | grep -v 'sources\\|javadoc' | grep 'target/org.faktorips' | grep -v 'bin');
                do
                    to=$(echo $i | sed 's/.jar/.pom/g');
                    from=$(dirname $i | sed 's|/target|/.flattened-pom.xml|g');
                    cp -v $from $to;
                done
                '''
            }
        }
    }

    post {
        always {
            parentReference referenceJob: "${JOB_NAME}", defaultBranch: 'hotfix/22.12',  targetBranch: '$GERRIT_BRANCH', latestBuildIfNotFound: false, latestCommitFallback: false, mergeOnlyJob: false, maxBuilds: 10, maxCommits: 500


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
