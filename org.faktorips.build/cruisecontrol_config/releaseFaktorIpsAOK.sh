#!/bin/bash

SCRIPTNAME=$(basename $0)
LOGFILE=${SCRIPTNAME}_$$.log
STORE_LOG=false

trap error ERR

handleError()
{
  if [ ! $? = 0 ] ; then
    error
  fi
}

error()
{
  echo "there was an error see log $LOGFILE" 
  exit 1
}

showUsageAndExit()
{
  echo 'Faktor IPS Release Build Script for AOK Builds'
  echo 'usage:  '
  echo '  '$0 '[script options]'
  echo '  script options:'
  echo '    -help, -h              print this message'
  echo '  mandatory:'
  echo '    -version [version]     the version which should be generated (e.g. "-version 2.2.0.rc1")'
  echo '  optional:'
  echo '    -storeLog              do not delete logfile after successful build'
  exit 1;
}

parseArgs()
{ 
  while [ $# != 0 ]
  do case "$1" in
    -version)       BUILD_VERSION=$2 ; shift ;;
    -storeLog)      STORE_LOG=true;;
    -h)             showUsageAndExit;;
    --help)         showUsageAndExit;;
    -*)             echo wrong parameter: $1 ; showUsageAndExit;;
    esac
    shift
  done
  
  if [ -z "$BUILD_VERSION" ] ; then
    echo '--> Error no version given!'
    echo '  '
    showUsageAndExit
  fi

  MAJOR_VERSION=`echo $BUILD_VERSION | awk -F. '{print $1}'`
  MINOR_VERSION=`echo $BUILD_VERSION | awk -F. '{print $2}'`
  DOWNLOAD_PATH=/var/www/update.faktorzehn.org/faktorips/aok/downloads/faktorips-$MAJOR_VERSION.$MINOR_VERSION
}

showParameter()
{  
  echo 
  echo Release build parameter:
  echo "  --------------------------------------------------------------------------------------"
  echo -e "  Release Version=\e[35m$BUILD_VERSION\e[0m"
  echo -e "  Logfile=\e[35m$LOGFILE\e[0m"
  echo -e "  Store Logfile (even if build successful)=\e[35m$STORE_LOG\e[0m"
  echo -e "  Downloadpath=\e[35m$DOWNLOAD_PATH\e[0m"
  echo -e "=> Start release build (\e[33my\e[0m)es? <="
  echo -e "   (make sure you've tagged all necessary projects!)"
  echo 
  read ANSWER
  if [ ! "$ANSWER" = "y" ]
    then echo "Cancel"; exit 1
  fi
}

doReleaseBuild()
{
  local UPDATE_SITE_ZIP_NAME=faktorips-updateSite-$BUILD_VERSION.zip
  local UPDATE_SITE_RESULT_PATH=tmp_fips_release/pluginbuilder_release_tmp/org.faktorips.pluginbuilder_release/results/updateSite-$BUILD_VERSION.zip
  touch $LOGFILE
  echo build default updatesite ...
  /opt/cc/work/releaseFaktorIps.sh -version $BUILD_VERSION -skipTest -forceBuild -skipTaggingCvs -customBuild aok -skipPublish >> $LOGFILE 2>&1 << EOF
y
EOF
  handleError
  echo copy result to $DOWNLOAD_PATH ...
  ##cp $UPDATE_SITE_RESULT_PATH $DOWNLOAD_PATH/$UPDATE_SITE_ZIP_NAME
  handleError
  echo
 
  echo build product ...
  /opt/cc/work/releaseFaktorIps.sh -version $BUILD_VERSION -skipTest -forceBuild -skipTaggingCvs -buildProduct de.aoksystems.omc.faktorips.core -customBuild aok -skipPublish >> $LOGFILE 2>&1 << EOF
y
EOF
  handleError
  echo copy result to $DOWNLOAD_PATH ...
  cp tmp_fips_release/pluginbuilder_release_tmp/org.faktorips.pluginbuilder_release/results/faktorips-$BUILD_VERSION-win32.win32.x86.zip $DOWNLOAD_PATH/faktorips-$BUILD_VERSION-aok-win32.win32.x86.zip
  handleError
  echo

  echo create updatesite for nwds
  cp /var/www/update.faktorzehn.org/faktorips/aok/downloads/faktorips-$MAJOR_VERSION.$MINOR_VERSION/$UPDATE_SITE_ZIP_NAME .
  # TODO in der site.xml de.aoksystems.omc.faktorips.feature.product auskommentieren
  mkdir tmp
  cp -r tmp_fips_release/checkout_release/org.faktorips.devtarget/eclipse/dropins/aok/eclipse/plugins tmp
  cp -r tmp_fips_release/checkout_release/org.faktorips.devtarget/eclipse/dropins/aok/eclipse/features tmp
  unzip -d tmp $UPDATE_SITE_ZIP_NAME 
  mv tmp/plugins/org.apache.commons.lang_2.6.jar tmp/plugins/org.apache.commons.lang_2.6.0.jar
  mv tmp/plugins/org.apache.commons.lang_2.6-sources.jar tmp/plugins/org.apache.commons.lang.sources_2.6.0.jar
  #  auskommentieren von <!-- <requires> ... </requires> -->
  mkdir tmpPatch
  cd tmpPatch  
  jar -xf ../tmp/features/org.faktorips.feature_${BUILD_VERSION}.jar feature.xml
  local FEATURE_XML=feature.xml
  cat $FEATURE_XML | sed "s/<requires>/<\!-- <requires>/" > ${FEATURE_XML}_TMP
  cat ${FEATURE_XML}_TMP | sed "s/<\/requires>/<\/requires> -->/" > $FEATURE_XML
  jar -uf ../tmp/features/org.faktorips.feature_${BUILD_VERSION}.jar *
  cd ..
  rm -r tmpPatch
  # fix nebula.jar Snapshot nicht im de.aok feature.xml umbenennen in ...20110316...jar
  # TODO warum liegt im devtarget deploy das Snapshot jar?
  mv tmp/plugins/org.eclipse.nebula.widgets.grid-1.0.0-SNAPSHOT.jar tmp/plugins/org.eclipse.nebula.widgets.grid_1.0.0.201103161451.jar
  cd tmp 
  zip -r $UPDATE_SITE_ZIP_NAME * -x \*CVS\* >> $LOGFILE
  echo copy result to $DOWNLOAD_PATH
  cp $UPDATE_SITE_ZIP_NAME /var/www/update.faktorzehn.org/faktorips/aok/downloads/faktorips-$MAJOR_VERSION.$MINOR_VERSION/updateSite_faktorips-${BUILD_VERSION}_nwds_aok.zip
  cd ..
  rm -r tmp
}

parseArgs $*
showParameter
doReleaseBuild

echo "OK: release build "${BUILD_VERSION}" successful"
if [ ! STORE_LOG = "true" ] ; then
  rm $LOGFILE
  echo "logfile deleted"
fi

