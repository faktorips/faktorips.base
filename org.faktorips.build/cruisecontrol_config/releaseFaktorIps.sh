#!/bin/bash
##############################################################################################################################
# Faktor IPS release build script
##############################################################################################################################
# Note that the default parameters are uses the server environment (faktorzehn.org).
# If tis script is running local, you can change the environment parameters by using the corresponding pararemters (see below).
# If this script should be executed under Windows then you need additional software like 'cygwin'.
#
# mandatory input parameter:
# -------------------------- 
#           -version [version]     : the version which should be generated and published, e.g. 2.2.0.rc1
#
# optional input parameters: 
# --------------------------
#           -category [category]   : the feature category used on the update site, 
#                                    default = the first two numbers from the build version (e.g. 2.2)
#           -overwrite             : to overwrite a previous version 
#                                    (if not set then the release build fails, if the release was build before)
#                                    default don't overwrite
#           -skipTest              : don't run the junit plugin tests
#                                    default run tests
#           -skipPublish           : don't publish the release to the update site and download directory
#                                    default publish the result
#           -skipTaggingCvs        : don't tag the projects before the release build will be performed, 
#                                    default is tag projects
#
# additional environment parameters (useful when the release must be build local)
# -------------------------------------------------------------------------------
#           -workingdir [dir]      : the absolute path to the working directory
#                                    default '/opt/cc/work'
#           -buildfile [file]      : the absolute path to the faktorips release build file
#                                    default '/opt/cc/work/build-faktorips.xml'
#           -projectsrootdir [dir] : root (parent) direcory of all faktorips projects
#                                    if cvs is used then all projects are checkout here,
#                                    if cvs isn't used (e.g. local build) then all projects must be exists here! 
#                                    default '/opt/cc/work/checkout_release'
#           -noCvs                 : copy projects instead using cvs,
#                                    default is use Cvs
#
# variables in the user environment:
# ----------------------------------
#                  ANT_HOME : ant home directory
#                              /opt/cc/apache-ant-1.6.5 if not set              
#                  JAVA_HOME : java home directory
#                              /opt/sun-jdk-1.5.0.08 if not set
#
#############################################################################################################################

#################################################
# init variables and parameters
#################################################

# default build environment
WORKINGDIR=/opt/cc/work
PROJECTSROOTDIR=$WORKINGDIR/checkout_release
CVS_ROOT=/usr/local/cvsroot
PUBLISH_DOWNLOAD_DIR=/var/www/localhost/htdocs/update.faktorzehn.org/faktorips/downloads
PUBLISH_UPDATESITE_DIR=/var/www/localhost/htdocs/update.faktorzehn.org/faktorips/

DEFAULT_ANT_HOME=/opt/cc/apache-ant-1.6.5
DEFAULT_JAVA_HOME=/opt/sun-jdk-1.5.0.08

# default java and ant environment
if [ -z "$ANT_HOME" ] ; then
  ANT_HOME=$DEFAULT_ANT_HOME
fi
if [ -z "$JAVA_HOME" ] ; then
  JAVA_HOME=$DEFAULT_JAVA_HOME
fi

FAKTORIPS_CORE_PLUGIN_NAME=org.faktorips.devtools.core
PLUGINBUILDER_PROJECT_NAME=org.faktorips.pluginbuilder

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
NOCVS=false

#################################################
# parse arguments
#################################################

while [ $# != 0 ]
do  case "$1" in
  -version)       BUILD_VERSION=$2 ; shift ;;
  -category)      BUILD_CATEGORY=$2 ; shift ;;
  -buildfile)     BUILDFILE=$2 ; shift ;;
  -workingdir)    WORKINGDIR=$2 ; shift ;;
  -projectsrootdir) PROJECTSROOTDIR=$2 ; shift ;;
  -overwrite)     OVERWRITE=true ;;
  -skipTest)      RUNTESTS=false ;;
  -skipPublish)   SKIPPUBLISH=true ;;
  -skipTaggingCvs) SKIPTAGCVS=true ;;
  -skipTaggingCvs) SKIPTAGCVS=true ;;
  -resultDir)     PUBLISH_DOWNLOAD_DIR=$2 ; shift ;;
  -updatesiteDir) PUBLISH_UPDATESITE_DIR=$2 ; shift ;;
  -noCvs)         NOCVS=true ;;
  -?)             SHOWHELP=true ;;
  --?)            SHOWHELP=true ;;
  -h)             SHOWHELP=true ;;
  --h)            SHOWHELP=true ;;
  -help)          SHOWHELP=true ;;
  --help)         SHOWHELP=true ;;
  -*)             SHOWHELP=true ; echo wrong parameter: $1 ;;
  esac
  shift
