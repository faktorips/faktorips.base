#!/bin/bash
set -e
REPO_ID=snapshots
REPO_URL=https://nexus.faktorzehn.de/content/repositories/snapshots/
#REPO_ID=ossrh
#REPO_URL=https://oss.sonatype.org/content/repositories/snapshots/
#REPO_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2
VERSION=$1
JAR_DIRECTORY=$2
DIR=$( cd "$( dirname "$0" )" && pwd )

RUNTIME_ARTIFACT_ID=org.faktorips.runtime
RUNTIME_LIB=$RUNTIME_ARTIFACT_ID.java5
RUNTIME_LIB_JAR=`ls $JAR_DIRECTORY/${RUNTIME_LIB}_*.jar`
RUNTIME_LIB_SOURCE_JAR=`ls $JAR_DIRECTORY/$RUNTIME_LIB.source_*.jar`
RUNTIME_LIB_JAVADOC_JAR=$DIR/fake-java-doc/$RUNTIME_LIB.javadoc.jar
#RUNTIME_LIB_JAVADOC_JAR=`ls $JAR_DIRECTORY/$RUNTIME_LIB.javadoc_*.jar`
RUNTIME_LIB_POM=$DIR/nexus-poms/runtime-pom.xml

VALUETYPES_ARTIFACT_ID=org.faktorips.valuetypes
VALUETYPES_LIB=$VALUETYPES_ARTIFACT_ID.java5
VALUETYPES_LIB_JAR=`ls $JAR_DIRECTORY/${VALUETYPES_LIB}_*.jar`
VALUETYPES_LIB_SOURCE_JAR=`ls $JAR_DIRECTORY/$VALUETYPES_LIB.source_*.jar`
VALUETYPES_LIB_JAVADOC_JAR=$DIR/fake-java-doc/$VALUETYPES_LIB.javadoc.jar
#VALUETYPES_LIB_JAVADOC_JAR=`ls $JAR_DIRECTORY/$VALUETYPES_LIB.javadoc_*.jar`
VALUETYPES_LIB_POM=$DIR/nexus-poms/valuetypes-pom.xml

echo "##########################"
echo REPO_ID    $REPO_ID
echo REPO_URL   $REPO_URL
echo VERSION    $VERSION
echo DIR        $DIR
echo "********"
echo $RUNTIME_ARTIFACT_ID
echo $RUNTIME_LIB_JAR
echo $RUNTIME_LIB_SOURCE_JAR
echo $RUNTIME_LIB_JAVADOC_JAR
echo $RUNTIME_LIB_POM
echo "********"
echo $VALUETYPES_ARTIFACT_ID
echo $VALUETYPES_LIB_JAR
echo $VALUETYPES_LIB_SOURCE_JAR
echo $VALUETYPES_LIB_JAVADOC_JAR
echo $VALUETYPES_LIB_POM
echo "########################"

mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$RUNTIME_LIB_JAR -Dsources=$RUNTIME_LIB_SOURCE_JAR -Djavadoc=$RUNTIME_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$RUNTIME_LIB_POM
mvn gpg:sign-and-deploy-file -Durl=$REPO_URL -DrepositoryId=$REPO_ID -Dfile=$VALUETYPES_LIB_JAR -Dsources=$VALUETYPES_LIB_SOURCE_JAR -Djavadoc=$VALUETYPES_LIB_JAVADOC_JAR -Dversion=$VERSION -DpomFile=$VALUETYPES_LIB_POM

