#!/bin/bash
#####################################################################
# Script for uploading the faktorips artifacts to maven central repository
# parameters:
# 1 : current version to deploy
# 2 : marker for deploy a release (for different repository and version)
#     milestone and release candidate = false 
#     final release = true
# 3 : base directory of the plugins
#####################################################################
 
set -e
VERSION=$1
IS_RELEASE=$2
BASE_DIRECTORY=$3

if [ "$IS_RELEASE" = "true" ] 
then
	REPO_ID=ossrh    
	REPO_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2
else
	REPO_ID=snapshots
	REPO_URL=https://nexus.faktorzehn.de/content/repositories/snapshots/
	#REPO_ID=ossrh    
	#REPO_URL=https://oss.sonatype.org/content/repositories/snapshots/
fi

if [[ $VERSION == *-SNAPSHOT ]]
then
	VERSION_SNAPSHOT=${VERSION}
else
	VERSION_SNAPSHOT=${VERSION}"-SNAPSHOT"
fi

echo "##########################"
echo "deployToMavenCentral"
echo "##########################"
echo REPO_ID:       $REPO_ID
echo REPO_URL:      $REPO_URL
echo VERSION:       $VERSION
echo IS_RELEASE:    $IS_RELEASE
echo BASE_DIR:      $BASE_DIRECTORY
echo "********"

TARGET_DIR="target"
POM_DIRECTORY=$BASE_DIRECTORY/"org.faktorips.p2repository"/$TARGET_DIR/scripts/nexus-poms

RUNTIME_LIB=org.faktorips.runtime.java5
RUNTIME_TARGET_DIRECTORY=$BASE_DIRECTORY/$RUNTIME_LIB/$TARGET_DIR
RUNTIME_LIB_PREFIX=$RUNTIME_TARGET_DIRECTORY/${RUNTIME_LIB}-${VERSION_SNAPSHOT}
RUNTIME_LIB_JAR=$RUNTIME_LIB_PREFIX.jar
RUNTIME_LIB_SOURCE_JAR=$RUNTIME_LIB_PREFIX-sources.jar
RUNTIME_LIB_JAVADOC_JAR=$RUNTIME_LIB_PREFIX-javadoc.jar
RUNTIME_LIB_POM=$POM_DIRECTORY/runtime-pom.xml

RUNTIME_CLIENT_LIB=faktorips-runtime-client
RUNTIME_CLIENT_TARGET_DIRECTORY=$BASE_DIRECTORY/$RUNTIME_LIB/client/$TARGET_DIR
RUNTIME_CLIENT_LIB_PREFIX=$RUNTIME_CLIENT_TARGET_DIRECTORY/${RUNTIME_CLIENT_LIB}-${VERSION_SNAPSHOT}
RUNTIME_CLIENT_LIB_JAR=$RUNTIME_CLIENT_LIB_PREFIX.jar
RUNTIME_CLIENT_LIB_SOURCE_JAR=$RUNTIME_CLIENT_LIB_PREFIX-sources.jar
RUNTIME_CLIENT_LIB_JAVADOC_JAR=$RUNTIME_CLIENT_LIB_PREFIX-javadoc.jar
RUNTIME_CLIENT_LIB_POM=$POM_DIRECTORY/runtime-client-pom.xml

VALUETYPES_LIB=org.faktorips.valuetypes.java5
VALUETYPES_TARGET_DIRECTORY=$BASE_DIRECTORY/$VALUETYPES_LIB/$TARGET_DIR
VALUETYPES_LIB_PREFIX=$VALUETYPES_TARGET_DIRECTORY/${VALUETYPES_LIB}-${VERSION_SNAPSHOT}
VALUETYPES_LIB_JAR=$VALUETYPES_LIB_PREFIX.jar
VALUETYPES_LIB_SOURCE_JAR=$VALUETYPES_LIB_PREFIX-sources.jar
VALUETYPES_LIB_JAVADOC_JAR=$VALUETYPES_LIB_PREFIX-javadoc.jar
VALUETYPES_LIB_POM=$POM_DIRECTORY/valuetypes-pom.xml

VALUETYPES_JODA_LIB=org.faktorips.valuetypes.joda
VALUETYPES_JODA_TARGET_DIRECTORY=$BASE_DIRECTORY/$VALUETYPES_JODA_LIB/$TARGET_DIR
VALUETYPES_JODA_LIB_PREFIX=$VALUETYPES_JODA_TARGET_DIRECTORY/${VALUETYPES_JODA_LIB}-${VERSION_SNAPSHOT}
VALUETYPES_JODA_LIB_JAR=$VALUETYPES_JODA_LIB_PREFIX.jar
VALUETYPES_JODA_LIB_SOURCE_JAR=$VALUETYPES_JODA_LIB_PREFIX-sources.jar
VALUETYPES_JODA_LIB_JAVADOC_JAR=$VALUETYPES_JODA_LIB_PREFIX-javadoc.jar
VALUETYPES_JODA_LIB_POM=$POM_DIRECTORY/valuetypes.joda-pom.xml


echo "INFO Runtime"
echo $RUNTIME_LIB_JAR
echo $RUNTIME_LIB_SOURCE_JAR
echo $RUNTIME_LIB_JAVADOC_JAR
echo $RUNTIME_LIB_POM
echo "********"
echo "INFO Valuetypes"
echo $VALUETYPES_LIB_JAR
echo $VALUETYPES_LIB_SOURCE_JAR
echo $VALUETYPES_LIB_JAVADOC_JAR
echo $VALUETYPES_LIB_POM
echo "********"
echo "INFO Valuetypes Joda"
echo $VALUETYPES_JODA_LIB_JAR
echo $VALUETYPES_JODA_LIB_SOURCE_JAR
echo $VALUETYPES_JODA_LIB_JAVADOC_JAR
echo $VALUETYPES_JODA_LIB_POM
echo "########################"

echo "upload Runtime"
mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$RUNTIME_LIB_JAR -Dsources=$RUNTIME_LIB_SOURCE_JAR -Djavadoc=$RUNTIME_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$RUNTIME_LIB_POM
echo "upload Runtime Client"
mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$RUNTIME_CLIENT_LIB_JAR -Dsources=$RUNTIME_CLIENT_LIB_SOURCE_JAR -Djavadoc=$RUNTIME_CLIENT_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$RUNTIME_CLIENT_LIB_POM
echo "upload Valuetypes"
mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$VALUETYPES_LIB_JAR -Dsources=$VALUETYPES_LIB_SOURCE_JAR -Djavadoc=$VALUETYPES_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$VALUETYPES_LIB_POM
echo "upload Valuetypes Joda"
mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$VALUETYPES_JODA_LIB_JAR -Dsources=$VALUETYPES_JODA_LIB_SOURCE_JAR -Djavadoc=$VALUETYPES_JODA_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$VALUETYPES_JODA_LIB_POM