done

# check if version is given as parameter
if [ $BUILD_VERSION = "NONE" ] ; then
  if [ $SHOWHELP = "false" ] ; then
    echo '--> Error no version given!'
    echo '  '
  fi
  SHOWHELP=true
fi

# check parameter combinations
#  fail if cvs should be tagged but no cvs is used
if [ $NOCVS -a ! $SKIPTAGCVS ] ; then
  echo '--> Error: cvs should be tagged ('skipTaggingCvs' is not set) but no cvs is used ('noCvs' is set)'
  echo '  '
  SHOWHELP=true
fi

# check if buildfile exists or is given as parameter
#  use a) the given buildfile (-buildfile) 
#      b) the default in server environment 
#      c) the default in faktorips project directory
if [ -z "$BUILDFILE" ] ; then
  BUILDFILE=$WORKINGDIR/build-faktorips.xml
  if [ ! -e $BUILDFILE ] ; then
    BUILDFILE=$PROJECTSROOTDIR/org.faktorips.build/cruisecontrol_config/build-faktorips.xml
  fi
fi
if [ ! -e $BUILDFILE ] ; then
  echo '--> Error buildfile not exists:' $BUILDFILE
  echo '    check the environment or use parameter -buildfile or -projectsrootdir'
    echo '  '
  SHOWHELP=true
fi

# extract build category from given version, if not given
if [ $BUILD_CATEGORY = "NONE" ]
  then BUILD_CATEGORY=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\1\.\2/g")
fi 

#################################################
# show script help if requested 
# or wrong parameter given
#################################################

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
  echo '  -skipTaggingCvs        skip tagging the projects'
  echo '                         default = tag projects before build'
  echo '  -noCvs                 do not use cvs to get faktorips projects, if given then the projects will copied from the projectsrootdir'
  echo '                         default is use cvs'
  echo '  -workingdir            the directory where the release will be build in '
  echo '                         the default is: ' $WORKINGDIR
  echo '  -projectsrootdir       the root/parent dir of all projects, all projects will be checkedout here'
  echo '                         if no cvs is used \(e.g. local copy\) then all projects must be exists here'
  echo '                         the default is: ' $PROJECTSROOTDIR
  echo '                         '
  echo 'e.g.: '$0' -version 2.2.0.rc1 -skipTest <== builds the release with version 2.2.0.rc1, category 2.2 \ 
       and skip running the tests during the build'
  exit 1
fi

#################################################
# print parameter and ask to build
#################################################

echo 
echo Release build parameter:
echo "  --------------------------------------------------------------------------------------"
echo "  Release Version=\e[32m$BUILD_VERSION\e[0m"
echo "  Feature Category=\e[32m$BUILD_CATEGORY\e[0m"
echo "  --------------------------------------------------------------------------------------"
echo -e "  -overwite        : Overwrite previous version =>\e[32m$OVERWRITE\e[0m<="
echo -e "  -skipTest        : No tests are executed =>\e[32m$RUNTESTS\e[0m<="
echo -e "  -skipPublish     : No publish (to updatesite and to download directory) =>\e[32m$SKIPPUBLISH\e[0m<="
echo -e "  -skipTaggingCvs  : Skip tagging cvs projects =>\e[32m$SKIPTAGCVS\e[0m<="
echo -e "  -noCvs           : Not using cvs =>\e[32m$NOCVS\e[0m<="
echo -e "  -projectsrootdir : Checkout/Copysource directory =>\e[32m$PROJECTSROOTDIR\e[0m<="
echo -e "  -workingdir      : Work directory =>\e[32m$WORKINGDIR\e[0m<="
if [ ! "$SKIPPUBLISH" = "true" ] ; then
  echo    "  Published result"
  echo -e "  -resultDir       : Result/Download directory =>\e[32m$PUBLISH_DOWNLOAD_DIR\e[0m<="
  echo -e "  -updatesiteDir   : Updatesite directory =>\e[32m$PUBLISH_UPDATESITE_DIR\e[0m<="
fi
echo "  --------------------------------------------------------------------------------------"
echo -e "=> Start release build (\e[31my\e[0m)es? <="
echo 
read ANSWER
if [ ! "$ANSWER" = "y" ]
 then echo "Cancel"; exit 1
fi

#################################################
# asserts before release build
#################################################

