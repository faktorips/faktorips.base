@Library('f10-jenkins-library@1.1_patches')
@Library('maven-central-release-library@main')
@Library('fips-jenkins-library@main')

import java.text.MessageFormat
import org.faktorips.jenkins.MavenProjectVersion

def p2RepositoryFolder = './devtools/eclipse/sites/org.faktorips.p2repository'
def mavenDocFolder = './maven/faktorips-maven-plugin'
def xsdFolder = './devtools/common/faktorips-schemas/src/main/resources'

def mavenDocDeployFolderTmpl = '/var/www/doc.faktorzehn.org/faktorips-maven-plugin/{0}.{1}'
def xsdDeployFolderTmpl = '/var/www/doc.faktorzehn.org/schema/faktor-ips/{0}.{1}'
def archiveZipFileTmpl = 'org.faktorips.p2repository-{0}.zip'
def archiveDeployDirTmpl = '/var/www/update.faktorzehn.org/faktorips/v{0}_{1}/downloads/faktorips-{0}.{1}'
def ps2DeployDirTmpl = '/var/www/update.faktorzehn.org/faktorips/v{0}_{1}'

def docServer = 'doc@doc.faktorzehn.org'
def p2Server = 'hudson@update.faktorzehn.org'

pipeline {
    agent any

    parameters {
        string description: '''Die zu veröffentlichte Version. Sollte nach dem <a href="https://wiki.faktorzehn.at/display/PRODEV/Benennung+von+Versionen">im Wiki</a> beschriebenen Schema aufgebaut sein.
                                <ul>
                                  <li>26.1.0.a20250901-01</li>
                                  <li>26.1.0.m01</li>
                                  <li>26.1.0.rc01</li>
                                  <li>26.1.0.release</li>
                                </ul>''', name: 'RELEASE_VERSION'
        string description: 'Die nächste Version; wird nach einem erfolgreichen Release mit einem eigenen Commit gesetzt. (SNAPSHOT wird automatisch hinzugefügt)', name: 'DEVELOPMENT_VERSION'
        string defaultValue: 'origin/main', description: 'Der zu bauende Branch', name: 'BRANCH'
    }

    tools {
        jdk 'JDK21'
        maven 'maven 3.9'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30'))
        skipDefaultCheckout true
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    currentBuild.displayName = "${JOB_NAME} ${params.RELEASE_VERSION} (${params.BRANCH})"

                    assert params.RELEASE_VERSION ==~ /^(\d+)\.(\d+)\.(\d+)\.(rc\d\d|m\d\d|a\d{8}-\d\d|release)$/
                    assert params.DEVELOPMENT_VERSION ==~ /(\d+\.)+\d+/

                    def scmVars = checkout([
                            $class           : 'GitSCM',
                            branches         : [[name: "${params.BRANCH}"]],
                            extensions       : [[$class: 'WipeWorkspace'], [$class: 'LocalBranch']],
                            userRemoteConfigs: scm.userRemoteConfigs
                    ])

                    LOCAL_BRANCH = scmVars.GIT_LOCAL_BRANCH

                    // parse the version
                    (_, major, minor, patch, kind) = (params.RELEASE_VERSION =~ /^(\d+)\.(\d+)\.(\d+)\.(rc\d\d|m\d\d|a\d{8}-\d\d|release)$/)[0]
                    releaseVersion = params.RELEASE_VERSION

                    def xmlfile = readFile 'pom.xml'
                    oldVersion = MavenProjectVersion.fromPom(xmlfile)
                }
            }
        }

        stage('Set versions') {
            steps {
                withMaven(publisherStrategy: 'EXPLICIT') {
                    sh "mvn -U -V org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false -Dartifacts=base,codequality-config,faktorips-coverage,faktorips-schemas,faktorips-runtime-bom,faktorips-devtools-bom"
                }
                // see https://github.com/eclipse-tycho/tycho/issues/1677
                sh "find devtools/eclipse/targets/ -type f -name 'eclipse-*.target' -exec sed -i 's/${oldVersion}/${releaseVersion}/' {} \\;"
                sh "git add . && git commit -m '[release] prepare release ${params.RELEASE_VERSION}' && git tag -a -m ${params.RELEASE_VERSION} v${params.RELEASE_VERSION}"

                withMaven(publisherStrategy: 'EXPLICIT') {
                    // install codequality-config, as it is used as an extension and setting the versions back won't work if it is missing
                    sh "mvn -V install -f codequality-config"
                    // install targets, as they are resolved by tycho when setting the versions back, which won't work if they are missing
                    sh "mvn -U -V -T 8 -fae -e clean install -DskipTests=true -Dmaven.skip.tests=true -pl :targets -am -Dtycho.localArtifacts=ignore"
                    sh "mvn -V org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${params.DEVELOPMENT_VERSION}-SNAPSHOT -DgenerateBackupPoms=false -Dartifacts=base,codequality-config,faktorips-coverage,faktorips-schemas,faktorips-runtime-bom,faktorips-devtools-bom"
                }
                // see https://github.com/eclipse-tycho/tycho/issues/1677
                sh "find devtools/eclipse/targets/ -type f -name 'eclipse-*.target' -exec sed -i 's/${releaseVersion}/${params.DEVELOPMENT_VERSION}-SNAPSHOT/' {} \\;"
                sh "git add . && git commit -m '[release] prepare for next development iteration'"

                sh "git checkout ${LOCAL_BRANCH}~1"
            }
        }

        stage('Build and Test') {

            environment {
                MAVEN_OPTS = '-Xmx4g'
            }

            steps {
                script {
                    sh 'rm -rf $HOME/.m2/repository/.meta'
                    sh 'rm -rf $HOME/.m2/repository/.cache'
                    sh 'rm -rf $HOME/.m2/repository/p2'
                }
                withMaven(publisherStrategy: 'EXPLICIT') {
                    sh "mvn -U -V -fae -e clean install -f codequality-config"
                    sh "mvn -U -V -T 8 -fae -e clean install -DskipTests=true -Dmaven.skip.tests=true -pl :targets -am -Dtycho.localArtifacts=ignore"
                    sh "mvn -U -V -T 8 -P release clean install site -Dtycho.localArtifacts=ignore"
                    sh "mvn -V -T 8 -fae -e site:stage -f maven"
                }

                junit testResults: "**/target/surefire-reports/*.xml", allowEmptyResults: true
                recordIssues enabledForFailure: true,
                        qualityGates: [[threshold: 1, type: 'NEW', unstable: true]],
                        tools: [java(), javaDoc(), spotBugs(), checkStyle(), eclipse()]
                jacoco sourceInclusionPattern: '**/*.java'
            }

            /*post {
                unsuccessful {
                    script {
                        // Stop even if build is unstable
                        error 'Build failure'
                    }
                }
            }*/
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

        stage('Deployment of Artifacts') {
            steps {

                deployToMavenCentral(
                        commands: ['mvn -V deploy -P release -DskipTests=true -Dmaven.test.skip=true -Dversion.kind=$kind'])

                // deploy p2 repository
                script {
                    def archiveZipFile = MessageFormat.format(archiveZipFileTmpl, releaseVersion)
                    def archiveDeployDir = MessageFormat.format(archiveDeployDirTmpl, major, minor)
                    def ps2DeployDir = MessageFormat.format(ps2DeployDirTmpl, major, minor)
                    // add license to zipped repository (archive download) using zip from the shell 
                    // create zip at root of repository
                    // each sh starts at root of git checkout, so no need to cd back
                    sh """
                        cd ${p2RepositoryFolder}/target/repository
                        cp -v ../../LICENSE.txt ../../agpl-3.0.txt .
                        zip -r ../${archiveZipFile} *
                    """
                    // copy results to server
                    sshagent(credentials: ['hudson.jenkins-f10org'], ignoreMissing: true) {
                        sh """
                            echo "copy to archive download"
                            ssh ${p2Server} 'mkdir -p ${archiveDeployDir}'
                            scp ${p2RepositoryFolder}/target/${archiveZipFile} ${p2Server}:${archiveDeployDir}
                        """
                        sh """
                            echo "copy repository to eclipse update site"
                            ssh ${p2Server} 'mkdir -p ${ps2DeployDir}/${releaseVersion}'
                            scp -r ${p2RepositoryFolder}/target/repository/* ${p2Server}:${ps2DeployDir}/${releaseVersion}
                        """
                        // create a composite, let eclipse see all versions in a sub dir e.g.: In v22_12 there could be 22.12.0-m01, 22.12.0-rc01 & 22.12.0-rfinal
                        // execute local script with stdin of ssh command
                        sh """
                            echo "create update site composite"
                            bash ${p2RepositoryFolder}/scripts/callSSH.sh ${p2Server} ${p2RepositoryFolder}/scripts/buildComposites.sh ${ps2DeployDir} ${ps2DeployDir}/${releaseVersion}
                        """
                        // set latest symlink
                        sh "ssh ${p2Server} \'cd /var/www/update.faktorzehn.org/faktorips; rm latest; ls -1v | grep -E \"^v[0-9_]+\" | tail -1 | xargs -i ln -s {} latest\'"
                    }
                    // deploy maven plugin doc
                    sshagent(credentials: ['docDeployRsaKey'], ignoreMissing: true) {
                        def mavenDocDeployFolder = MessageFormat.format(mavenDocDeployFolderTmpl, major, minor)
                        replaceOnServer server: docServer, port: '2004', localFolder: mavenDocFolder, remoteFolder: mavenDocDeployFolder
                        // set latest symlink
                        sh "ssh doc@doc.faktorzehn.org -p 2004 \'cd /var/www/doc.faktorzehn.org/faktorips-maven-plugin/; rm latest; ls -I \"*[a-zA-Z]*\" -1v | tail -1 | xargs -i ln -s {} latest\'"

                        // deploy xsd schemas
                        def xsdDeployFolder = MessageFormat.format(xsdDeployFolderTmpl, major, minor)
                        replaceOnServer server: docServer, port: '2004', localFolder: xsdFolder, remoteFolder: xsdDeployFolder
                    }
                }
            }
        }

        stage('git push') {
            steps {
                sh "git push origin ${LOCAL_BRANCH} --follow-tags"
            }
        }
    }

    post {
        unsuccessful {
            sendFailureEmail()
        }
    }
}
