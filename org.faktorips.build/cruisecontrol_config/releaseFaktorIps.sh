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
#           -useBranch [branch]    : use a given branch instead HEAD
#           -buildProduct [product project dir]
#                                  : builds the product in the given project instead of building the features and plugins
#
# additional functionality
#-------------------------
#           -createBranch          : to create a branch, the latest head stand will be branched
#                                    with using -useBranch [branch] the name of the branch will be specified
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
# Functions
#################################################

printBoolean ()
{
if [ "$1" = "true" ] ; then
  echo -e "\e[32mtrue\e[0m"
else
  echo -e "\e[31mfalse\e[0m"
fi
}

negation ()
{
if [ "$1" = "true" ] ; then
  echo "false"
else
  echo "true"
fi
}

echo '  '

#################################################
# init variables and parameters
#################################################

CRUISE_ANT_HOME=/opt/cc/apache-ant-1.6.5

DEFAULT_ANT_HOME=$CRUISE_ANT_HOME
DEFAULT_JAVA_HOME=/opt/sun-jdk-1.5.0.08

# use cruise's ant if the user is cruise or
# if no ant home is set
if [ $(whoami) = "cruise" ] ; then 
  ANT_HOME=$CRUISE_ANT_HOME
else 
  # if ant is set and not ant command exists use default
  if [ -n "$ANT_HOME" -a ! -e $ANT_HOME/bin/ant ] ; then
    ANT_HOME=$DEFAULT_ANT_HOME
  fi
fi

# default build environment
#   substitution, if variable not use default
WORKINGDIR=${WORKINGDIR:-'/opt/cc/work'}
PUBLISH_DOWNLOAD_DIR=${PUBLISH_DOWNLOAD_DIR:-'/var/www/localhost/htdocs/update.faktorzehn.org/faktorips/downloads'}
PUBLISH_UPDATESITE_DIR=${PUBLISH_UPDATESITE_DIR:-'/var/www/localhost/htdocs/update.faktorzehn.org/faktorips'}
CVS_ROOT=${CVS_ROOT:-'/usr/local/cvsroot'}

PROJECTSROOTDIR=$WORKINGDIR/checkout_release
if [ ! -e $PROJECTSROOTDIR -a ! -d $PROJECTSROOTDIR ] ; then
  mkdir $PROJECTSROOTDIR
fi

# default java and ant environment
ANT_HOME=${ANT_HOME:-$DEFAULT_ANT_HOME}
JAVA_HOME=${JAVA_HOME:-$DEFAULT_JAVA_HOME}

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
BUILDPRODUCT=
BRANCH=
CREATE_BRANCH=false

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
  -useBranch)     BRANCH=$2 ; shift ;;
  -overwrite)     OVERWRITE=true ;;
  -skipTest)      RUNTESTS=false ;;
  -skipPublish)   SKIPPUBLISH=true ;;
  -skipTaggingCvs) SKIPTAGCVS=true ;;
  -buildProduct)  BUILDPRODUCT=$2 ; shift ;;
  -resultDir)     PUBLISH_DOWNLOAD_DIR=$2 ; shift ;;
  -updatesiteDir) PUBLISH_UPDATESITE_DIR=$2 ; shift ;;
  -noCvs)         NOCVS=true ;;
  -createBranch)  CREATE_BRANCH=true ;;
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

# enhance environment with given parameter
PLUGINBUILDER_PROJECT_DIR=$PROJECTSROOTDIR/$PLUGINBUILDER_PROJECT_NAME

# assert environment
if [ ! -e $ANT_HOME/bin/ant ] ; then
  echo 'Error ant not found '$ANT_HOME/bin/ant' - Please set ANT_HOME.' 
  echo '  '
  SHOWHELP=true
fi

#
# special case create branch
#   create branch and exit here
#
if [ "$CREATE_BRANCH" = "true" ] ; then
  if [ -z "$BRANCH" ] ; then
    echo "please specify the branch using -useBranch <branchname>"
    exit 1
  fi
  
  BRANCH_TAG="Root_"$BRANCH

  echo Create branch parameter:
  echo "  --------------------------------------------------------------------------------------"
  echo -e "  -useBranch       : Create cvs branch \e[35m$BRANCH\e[0m"
  echo "  --------------------------------------------------------------------------------------"
  echo -e "=> Start creating branch (\e[33my\e[0m)es? <="
  echo 
  read ANSWER
  if [ ! "$ANSWER" = "y" ]
    then echo "Cancel"; exit 1
  fi
  
  echo "branching "$BRANCH" ..."

  # TODO fail if tag exists!
  
  # branch all projects specified in the pluginbuilder map file (all necessary plugin and feature projects)
  cvs -d $CVS_ROOT co -d $PLUGINBUILDER_PROJECT_DIR/maps $PLUGINBUILDER_PROJECT_NAME/maps/all_copy.map
  for project in $( cat $PLUGINBUILDER_PROJECT_DIR/maps/all_copy.map | sed -r "s/.*COPY,@WORKSPACE@,(.*)/\1/g" ) ; do
    # 1. create root tag as start point for the branch 
    echo "tagging HEAD with branch tag: '"$BRANCH_TAG"', project "$project
    cvs -d $CVS_ROOT rtag -R $BRANCH_TAG $project
	  # 2. branch project 
	  #  -r : says that this branch should be rooted to this revision
    echo "create branch: '"$BRANCH"', project "$project
	  cvs -d $CVS_ROOT rtag -R -b -r $BRANCH_TAG $BRANCH $project
  done
  
  exit