# assert correct bundle version in core plugin
#   the bundle version stored in the core plugin must be equal to the given version
if [ ! $NOCVS ] ; then
    #  checkout core plugin and check bundle version
    PLUGINBUILDER_PROJECT_DIR=$PROJECTSROOTDIR/$PLUGINBUILDER_PROJECT_NAME
    TMP_CHECKOUTDIR=$PROJECTSROOTDIR/tmp_release_build
    mkdir $TMP_CHECKOUTDIR
    cvs -d $CVS_ROOT co -d $TMP_CHECKOUTDIR $FAKTORIPS_CORE_PLUGIN_NAME/META-INF
    CORE_BUNDLE_VERSION=$(cat $TMP_CHECKOUTDIR/MANIFEST.MF | grep Bundle-Version | sed -r "s/.*:\ *(.*)/\1/g")
    rm -R $TMP_CHECKOUTDIR
else 
    #  read bundle version from the core project stored in the projectsrootdir
    PLUGINBUILDER_PROJECT_DIR=$PROJECTSROOTDIR/$PLUGINBUILDER_PROJECT_NAME
    CORE_BUNDLE_VERSION=$(cat $PROJECTSROOTDIR/$FAKTORIPS_CORE_PLUGIN_NAME//META-INF/MANIFEST.MF | grep Bundle-Version | sed -r "s/.*:\ *(.*)/\1/g")
fi
# compare bundle version with given release version
if [ ! "$CORE_BUNDLE_VERSION" = "$BUILD_VERSION" ]
  then 
    echo "=> Cancel build: wrong bundle version in plugin '$FAKTORIPS_CORE_PLUGIN_NAME', found '$CORE_BUNDLE_VERSION', but expected '$BUILD_VERSION'"
    echo "   update the core bundle version or restart the release build with version '"$CORE_BUNDLE_VERSION"'"
    exit 1
fi 

#################################################
# perform the pre build steps
# - create release.properties
# - tag projects
#################################################

# change to working directory
if [ ! -e $WORKINGDIR ] ; then 
  mkdir -p $WORKINGDIR
fi
cd $WORKING_DIR

# tag cvs projects and generate release.properties
RELEASE_PROPERTY_DIR=$PLUGINBUILDER_PROJECT_DIR/releases
RELEASE_PROPERTIES=$RELEASE_PROPERTY_DIR/$BUILD_VERSION.properties

if [ ! $NOCVS ] ; then
  # 1. checkout previous pluginbuilder release properties
  cvs -d $CVS_ROOT co -d $RELEASE_PROPERTY_DIR org.faktorips.pluginbuilder/releases
fi

# 2. asserts 
#    a) check if release properties exist (only if overwrite is false)
if [ ! "$OVERWRITE" = "true" -a -f $RELEASE_PROPERTIES  ] ; then 
  echo "=> Cancel build: release already exists ("$RELEASE_PROPERTIES")"
  echo "   delete the previous release build or use parameter -overwrite"
  exit 1
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

# 4. checkin (add+commit) generated release.properties
if [ ! $NOCVS ] ; then
  if [ ! -f $RELEASE_PROPERTIES ] ; then
    # only a new file will be added
    cvs -d $CVS_ROOT add $RELEASE_PROPERTIES
  fi
  # update file in cvs
  cvs -d $CVS_ROOT commit -m "release build $BUILD_VERSION" $RELEASE_PROPERTIES
fi

# 5. tag all projects defined in the pluginbuilder project (move tag if already exists)
#    if skip tag is true then don't tag project, the previous tagged versions are used for the build
if [ ! "$SKIPTAGCVS" ] ; then
  # a) tag pluginbuilder project -> rtag = tag current versions of projects in repository
  cvs -d $CVS_ROOT rtag -F -R $FETCH_TAG $PLUGINBUILDER_PROJECT_NAME

  # b) tag all projects specified in the pluginbuilder map file (all necessary plugin and feature projects)
  for project in $( cat $PLUGINBUILDER_PROJECT_DIR/maps/all_copy.map | sed -r "s/.*COPY,@WORKSPACE@,(.*)/\1/g" ) ; do
    cvs -d $CVS_ROOT rtag -F -R $FETCH_TAG $project
  done
fi

#################################################
# call ant to perform the specified release build
#################################################

echo $BUILDFILE
EXEC="$ANT_HOME/bin/ant -buildfile $BUILDFILE release \
 -Dbuild.version=$BUILD_VERSION \
 -Dbuild.category=$BUILD_CATEGORY \
 -Doverwrite=$OVERWRITE \
 -Druntests=$RUNTESTS \
 -DskipPublish=$SKIPPUBLISH \
 -DprojectsRootDir=$PROJECTSROOTDIR \
 -Dbasedir=$WORKINGDIR \
 -DnoCvs=$NOCVS \
 -DdownloadDir=$PUBLISH_DOWNLOAD_DIR \
 -Dupdatesite.path=$PUBLISH_UPDATESITE_DIR \
 "
echo $EXEC
exec $EXEC
