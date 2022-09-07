#!/bin/bash
######################################################################################################################
# For this script to work you need an ssh key on the doc.faktorzehn.org server.                                      #
# The sript needs no parameters and expects only one version in the schema pom.xml.                                  #
# If dependencies are added they either should be versioned in a bom file, or the version should be read with        #
# an xpath tool e.g.: xmlstarlet sel -N x=http://maven.apache.org/POM/4.0.0 -t -m /x:project/x:version -v . pom.xml  #
######################################################################################################################

command -v ssh -p 2004 doc@doc.faktorzehn.org ls >/dev/null 2>&1 || { echo >&2 "Connection to doc.faktorzehn.org failed, check your connection or ssh key.  Aborting."; exit 1; }

DEPLOY_DIR='/var/www/doc.faktorzehn.org/schema/faktor-ips'
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "Parsing pom.xml ..."

SCHEMA_VERSION=$( cat $SCRIPT_DIR/../pom.xml | grep '<version>' | grep -oP '(?<=>)[0-9]+\.[0-9]+')

echo "According to the pom.xml the schema version is $SCHEMA_VERSION ..."

scp -P 2004 -r "$SCRIPT_DIR/../src/main/resources" doc@doc.faktorzehn.org:"$DEPLOY_DIR/$SCHEMA_VERSION.tmp" || { echo >&2 "Failed to scp files to server doc.faktorzehn.org"; exit 1; }

ssh -p 2004 doc@doc.faktorzehn.org "mkdir -p $DEPLOY_DIR/$SCHEMA_VERSION && mv $DEPLOY_DIR/$SCHEMA_VERSION $DEPLOY_DIR/$SCHEMA_VERSION.old && mv $DEPLOY_DIR/$SCHEMA_VERSION.tmp $DEPLOY_DIR/$SCHEMA_VERSION && rm -rf $DEPLOY_DIR/$SCHEMA_VERSION.old" || { echo >&2 "Failed to replace the remote folder on the server doc.faktorzehn.org. Manual intervention required, check folder $SCHEMA_VERSION, $SCHEMA_VERSION.old & $SCHEMA_VERSION.tmp in $DEPLOY_DIR"; exit 1; }

echo "Finished deploying to doc.faktorzehn.org:$DEPLOY_DIR/$SCHEMA_VERSION"
echo "Check https://doc.faktorzehn.org/schema/faktor-ips/$SCHEMA_VERSION/ for the xsd-schemas"