fi

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
if [ "$NOCVS" = "true" -a ! "$SKIPTAGCVS" = "true" ] ; then
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

# extract build category from given version, if no category is given
if [ $BUILD_CATEGORY = "NONE" ]
  then BUILD_CATEGORY=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\1\.\2/g")
fi 

# if a branch should be used the cvs must be used
if [ $NOCVS = "true" -a -n "$BRANCH" ] ; then
    echo "=> Cancel build: a branch could only be used if cvs is used!"
    echo "   "
  SHOWHELP=true
fi

#################################################
# show script help if requested 
# or wrong parameter given
#################################################

if [ $SHOWHELP = "true" ] ; then
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
  echo '  -useBranch [branch]    use a given branch instead HEAD, when using branch cvs must be used'
  echo '  -noCvs                 do not use cvs to get faktorips projects,'
  echo '                         if given then the projects will copied from the projectsrootdir'
  echo '                         default is use cvs'
  echo '  -buildProduct [product project dir]'
  echo '                         builds the products in the given project instead of building the features and plugins'
  echo '  -workingdir [dir]      the directory where the release will be build in '
  echo '                         the default is: ' $WORKINGDIR
  echo '  -projectsrootdir [dir] the root/parent dir of all projects, all projects will be checkedout here'
  echo '                         if no cvs is used \(e.g. local copy\) then all projects must be exists here'
  echo '                         the default is: ' $PROJECTSROOTDIR
  echo 'additional functionality '
  echo '  -createBranch          to create a branch, the latest HEAD stand will be branched'
  echo '                         with -useBranch [branch] the name of the branch will be specified'
  echo '                         ' 
  echo 'e.g.: '$0' -version 2.2.0.rc1 -skipTest'
  echo '      builds the release with version 2.2.0.rc1, category 2.2 ' 
  echo '      and skip running the tests during the build'
  exit 1
fi

#################################################
# print parameter and ask to build
#################################################

echo 
echo Release build parameter:
echo "  --------------------------------------------------------------------------------------"
echo -e "  Release Version=\e[35m$BUILD_VERSION\e[0m"
echo -e "  Feature Category=\e[35m$BUILD_CATEGORY\e[0m"
echo "  --------------------------------------------------------------------------------------"
echo -e "  -overwite        : Fail if version exists "$(printBoolean $( negation $OVERWRITE))
echo -e "  -skipTest        : Run tests "$(printBoolean $RUNTESTS)
echo -e "  -skipPublish     : Publish result (to updatesite and to download directory) "$(printBoolean $(negation $SKIPPUBLISH))
echo -e "  -skipTaggingCvs  : Tag cvs projects "$(printBoolean $(negation $SKIPTAGCVS))
echo -e "  -noCvs           : Use cvs "$(printBoolean $(negation $NOCVS))
if [ -n "$BRANCH" ] ; then
  echo -e "  -useBranch       : Build using cvs branch \e[32m$BRANCH\e[0m"
else
  echo -e "  -useBranch       : None, use \e[35mHEAD\e[0m"
fi
if [ -n "$BUILDPRODUCT" ] ; then
  echo -e "  -buildProduct    : Build product \e[35m$BUILDPRODUCT\e[0m"
fi
echo -e "  -projectsrootdir : Checkout/Copysource directory \e[35m$PROJECTSROOTDIR\e[0m"
echo -e "  -workingdir      : Work directory \e[35m$WORKINGDIR\e[0m"
if [ ! "$SKIPPUBLISH" = "true" ] ; then
  echo    "  Published result"
  echo -e "  -resultDir       : Result/Download directory \e[35m$PUBLISH_DOWNLOAD_DIR\e[0m"
  echo -e "  -updatesiteDir   : Updatesite directory \e[35m$PUBLISH_UPDATESITE_DIR\e[0m"
