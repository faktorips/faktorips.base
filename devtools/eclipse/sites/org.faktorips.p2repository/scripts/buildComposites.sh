#!/bin/bash
# creates a composite repository
# $1: the name of the highest directory where the composite should start, e.g. /var/update/faktorips/v3_12/3.12.1-rfinal
# $2: the name of the lowest directory where the composite should end, e.g. /var/update/faktorips/

IGNORE="downloads"

createComposite()
{
#Creates a composite p2 site from all subdirectories of the working directory and deletes old snapshots from the plugins and features subdirectories of those subdirectories
contentHeader="<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='Faktor-IPS'
    type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
  <properties size='1'>
    <property name='p2.timestamp' value='%s'/>
  </properties>
  <children size='%d'>"
child="
    <child location='%s'/>"
timestamp=$(date +"%s")
number=$(ls -tr1d $COMPOSITE_DIR/*/ | grep -v $IGNORE | wc -l)
contentFile=$COMPOSITE_DIR/compositeContent.xml
artifactFile=$COMPOSITE_DIR/compositeArtifacts.xml
artifactHeader="<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='Faktor-IPS'
    type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>
  <properties size='1'>
    <property name='p2.timestamp' value='%s'/>
  </properties>
  <children size='%d'>"
footer="
  </children>
</repository>"
printf "$contentHeader" "$timestamp" "$number" > $contentFile
printf "$artifactHeader" "$timestamp" "$number" > $artifactFile
SUBDIRS=$(ls -tr1d $COMPOSITE_DIR/* | grep -v $IGNORE)
for dir in $SUBDIRS; do
    if test -d "$dir"; then
	REL_DIR=./${dir##/*/}
        printf "$child" "$REL_DIR" >> $contentFile
        printf "$child" "$REL_DIR" >> $artifactFile
    fi
done
printf "$footer" >> $contentFile
printf "$footer" >> $artifactFile
}

set -e

BASEDIR=`readlink -f $1`
REPOSITORYDIR=`readlink -f $2`
COMPOSITE_DIR=`readlink -f $REPOSITORYDIR`
until [ $COMPOSITE_DIR = $BASEDIR ] || [ $COMPOSITE_DIR = "/" ]
do
	COMPOSITE_DIR=`dirname $COMPOSITE_DIR`
	createComposite
done


