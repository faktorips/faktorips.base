@Library('f10-jenkins-library@1.1_patches')
@Library('maven-central-release-library@main')
@Library('fips-jenkins-library@main')
@Library('release-library@1.2')

def p2RepositoryFolder = './devtools/eclipse/sites/org.faktorips.p2repository'
def p2Server = 'hudson@update.faktorzehn.org'
def(major, minor, patch, kind, isAlpha, isRelease, releasePattern) = parseVersion()

def setVersionForPlatformBoms(String newVersion) {
    sh """
        for boms in \$(find devtools/common/bom/ -type d -name "*-platform-dependencies");
        do
            mvn -f \$boms/pom.xml versions:update-parent -DparentVersion=${newVersion} -DgenerateBackupPoms=false -DallowSnapshots=true -DskipResolution=true;
        done
    """
}

def configureRelease() {
    withMaven(publisherStrategy: 'EXPLICIT') {
        oldVersion = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
        sh "mvn -U -V org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${params.RELEASE_VERSION} -DgenerateBackupPoms=false -Dartifacts=base,codequality-config,faktorips-coverage,faktorips-schemas,faktorips-runtime-bom,faktorips-devtools-bom"
        // see https://github.com/eclipse-tycho/tycho/issues/1677
        sh "find devtools/eclipse/targets/ -type f -name 'eclipse-*.target' -exec sed -i 's/${oldVersion}/${params.RELEASE_VERSION}/' {} \\;"
        // install codequality-config, as it is used as an extension and setting the versions back won't work if it is missing
        // must be installed before enforcer plugin is executed
        sh "mvn -U -V -fae -e clean install -f codequality-config"
        setVersionForPlatformBoms(params.RELEASE_VERSION)
    }
}

def configureDevelopment() {
    // in that case (e.g. an alpha), no deliberate version must be set. We simply revert the release commit to the previous version
    if (params.DEVELOPMENT_VERSION) {
        withMaven(publisherStrategy: 'EXPLICIT') {
            // install targets, as they are resolved by tycho when setting the versions back, which won't work if they are missing
            sh "mvn -U -V -T 8 -fae -e clean install -DskipTests=true -Dmaven.skip.tests=true -pl :targets -am -Dtycho.localArtifacts=ignore"
            sh "mvn -V org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${params.DEVELOPMENT_VERSION}-SNAPSHOT -DgenerateBackupPoms=false -Dartifacts=base,codequality-config,faktorips-coverage,faktorips-schemas,faktorips-runtime-bom,faktorips-devtools-bom"
            sh "mvn -U -V -fae -e clean install -f codequality-config"
        }
        // see https://github.com/eclipse-tycho/tycho/issues/1677
        sh "find devtools/eclipse/targets/ -type f -name 'eclipse-*.target' -exec sed -i 's/${params.RELEASE_VERSION}/${params.DEVELOPMENT_VERSION}-SNAPSHOT/' {} \\;"
        setVersionForPlatformBoms("${params.DEVELOPMENT_VERSION}-SNAPSHOT")
    }
}

def parseVersion() {
    def releasePattern = /^(?<major>\d+)\.(?<minor>\d+)\.(\d+)\.(rc\d\d|m\d\d|a\d{8}-\d\d|release)$/
    def (_, major, minor, patch, kind) = (params.RELEASE_VERSION =~ releasePattern)[0]
    def isAlpha = kind =~ /a\d{8}-\d\d$/
    def isRelease = kind == 'release'
    return [major, minor, patch, kind, isAlpha.find(), isRelease, releasePattern]
}

pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'maven 3.9'
    }

    environment {
        REFERENCE_JOB = 'FaktorIPS_hotfix_24.1'
        MAVEN_OPTS = '-Xmx4g'
        DISPLAY = ':0'
    }

    options {
        skipDefaultCheckout true
    }

    stages {
        stage('Prepare release') {
            steps {
                script {
                    prepareRelease {
                        rootModule {
                            versionSetters(this.&configureRelease, this.&configureDevelopment)
                        }
                    }
                }
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    sh 'rm -rf $HOME/.m2/repository/.meta'
                    sh 'rm -rf $HOME/.m2/repository/.cache'
                    sh 'rm -rf $HOME/.m2/repository/p2'

                    // the build runs on JDK 21 for Tycho 5, while the bundles keep BREE JavaSE-17
                    // and tycho-surefire resolves the test JVM from this toolchain;
                    // JDK11/JDK8 are needed for runtime modules whose java.version is still 11/1.8
                    createToolchain jdk: ['AdoptiumJDK17', 'JDK11', 'JDK8'], outputFile: 'toolchains.xml'
                    withMaven(publisherStrategy: 'EXPLICIT') {
                        sh "mvn -U -V -T 8 -fae -e clean install -DskipTests=true -Dmaven.skip.tests=true -pl :targets -am -Dtycho.localArtifacts=ignore"
                        if (isAlpha) {
                            sh "mvn -U -V -T 8 clean install site checkstyle:checkstyle -Dtycho.localArtifacts=ignore -t toolchains.xml"
                        } else {
                            // gpg signing of artifacts for maven central
                            sh "mvn -U -V -T 8 -P mavenCentralRelease clean install site checkstyle:checkstyle -Dtycho.localArtifacts=ignore -t toolchains.xml"
                        }
                        sh "mvn -V -T 8 -fae -e site:stage -f maven"
                    }

                    discoverReferenceBuild referenceJob: "${REFERENCE_JOB}", requiredResult: hudson.model.Result.SUCCESS

                    junit testResults: "**/target/surefire-reports/*.xml", allowEmptyResults: true
                    recordIssues enabledForFailure: true,
                            qualityGates: [[threshold: 1, type: 'NEW', unstable: true]],
                            tools: [java(), javaDoc(), spotBugs(), checkStyle(), eclipse()]
                    jacoco sourceInclusionPattern: '**/*.java'
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

        stage('Deployment of Artifacts') {
            steps {
                // deploy p2 repository
                script {
                    uploadRelease() {
                        if (isAlpha) {
                            withMaven(publisherStrategy: 'EXPLICIT') {
                                sh "mvn -V deploy -DskipTests=true -Dmaven.test.skip=true-t toolchains.xml -Dversion.kind=$kind"
                            }
                        } else {
                            deployToMavenCentral(
                                    commands: ['mvn -V deploy -P mavenCentralRelease -DskipTests=true -Dmaven.test.skip=true -t toolchains.xml -Dversion.kind=$kind']
                            )
                        }
                        def archiveZipFile = "org.faktorips.p2repository-${params.RELEASE_VERSION}.zip"
                        def archiveDeployDir = "/var/www/update.faktorzehn.org/faktorips/v${major}_${minor}/downloads/faktorips-${major}.${minor}"
                        def ps2DeployDir = "/var/www/update.faktorzehn.org/faktorips/v${major}_${minor}"
                        // add license to zipped repository (archive download) using zip from the shell
                        // create zip at root of repository
                        // each sh starts at root of git checkout, so no need to cd back
                        sh """
                            cd ${p2RepositoryFolder}/target/repository
                            cp -v ../../LICENSE.txt ../../agpl-3.0.txt .
                            zip -r ../${archiveZipFile} *
                        """
                        // copy results to server
                        sh """
                            echo "copy to archive download"
                            ssh ${p2Server} 'mkdir -p ${archiveDeployDir}'
                            scp ${p2RepositoryFolder}/target/${archiveZipFile} ${p2Server}:${archiveDeployDir}
                        """
                        sh """
                            echo "copy repository to eclipse update site"
                            ssh ${p2Server} 'mkdir -p ${ps2DeployDir}/${params.RELEASE_VERSION}'
                            scp -r ${p2RepositoryFolder}/target/repository/* ${p2Server}:${ps2DeployDir}/${params.RELEASE_VERSION}
                        """
                        // update the minor composite (v27_1 etc.) so Eclipse users see all patch releases within the minor line.
                        // the root composite is static (points only to 'latest') and is never touched by the build —
                        // including historical minor lines there caused Eclipse to offer Luna-compatible bundles to current users.

                        // Workaround for Tycho 5.0.3 cold-start bug: modify-composite-repository re-throws
                        // ProvisionException(code=1000) instead of creating a fresh composite when the remote
                        // URL returns 404 (first release of a new minor line). Pre-create empty composite files
                        // on the server so Tycho can load and then modify them.
                        def compositeExists = sh(
                            script: "ssh ${p2Server} '[ -f ${ps2DeployDir}/compositeContent.xml ]'",
                            returnStatus: true
                        ) == 0
                        if (!compositeExists) {
                            sh "mkdir -p ${p2RepositoryFolder}/target/composite-init"
                            writeFile file: "${p2RepositoryFolder}/target/composite-init/compositeContent.xml", text: """\
<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='Faktor-IPS ${major}.${minor} Updates' type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
  <properties size='1'><property name='p2.timestamp' value='0'/></properties>
  <children size='0'/>
</repository>"""
                            writeFile file: "${p2RepositoryFolder}/target/composite-init/compositeArtifacts.xml", text: """\
<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='Faktor-IPS ${major}.${minor} Updates' type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>
  <properties size='1'><property name='p2.timestamp' value='0'/></properties>
  <children size='0'/>
</repository>"""
                            sh "scp ${p2RepositoryFolder}/target/composite-init/composite*.xml ${p2Server}:${ps2DeployDir}/"
                        }

                        sh "mvn -P update-composite -pl :faktorips-devtools-eclipse-sites -Dmajor=${major} -Dminor=${minor} -Drelease.version=${params.RELEASE_VERSION} --no-transfer-progress generate-resources"
                        sh "scp ${p2RepositoryFolder}/target/composite-out/v${major}_${minor}/composite*.xml ${p2RepositoryFolder}/target/composite-out/v${major}_${minor}/p2.index ${p2Server}:${ps2DeployDir}/"
                        def isMainBranch = env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main'
                        def isUpdateLatest = isMainBranch && isRelease
                        if(isUpdateLatest) {
                            // set latest symlink
                            sh "ssh ${p2Server} \'cd /var/www/update.faktorzehn.org/faktorips; rm latest; ls -1v | grep -E \"^v[0-9_]+\" | tail -1 | xargs -i ln -s {} latest\'"
                        }
                        // deploy maven plugin doc
                        withMaven(publisherStrategy: 'EXPLICIT') {
                            uploadDocumentation project: 'faktorips-maven-plugin', folder: 'maven/faktorips-maven-plugin', updateLatest: isUpdateLatest, legacyMode: true, legacyUser: 'jenkins-fips-legacy' // SNAPSHOT versions should also publish to major.minor
                            uploadDocumentation project: 'schema/faktor-ips', folder: 'devtools/common/faktorips-schemas/src/main/resources', updateLatest: false, releasePattern: releasePattern, legacyMode: true, legacyUser: 'jenkins-fips-legacy'
                        }
                    }
                }
            }
        }
    }

    post {
        unsuccessful {
            sendFailureEmail()
        }
    }
}
