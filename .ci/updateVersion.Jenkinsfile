library 'f10-jenkins-library@1.1_patches'
library 'fips-jenkins-library@main'
import java.text.MessageFormat

def lib = library('fips-jenkins-library@main').org.faktorips.jenkins

pipeline {
    agent any

    parameters {
        string description: 'Die nächste Version, z.B. 22.12.1 (-SNAPSHOT wird automatisch hinzugefügt)', name: 'NEW_VERSION'
        string defaultValue: 'origin/main', description: 'Der zu bauende Branch', name: 'BRANCH'
    }

    tools {
        jdk 'JDK21'
        maven 'maven 3.9'
    }

    options {
        skipDefaultCheckout true
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    currentBuild.displayName = "Update version to ${params.NEW_VERSION} (${params.BRANCH})"

                    assert params.NEW_VERSION ==~ /(\d+\.)+\d+/

                    def scmVars = checkout([
                        $class: 'GitSCM',
                        branches: [[name: "${params.BRANCH}"]],
                        extensions: [[$class: 'WipeWorkspace'], [$class: 'LocalBranch']],
                        userRemoteConfigs: scm.userRemoteConfigs
                    ])

                    LOCAL_BRANCH = scmVars.GIT_LOCAL_BRANCH
                    
                    def xmlfile = readFile 'pom.xml'
                    oldVersion = lib.MavenProjectVersion.fromPom(xmlfile)
                    newVersion = params.NEW_VERSION+'-SNAPSHOT'
                }
            }
        }

        stage('Update versions') {
            steps {
                osSpecificMaven commands: [
                    "mvn -V org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${newVersion} -DgenerateBackupPoms=false -Dartifacts=base,codequality-config,faktorips-coverage,faktorips-schemas,faktorips-runtime-bom,faktorips-devtools-bom"
                ]
                // see https://github.com/eclipse-tycho/tycho/issues/1677
                sh "find devtools/eclipse/targets/ -type f -name 'eclipse-*.target' -exec sed -i 's/${oldVersion}/${newVersion}/' {} \\;"
                sh "git add . && git commit -m 'Update version to ${newVersion}'"
                sh "git push origin ${LOCAL_BRANCH}"
            }
        }
    }

    post {
        unsuccessful {
            failedEmail to: 'fips@faktorzehn.de'
        }
    }
}
