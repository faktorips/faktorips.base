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
#                  -skipTagCvs : don't tag the projects before the release build will be performed, 
#                                default is tag projects
##########################################################################################################

WORKING_DIR=/opt/cc/work
CHECKOUT_WORKSPACE=$WORKING_DIR/checkout

export DISPLAY=:0.0
export ANT_OPTS=-Xmx1024m

# default parameters
OVERWRITE=false
RUNTESTS=true
SKIPPUBLISH=false
BUILD_VERSION=NONE
BUILD_CATEGORY=NONE
SHOWHELP=false
SKIPTAGCVS=false

# change to working directory
cd $WORKING_DIR

# parse arguments
while [ $# != 0 ]
do  case "$1" in
  -version)       BUILD_VERSION=$2 ; shift ;;
  -category)      BUILD_CATEGORY=$2 ; shift ;;
  -overwrite)     OVERWRITE=true ;;
  -skipTest)      RUNTESTS=false ;;
  -skipPublish)   SKIPPUBLISH=true ;;
  -skipTagCvs)    SKIPTAGCVS=true ;;
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

# check if version is givem
if [ $BUILD_VERSION = "NONE" ] ; then
  if [ $SHOWHELP = "false" ] ; then
    echo '--> Fehler keine version gegeben!'
    echo '  '
  fi
  SHOWHELP=true
fi

# show script help if requested
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
  echo '  -skipTagCvs            skip tagging the projects'
  echo '                         default = tag projects before build'
  echo '                         '
  echo 'e.g.: '$0' -version 2.2.0.rc1 -skipTest <== builds the release with version 2.2.0.rc1, category 2.2 \ 
       and skip running the tests during the build'
  exit 1
fi

# extract build category if not given
if [ $BUILD_CATEGORY = "NONE" ]
  then BUILD_CATEGORY=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\1\.\2/g")
fi 

# print parameter and ask to build
echo 
echo Release build parameter:
echo "  ---------------------------------"
echo "  Release Version=$BUILD_VERSION"
echo "  Feature Category=$BUILD_CATEGORY"
echo "  ---------------------------------"
echo "  Overwrite previous version (-overwrite)=$OVERWRITE"
echo "  Run tests (-skipTest)=$RUNTESTS"
echo "  No publish (-skipPublish) (Updatesite and DownloadDir)=$SKIPPUBLISH"
echo "  Skip tagging cvs projects (-skipTagCvs)=$SKIPTAGCVS"
echo "  ---------------------------------"
echo "Start release build (y)es?"
echo 
read ANSWER
if [ ! "$ANSWER" = "y" ]
 then echo "Cancel"; exit 1
fi

# tag cvs projects and generate release.properties
if [ ! "$SKIPTAGCVS" = "true" ]; then 
  RELEASE_PROPERTY_DIR=$CHECKOUT_WORKSPACE/org.faktorips.pluginbuilder/releases
  RELEASE_PROPERTIES=$RELEASE_PROPERTIE_DIR/$BUILD_VERSION.properties

  # 1. checkout pluginbuilder release properties
  cvs -d /usr/local/cvsroot co -d $RELEASE_PROPERTY_DIR org.faktorips.pluginbuilder/releases
  
  # 2. assert if properties exist and overwite is false
  if [ ! "$OVERWRITE" = "true" -a -f $RELEASE_PROPERTIES  ]
    then echo "Cancel build: release already exists ($RELEASE_PROPERTIES)"; exit 1
  fi
  
  # 3. generate release.properties
  VERSION_QUALIFIER=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\4/g")
  VERSION=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\1\.\2\.\3/g")
  FETCH_TAG=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/v\1_\2_\3_\4/g")
  echo "# written from $0" > $RELEASE_PROPERTIES
  echo "# $(date)" >> $RELEASE_PROPERTIES
  echo buildType= >> $RELEASE_PROPERTIES
  echo version=$VERSION  >> $RELEASE_PROPERTIES
  echo fetchTag=$FETCH_TAG  >> $RELEASE_PROPERTIES
  echo version.qualifier=$VERSION_QUALIFIER  >> $RELEASE_PROPERTIES
  echo buildTypePresentation=  >> $RELEASE_PROPERTIES

  # 4. checkin (add) generated release.properties
  
  
  # 5. tag all projects defined in the pluginbuilder project if skip tag is false
  if [ ! "$SKIPTAGCVS" = "true" ] ; then
    for project in $(cat $CHECKOUT_WORKSPACE/org.faktorips.pluginbuilder/maps/all_copy.map | sed -r "s/.*COPY,@WORKSPACE@,(.*)/\1/g")
      do echo $project
    done
  fi
  exit 1
fi

# call ant to perform the specified release build
ANT_HOME=/opt/cc/apache-ant-1.6.5
JAVA_HOME=/opt/sun-jdk-1.5.0.08
FAKTOR_IPS_BUILD_HOME=/opt/cc/work

EXEC="$ANT_HOME/ant -buildfile $FAKTOR_IPS_BUILD_HOME/build-faktorips.xml release \
 -Dbuild.version=$BUILD_VERSION \
 -Dbuild.category=$BUILD_CATEGORY \
 -Doverwrite=$OVERWRITE \
 -Druntests=$RUNTESTS \
 -DskipPublish=$SKIPPUBLISH"
echo $EXEC
exec $EXEC
