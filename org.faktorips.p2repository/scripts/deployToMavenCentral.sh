#!/bin/bash
#####################################################################
# Script for uploading the faktorips artifacts to maven central repository
# parameters:
# 1 : current version to deploy
# 2 : marker for deploy a release (for different repository and version)
#     milestone and release candidate = false 
#     final release = true
# 3 : directory with the jar files/poms/javadocs
#####################################################################
 
set -e
VERSION=$1
IS_RELEASE=$2
JAR_DIRECTORY=$3

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

echo "##########################"
echo "deployToMavenCentral"
echo "##########################"
echo REPO_ID:       $REPO_ID
echo REPO_URL:      $REPO_URL
echo VERSION:       $VERSION
echo IS_RELEASE:    $IS_RELEASE
echo JAR_DIR:       $JAR_DIRECTORY
echo "********"

RUNTIME_ARTIFACT_ID=org.faktorips.runtime
RUNTIME_LIB=$RUNTIME_ARTIFACT_ID.java5
RUNTIME_LIB_JAR=`ls $JAR_DIRECTORY/${RUNTIME_LIB}_*.jar`
RUNTIME_LIB_SOURCE_JAR=`ls $JAR_DIRECTORY/$RUNTIME_LIB.source_*.jar`
RUNTIME_LIB_JAVADOC_JAR=$JAR_DIRECTORY/$RUNTIME_LIB.javadoc.jar
#RUNTIME_LIB_JAVADOC_JAR=`ls $JAR_DIRECTORY/$RUNTIME_LIB.javadoc_*.jar`
RUNTIME_LIB_POM=$JAR_DIRECTORY/runtime-pom.xml

VALUETYPES_ARTIFACT_ID=org.faktorips.valuetypes
VALUETYPES_LIB=$VALUETYPES_ARTIFACT_ID.java5
VALUETYPES_LIB_JAR=`ls $JAR_DIRECTORY/${VALUETYPES_LIB}_*.jar`
VALUETYPES_LIB_SOURCE_JAR=`ls $JAR_DIRECTORY/$VALUETYPES_LIB.source_*.jar`
VALUETYPES_LIB_JAVADOC_JAR=$JAR_DIRECTORY/$VALUETYPES_LIB.javadoc.jar
#VALUETYPES_LIB_JAVADOC_JAR=`ls $JAR_DIRECTORY/$VALUETYPES_LIB.javadoc_*.jar`
VALUETYPES_LIB_POM=$JAR_DIRECTORY/valuetypes-pom.xml

VALUETYPES_JODA_ARTIFACT_ID=org.faktorips.valuetypes.joda
VALUETYPES_JODA_LIB=$VALUETYPES_JODA_ARTIFACT_ID
VALUETYPES_JODA_LIB_JAR=`ls $JAR_DIRECTORY/${VALUETYPES_JODA_LIB}_*.jar`
VALUETYPES_JODA_LIB_SOURCE_JAR=`ls $JAR_DIRECTORY/$VALUETYPES_JODA_LIB.source_*.jar`
VALUETYPES_JODA_LIB_JAVADOC_JAR=$JAR_DIRECTORY/$VALUETYPES_JODA_LIB.javadoc.jar
#VALUETYPES_JODA_LIB_JAVADOC_JAR=`ls $JAR_DIRECTORY/$VALUETYPES_JODA_LIB.javadoc_*.jar`
VALUETYPES_JODA_LIB_POM=$JAR_DIRECTORY/valuetypes.joda-pom.xml


echo "INFO $RUNTIME_ARTIFACT_ID"
echo $RUNTIME_LIB_JAR
echo $RUNTIME_LIB_SOURCE_JAR
echo $RUNTIME_LIB_JAVADOC_JAR
echo $RUNTIME_LIB_POM
echo "********"
echo "INFO $VALUETYPES_ARTIFACT_ID"
echo $VALUETYPES_LIB_JAR
echo $VALUETYPES_LIB_SOURCE_JAR
echo $VALUETYPES_LIB_JAVADOC_JAR
echo $VALUETYPES_LIB_POM
echo "********"
echo "INFO $VALUETYPES_JODA_ARTIFACT_ID"
echo $VALUETYPES_JODA_LIB_JAR
echo $VALUETYPES_JODA_LIB_SOURCE_JAR
echo $VALUETYPES_JODA_LIB_JAVADOC_JAR
echo $VALUETYPES_JODA_LIB_POM
echo "########################"

echo "upload $RUNTIME_ARTIFACT_ID"
mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$RUNTIME_LIB_JAR -Dsources=$RUNTIME_LIB_SOURCE_JAR -Djavadoc=$RUNTIME_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$RUNTIME_LIB_POM
echo "upload $VALUETYPES_ARTIFACT_ID"
mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$VALUETYPES_LIB_JAR -Dsources=$VALUETYPES_LIB_SOURCE_JAR -Djavadoc=$VALUETYPES_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$VALUETYPES_LIB_POM
echo "upload $VALUETYPES_JODA_ARTIFACT_ID"
mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$VALUETYPES_JODA_LIB_JAR -Dsources=$VALUETYPES_JODA_LIB_SOURCE_JAR -Djavadoc=$VALUETYPES_JODA_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$VALUETYPES_JODA_LIB_POM

