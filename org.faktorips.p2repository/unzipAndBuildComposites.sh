#!/bin/bash

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
number=$(ls -tr1d $COMPOSITE_DIR/*/ | wc -l)
contentFile=$COMPOSITE_DIR/compositeContent.xml
artifactFile=$COMPOSITE_DIR/compositeArtifacts.xml
artifactHeader="<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='&quot;IPS 4 FSPM Composite Site&quot;'
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
for dir in $COMPOSITE_DIR/*; do
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
set -x

#First unzip and remove the zip file given via second parameter
BASEDIR=$1
ZIPFILE=$2
if [ -z $BASEDIR ] || [ -z $ZIPFILE ]
then
	echo "Decompress a ziped update site and create composite repositories for every directory up to a given base directory."
	echo "The composite repositories reference every sub directory."
	echo "The script overwrites existing composite repository without asking."
	echo "Usage:"
	echo "First parameter set the base directory for the composite repository"
	echo "Second parameter set the update site zip file to unzip. Zipfile must be located in a subdirectory of the base directory"
	echo "e.g. unzipAndBuildComposite /var/www/update.faktorzehn.org/faktorips /var/www/update.faktorzehn.org/faktorips/v3_7/3.7.0-x/v3_7-3.7.0.x.zip"
	exit 1
fi
ZIPDIR=$(dirname $ZIPFILE)
unzip -d $ZIPDIR $ZIPFILE
#rm $ZIPFILE

#second create composites for every directory until first parameter
COMPOSITE_DIR=$ZIPDIR
until [ $COMPOSITE_DIR = $BASEDIR ] || [ $COMPOSITE_DIR = "/" ]
do
	COMPOSITE_DIR=$(dirname $COMPOSITE_DIR)
	createComposite
done