fi
echo "  --------------------------------------------------------------------------------------"
echo -e "=> Start release build (\e[33my\e[0m)es? <="
echo 
read ANSWER
if [ ! "$ANSWER" = "y" ]
 then echo "Cancel"; exit 1
fi

#################################################
# asserts before release build
#################################################

VERSION_QUALIFIER=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\4/g")
VERSION=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\1\.\2\.\3/g")
FETCH_TAG=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/v\1_\2_\3_\4/g")

MIGRATION_STRATEGY_CLASS="Migration_"$(echo $FETCH_TAG | sed 's|v||g')".java"
MIGRATION_STRATEGY_PATH=$FAKTORIPS_CORE_PLUGIN_NAME"/src/org/faktorips/devtools/core/internal/migration/"$MIGRATION_STRATEGY_CLASS

# assert correct bundle version in core plugin and existing migration strategy
#   the bundle version stored in the core plugin must be equal to the given version
#   the migration strategy java class must be exists see MIGRATION_STRATEGY_PATH
MIGRATION_EXISTS=false
if [ ! "$NOCVS" = "true" ] ; then
    # Note: if skipTaggingCvs is used then this assert may be check the wrong versions
    #       because only the latest file will be checked, because the tagging will be performed later
    #  checkout core plugin and check bundle version
    TMP_CHECKOUTDIR1=$PROJECTSROOTDIR/tmp_release_build1
    TMP_CHECKOUTDIR2=$PROJECTSROOTDIR/tmp_release_build2
    mkdir $TMP_CHECKOUTDIR1
    mkdir $TMP_CHECKOUTDIR2
    if [ -n "$BRANCH" ] ; then
      echo "checkout using branch: "$BRANCH
      cvs -d $CVS_ROOT co -d $TMP_CHECKOUTDIR1 -r $BRANCH $FAKTORIPS_CORE_PLUGIN_NAME/META-INF
      cvs -d $CVS_ROOT co -d $TMP_CHECKOUTDIR2 -r $BRANCH $MIGRATION_STRATEGY_PATH
    else
      echo "checkout HEAD" 
      cvs -d $CVS_ROOT co -d $TMP_CHECKOUTDIR1 $FAKTORIPS_CORE_PLUGIN_NAME/META-INF
      cvs -d $CVS_ROOT co -d $TMP_CHECKOUTDIR2 $MIGRATION_STRATEGY_PATH
    fi
    
    CORE_BUNDLE_VERSION=$(cat $TMP_CHECKOUTDIR1/MANIFEST.MF | grep Bundle-Version | sed -r "s/.*:\ *(.*)/\1/g")

    if [ -e $TMP_CHECKOUTDIR2/$MIGRATION_STRATEGY_CLASS ] ; then
    	MIGRATION_EXISTS=true
    fi
    
    rm -R $TMP_CHECKOUTDIR1
    rm -R $TMP_CHECKOUTDIR2
