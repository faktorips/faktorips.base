library 'f10-jenkins-library@1.1_patches'

def p2Server = 'hudson@update.faktorzehn.org'
def ps2MirrorDir = '/var/www/update.faktorzehn.org/p2repositories'
def pomString = """<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.faktorips.p2mirror</groupId>
    <artifactId>org.faktorips.p2mirror.${eclipseVersion}</artifactId>
    <version>4.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <source.encoding>UTF-8</source.encoding>
        <tycho-version>4.0.8</tycho-version>
        <eclipse-version>${eclipseVersion}</eclipse-version>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.eclipse.tycho.extras</groupId>
                <artifactId>tycho-p2-extras-plugin</artifactId>
                <version>\${tycho-version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>mirror</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <source>
                    <!-- source repositories to mirror from -->
                        <repository>
                            <url>https://download.eclipse.org/releases/\${eclipse-version}/</url>
                            <layout>p2</layout>
                        </repository>
                    </source>
                    <destination>\${mirror.destination}</destination>
                    <followStrictOnly>false</followStrictOnly>
                    <includeOptional>true</includeOptional>
                    <includeNonGreedy>true</includeNonGreedy>
                    <latestVersionOnly>false</latestVersionOnly>
                    <mirrorMetadataOnly>false</mirrorMetadataOnly>
                    <compress>true</compress>
                    <append>true</append>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
"""

pipeline {
    agent any

    parameters {
        string description: 'Eclipse Version z.B: 2024-09', name: 'eclipseVersion'
    }

    tools {
        jdk 'JDK21'
        maven 'maven 3.9'
    }

    options {
        skipDefaultCheckout true
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Create Mirror') {
            steps {
                script {
                    sh "mkdir -p /tmp/mirror/${eclipseVersion}"
                    writeFile encoding: 'UTF-8', file: "/tmp/mirror/${eclipseVersion}/mirror.pom.xml", text: "${pomString}"
                    withMaven(publisherStrategy: 'EXPLICIT') {
                        sh "mvn -U -V tycho-p2-extras:mirror -f /tmp/mirror/${eclipseVersion}/mirror.pom.xml -Dmirror.destination=/tmp/mirror/${eclipseVersion}"
                    }
                }
            }
        }

        stage('Copy Mirror') {
            steps {
                script {
                    replaceOnServer server:p2Server, port:'22', localFolder:"/tmp/mirror/${eclipseVersion}", remoteFolder:"${ps2MirrorDir}/${eclipseVersion}"
                }
            }
        }
    }

    post {
        always {
            script {
                echo "Cleanup /tmp/mirror/${eclipseVersion}"
                sh "rm -rf /tmp/mirror/${eclipseVersion}"
            }
        }
        unsuccessful {
            sendFailureEmail()
        }
    }
}

// upload a local folder to a tmp folder on the server
// mv existing remoteFolder before swaping it with the tmp one
// remove the old folder
def replaceOnServer(def server, def port, def localFolder, def remoteFolder) {
    sh "echo 'save replace files on server'"
    sh "scp -P ${port} -r ${localFolder} ${server}:${remoteFolder}_deploy"
    // a non existing folder would stop the script therefore make it or ignore it (-p)
    sh "ssh -p ${port} ${server} 'mkdir -p ${remoteFolder} && mv ${remoteFolder} ${remoteFolder}_old && mv ${remoteFolder}_deploy ${remoteFolder} && rm -rf ${remoteFolder}_old'"
}
