#!/bin/bash
###########################################################################################################
# Faktor IPS release build script
# mandatory input: 
#                  -version [version] : the version which should be generated and published, e.g. 2.2.0.rc1
# optional input: 
#                  -category [category]: the feature category used on the update site, 
#                                        default =  the first two numbers from the build version (e.g. 2.2)
#                  -overwrite : overwrite previous version (otherwise if the release exists then the release build fails)
#                               default don't overwrite
#                  -skipTest : don't run the junit plugin tests
#                              default run tests
#                  -skipPublish : don't publish the release to the update site and download directory
#                                 default publish the result
##########################################################################################################

export DISPLAY=:0.0
export ANT_OPTS=-Xmx1024m

OVERWRITE=false
RUNTESTS=true
SKIPPUBLISH=false
BUILD_VERSION=NONE
BUILD_CATEGORY=NONE
SHOWHELP=false

while [ $# != 0 ]
do  case "$1" in
  -version)       BUILD_VERSION=$2 ; shift ;;
  -category)      BUILD_CATEGORY=$2 ; shift ;;
  -overwrite)     OVERWRITE=true ;;
  -skipTest)      RUNTESTS=false ;;
  -skipPublish)   SKIPPUBLISH=true ;;
  -?)             SHOWHELP=true ;;
  --?)            SHOWHELP=true ;;
  -h)             SHOWHELP=true ;;
  --h)            SHOWHELP=true ;;
  -help)          SHOWHELP=true ;;
  --help)         SHOWHELP=true ;;
  -*)   ;;
  esac
  shift
done

if [ $BUILD_VERSION = "NONE" ] ; then
  if [ $SHOWHELP = "false" ] ; then
    echo '--> Fehler keine version gegeben!'
    echo '  '
  fi
  SHOWHELP=true
fi

# Show script help if requested
if $SHOWHELP ; then
  echo 'Faktor IPS Release Build Script'
  echo 'usage:  '
  echo $0 '[script options]'
  echo 'script options:'
  echo '  -help, -h              print this message'
  echo 'mandatory:'
  echo '  -version [version]     the version which should be generated and published (e.g. "-version 2.2.0.rc1")'
  echo 'optional:'
  echo '  -category [category]   the feature category used on the update site,'
  echo '                         default = the first two numbers from the build version (e.g. "-category 2.2")'
  echo '  -overwrite             overwrite previous version (otherwise if the release exists then the release build fails)'
  echo '                         default = no overwrite'
  echo '  -skipTest              skip run of junit plugin tests'
  echo '                         default = run tests'
  echo '  -skipPublish           skip publish the release to the update site and download directory'
  echo '                         default = publish the result'
  echo '                         '
  echo 'e.g.: '$0' -version 2.2.0.rc1 -skipTest <== builds the release with version 2.2.0.rc1, category 2.2 \ 
       and skip running the tests during the build'
  exit 1
fi

if [ $BUILD_CATEGORY = "NONE" ]
  then BUILD_CATEGORY=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\1\.\2/g")
fi 

echo 
echo "Soll das Release erstellt werden (j)a ?"
echo "  ---------------------------------"
echo "  Release Version=$BUILD_VERSION"
echo "  Feature Category=$BUILD_CATEGORY"
echo "  ---------------------------------"
echo "  Overwrite previous version=$OVERWRITE"
echo "  Run tests=$RUNTESTS"
echo "  No publish (Updatesite and DownloadDir)=$SKIPPUBLISH"
echo 

read answer
if [ ! "$ANSWER" = "j" ]
 then echo "Abbruch"; exit 1
fi

ANT_HOME=/opt/cc/apache-ant-1.6.5
JAVA_HOME=/opt/sun-jdk-1.5.0.08
FAKTOR_IPS_BUILD_HOME=/opt/cc/work

EXEC="$ANT_HOME/ant -buildfile $FAKTOR_IPS_BUILD_HOME/build-faktorips.xml release \
 -Dbuild.version=$BUILD_VERSION -Dbuild.category=$BUILD_CATEGORY -Doverwrite=$OVERWRITE -Druntests=$RUNTESTS -DskipPublish=$SKIPPUBLISH"
echo $EXEC
exec $EXEC