else
    if [ -e $PROJECTSROOTDIR/MIGRATION_STRATEGY ] ; then
    	MIGRATION_EXISTS=true
    fi
    
    #  read bundle version from the core project stored in the projectsrootdir
    CORE_BUNDLE_VERSION=$(cat $PROJECTSROOTDIR/$FAKTORIPS_CORE_PLUGIN_NAME//META-INF/MANIFEST.MF | grep Bundle-Version | sed -r "s/.*:\ *(.*)/\1/g")
fi

if [ "$MIGRATION_EXISTS" = "false" ] ; then
  echo "=> Cancel build: Migrationstrategy not exists (if using cvs the java source must also be tagged)! "$MIGRATION_STRATEGY
  exit 1
else
  echo "Ok migration strategy class found"
fi

# compare bundle version with given release version
if [ ! "$CORE_BUNDLE_VERSION" = "$BUILD_VERSION" ]
  then 
    echo "=> Cancel build: wrong bundle version in plugin '$FAKTORIPS_CORE_PLUGIN_NAME', found '$CORE_BUNDLE_VERSION', but expected '$BUILD_VERSION'"
    echo "   update the core bundle version or restart the release build with version '"$CORE_BUNDLE_VERSION"'"
    exit 1
fi 

# check if migration strategy exists
if [ ! "$NOCVS" = "true" ] ; then
  TMP_CHECKOUTDIR=$PROJECTSROOTDIR/tmp_release_build
    mkdir $TMP_CHECKOUTDIR  
  rm -R $TMP_CHECKOUTDIR
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

# 1. checkout previous pluginbuilder release properties
if [ ! "$NOCVS" = "true" ] ; then
  rm -R $RELEASE_PROPERTY_DIR
  cvs -d $CVS_ROOT co -d $RELEASE_PROPERTY_DIR $PLUGINBUILDER_PROJECT_NAME/releases
fi

# 2. asserts 
#    a) check if release properties exist (only if overwrite is false)
if [ ! "$OVERWRITE" = "true" -a -f $RELEASE_PROPERTIES  ] ; then 
  echo "=> Cancel build: release already exists ("$RELEASE_PROPERTIES")"
  echo "   delete the previous release build or use parameter -overwrite"
  exit 1
fi

# 3. generate release.properties
RELEASE_PROPERTIES_EXISTS=false
if [ -f $RELEASE_PROPERTIES ] ; then
  RELEASE_PROPERTIES_EXISTS=true
fi

# assert 
#    b) if release property already exists then tagging Cvs is not allowed
if [ $RELEASE_PROPERTIES_EXISTS = "true" -a ! "$SKIPTAGCVS" = "true" ] ; then
  echo "=> Cancel build: tagging is not allowed if the release already exists!"
  echo "   Please use -skipTaggingCvs or remove the release.properties  and try again."
  exit 1
fi

# create release property file if not exists or overwrite is true
if [ "$RELEASE_PROPERTIES_EXISTS" = "false" -o "$OVERWRITE" = "true" ] ; then
  echo "# written by $0" > $RELEASE_PROPERTIES
  echo "# $(date)" >> $RELEASE_PROPERTIES
  echo buildType= >> $RELEASE_PROPERTIES
  echo version=$VERSION  >> $RELEASE_PROPERTIES
  echo fetchTag=$FETCH_TAG  >> $RELEASE_PROPERTIES
  echo version.qualifier=$VERSION_QUALIFIER  >> $RELEASE_PROPERTIES
  echo buildTypePresentation=  >> $RELEASE_PROPERTIES
  
  # 4. checkin (add+commit) generated release.properties
  if [ ! "$NOCVS" = "true" ] ; then
    if [ "$RELEASE_PROPERTIES_EXISTS" = "false" ] ; then
      # only a new file will be added
      cvs -d $CVS_ROOT add $RELEASE_PROPERTIES
    fi
    # update file in cvs
    cvs -d $CVS_ROOT commit -m "release build $BUILD_VERSION" $RELEASE_PROPERTIES
  fi
fi

# 5. tag all projects defined in the pluginbuilder project (move tag if already exists)
#    if skip tag is true then don't tag project, the previous tagged versions are used for the build
if [ ! "$SKIPTAGCVS" = "true" ] ; then
  # a) tag pluginbuilder project 
  #     -> rtag : tag current versions of projects in repository
  #     -> -F : move tag if it already exists (overwrite checked above, by searching for existing release.properties)
  #     -> -R : process directories recursively
  #    the pluginbuilder project doesn't support branches (not necessary) (-r)
  cvs -d $CVS_ROOT rtag -F -R $FETCH_TAG $PLUGINBUILDER_PROJECT_NAME

  # b) tag all projects specified in the pluginbuilder map file (all necessary plugin and feature projects)
  cvs -d $CVS_ROOT co -r $FETCH_TAG -d $PLUGINBUILDER_PROJECT_DIR/maps $PLUGINBUILDER_PROJECT_NAME/maps/all_copy.map
  for project in $( cat $PLUGINBUILDER_PROJECT_DIR/maps/all_copy.map | sed -r "s/.*COPY,@WORKSPACE@,(.*)/\1/g" ) ; do
    if [ -n "$BRANCH" ] ; then
      # using branch
      # -> -r rev : existing revision/tag
      echo "tagging using branch: "$BRANCH", project: "$project
      cvs -d $CVS_ROOT rtag -F -R -r $BRANCH $FETCH_TAG $project
    else
      # not using branch
      echo "tagging HEAD, project: "$project
      cvs -d $CVS_ROOT rtag -F -R $FETCH_TAG $project
    fi
  done
fi

# if using a branch then the all_cvs.map file must be patched, 
#   -> if no cvs is used then no patching is necessary (but this is not supported, see assert below)
NOBRANCH=true
if [ -n "$BRANCH" -a ! "$NOCVS" = "true" ] ; then
  # replace HEAD with given BRANCH
  NOBRANCH=false
  cat $PLUGINBUILDER_PROJECT_DIR/maps/all_cvs.map | sed -r "s/(.*)HEAD(.*)/\1$BRANCH\2/g" > $PLUGINBUILDER_PROJECT_DIR/maps/all_cvs_branch.map
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
 -DproductProject=$BUILDPRODUCT \
 -DnoBranch=$NOBRANCH
 "
echo $EXEC
exec $EXEC
