#!/bin/bash
# TODO siehe Assert1
##############################################################################################################################
# Faktor IPS release build script
##############################################################################################################################
# Note that the default parameters are uses the server environment (faktorzehn.org).
# If this script is running local, you can change the environment parameters by using the corresponding parameters (see below).
# If this script should be executed under Windows then you need additional software like 'cygwin'.
#
# mandatory input parameter:
# -------------------------- 
#           -version [version]     : the version which should be generated and published, e.g. 2.2.0.rc1
#
# optional input parameters: 
# --------------------------
#           -customBuild [name]   : to use a special set of plugins or features (e.g. name=aok or ergo)
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
#           -useBranch [branch]    : use a given branch instead HEAD (only necessary if the projects should be tagged)
#           -buildProduct [product project dir]
#                                  : builds the product in the given project instead of building the features and plugins
#           -resultDir             : publish directory
#           -updatesiteDir         : updatesite directory
#			-updatesiteFile		   : the name of the update site file
#
# hiden parameter:
# ----------------
#           -forceBuild            : force build, no resource limit check  
#           -forceTaggingCvs       : force tagging projects (no assert if the release already exists)
#
# additional functionality
#-------------------------
#           -createBranch          : to create a branch, the latest head stand will be branched by default (see -branchRootTag)
#                                    with -version [versionnumber] the name (version) of the base version
#                                    used for this branch must be specified (e.g. "-version 2.2.branch")
#           -branchRootTag <tag>   : optinal - use the given tag as starting point for the branch otherwise HEAD will be used
#                                    (e.g. "-branchRootTag v2_2_0_rfinal")
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

BASH_PARAM="-e"
set $BASH_PARAM

# fix problem with swt and x server?
GDK_NATIVE_WINDOWS=1

echo '  '

BUILD_VERSION=""
BUILD_CATEGORY=""
BRANCH=""
BUILDPRODUCT=""
OVERWRITE=false
RUNTESTS=true
SKIPPUBLISH=false
SKIPTAGCVS=false
NOCVS=false
CREATE_BRANCH=false
FORCE_BUILD=false
FORCE_TAGCVS=false
CUSTOM_BUILD="NONE"

FAKTORIPS_CORE_PLUGIN_NAME=org.faktorips.devtools.core
PLUGINBUILDER_PROJECT_NAME=org.faktorips.pluginbuilder
BUILD_PROJECT_NAME=org.faktorips.build
INTEGRATIONTEST_PROJECTS=(org.faktorips.integrationtest org.faktorips.integrationtest.java5)
CUSTOMER_PRODUCT_PROJECT=de.qv.faktorips.feature.product
DEVTARGET_PLUGIN_NAME=org.faktorips.devtarget

CREATE_LIZENZ_SCRIPT=$PLUGINBUILDER_PROJECT_NAME/lizenz/createFaktorIpsLizenz.sh
LIZENZ_PDF=$PLUGINBUILDER_PROJECT_NAME/lizenz/result/FaktorIPS_Lizenzvertrag.pdf
DATENSCHUTZ_PDF=$PLUGINBUILDER_PROJECT_NAME/lizenz/result/FaktorIPS_Datenschutzbestimmung.pdf

###########
# functions
###########
. $(dirname $0)/releaseFaktorIpsCvsModule

initEnvironment()
{
  # environment
  
  if [ $MAJOR_VERSION -ge 3 ] ; then
  	MIN_VERSION_3=true
  else
  	MIN_VERSION_3=false
  fi
  
  export DISPLAY=:0.0
  export ANT_OPTS=-Xmx1024m

  if [ ! -d $PROJECTSROOTDIR ] ; then
    mkdir $PROJECTSROOTDIR
  fi

  # TODO checkoutdir aufraeumen removen

  # default java and ant environment
  ANT_HOME=$DEFAULT_ANT_HOME
  JAVA_HOME=$DEFAULT_JAVA_HOME
  
  # cvs logfile
  if [ -f $CVS_LOG ] ; then
    rm $CVS_LOG
  fi 
  touch $CVS_LOG
}

initDefaultParameter()
{
  # etc
  DEFAULT_CVS_ROOT='/usr/local/cvsroot'
  DEFAULT_ANT_HOME=${DEFAULT_ANT_HOME:-'/opt/cc/apache-ant-1.6.5'}
  DEFAULT_JAVA_HOME=${DEFAULT_JAVA_HOME:-'/usr/lib/jvm/java-6-sun'}
  CVS_ROOT=${CVS_ROOT:-$DEFAULT_CVS_ROOT}

  WORKINGDIR=${WORKINGDIR:-'./tmp_fips_release'}
  # create working dir
  if [ ! -d $WORKINGDIR ] ; then 
    mkdir -p $WORKINGDIR
  fi
  # convert to absolut path
  WORKINGDIR=$(cd $WORKINGDIR; pwd)
  if [ -z $PROJECTSROOTDIR ] ; then
    PROJECTSROOTDIR=$WORKINGDIR/checkout_release
  fi
  
  MAJOR_VERSION=`echo $BUILD_VERSION | awk -F. '{print $1}'`
  MINOR_VERSION=`echo $BUILD_VERSION | awk -F. '{print $2}'`
  
  if [ $MAJOR_VERSION -ge 3 ] ; then
	PUBLISH_DOWNLOAD_DIR=${PUBLISH_DOWNLOAD_DIR:-'/var/www/update.faktorzehn.org/faktorips/v'$MAJOR_VERSION'_'$MINOR_VERSION'/downloads'}
  	PUBLISH_UPDATESITE_DIR=${PUBLISH_UPDATESITE_DIR:-'/var/www/update.faktorzehn.org/faktorips/v'$MAJOR_VERSION'_'$MINOR_VERSION}
  	PUBLISH_UPDATESITE_FILE=${PUBLISH_UPDATESITE_FILE:-'site_v'$MAJOR_VERSION'_'$MINOR_VERSION.xml}
  else
  	PUBLISH_DOWNLOAD_DIR=${PUBLISH_DOWNLOAD_DIR:-'/var/www/update.faktorzehn.org/faktorips/downloads'}
  	PUBLISH_UPDATESITE_DIR=${PUBLISH_UPDATESITE_DIR:-'/var/www/update.faktorzehn.org/faktorips'}
  fi
  PLUGINBUILDER_PROJECT_DIR=$PROJECTSROOTDIR/$PLUGINBUILDER_PROJECT_NAME
  RELEASE_PROPERTY_DIR=$PLUGINBUILDER_PROJECT_DIR/releases
  RELEASE_PROPERTIES=$RELEASE_PROPERTY_DIR/$BUILD_VERSION.properties
  
  CVS_LOG=$(pwd)/cvs.log
}

getFetchTagVersion ()
{
 INPUT_VERSION=$1
 export VERSION_QUALIFIER=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\4/g")
 export VERSION=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\1\.\2\.\3/g")
 export FETCH_TAG=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/v\1_\2_\3_\4/g")
# release numbers withput qualifierer are not supported by eclipse build files (e.g. 1.0.0)?
# if [ "$FETCH_TAG" = "$INPUT_VERSION" ] ; then
#   # maybe without qualifier
#   export VERSION_QUALIFIER="NONE"
#   export VERSION=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)/\1\.\2\.\3/g")
#   export FETCH_TAG=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.(.*)/v\1_\2_\3/g")
# fi
 assertVersionFormat $VERSION $VERSION_QUALIFIER $FETCH_TAG \
  "Wrong release version format '$INPUT_VERSION', must be tree numbers followed by the qualifier (major.minor.micro.qualifier), e.g. 2.2.0.rfinal"
 if [ "$VERSION_QUALIFIER" = "NONE" ] ; then
   export VERSION_QUALIFIER=""
 fi
}

getFetchTagVersionForBranch ()
{
 INPUT_VERSION=$1
 export VERSION_QUALIFIER=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.(.*)/\3/g")
 export VERSION=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)/\1\.\2/g")
 export FETCH_TAG=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.(.*)/v\1_\2_\3/g")
 assertVersionFormat $VERSION $VERSION_QUALIFIER $FETCH_TAG \
  "Wrong branch version format '$INPUT_VERSION', must be two numbers followed by the qualifier (major.minor.qualifier), e.g. 2.2.patches"
}

assertVersionFormat ()
{
 VERSION=$1 
 VERSION_QUALIFIER=$2
 FETCH_TAG=$3
 FAIL_MESSAGE=$4
 POINTEXISTS=$( echo $VERSION | grep "\." | wc -l )
 if [ -z $VERSION -o -z $FETCH_TAG -o "$VERSION" = "$FETCH_TAG" -o "$POINTEXISTS" -eq 0 ] ; then
   echo $FAIL_MESSAGE
   exit 1
 fi
}

generateIndexHtml ()
{
  local LINK_PREFIX=$1
  local DIR=$2
  local FIELD_SEPARATOR=2
  if [ $# -gt 2 ] ; then
    local CATEGORY=$3
    local DIR=$DIR"/"$CATEGORY
    # -b11 = pos after 'faktorips_'
    FIELD_SEPARATOR=$(echo $CATEGORY | cut -b11)
  fi

  local OUTFILE="index.html"
  local OUT=$DIR"/"$OUTFILE
  
  echo "<html>" > $OUT
  
  local PREV_FILE_VERSION=0
  # for each file order by version nr
  for i in $(ls $DIR | sort -t$FIELD_SEPARATOR -k2) ; do
    if [ "$i" = "$OUTFILE" ] ; then
      # ignore index.html file
      continue
    fi
    # only files containing "faktorips" or "updatesite"
    if [ ! $( echo $i | grep "faktorips" | wc -l ) -eq 1 -a ! $( echo $i | grep updatesite | wc -l) -eq 1 ] ; then
        continue
    fi
    
    # space between categories
    local FILE_VERSION=$(echo $i | sed -r 's|.*-([0-9]*\.[0-9]*\.[0-9]*)\..*|\1|g')
    if [ ! "$PREV_FILE_VERSION" = "$FILE_VERSION" ] ; then
      echo "<br>" >> $OUT
    fi
    PREV_FILE_VERSION=$FILE_VERSION

    if [ -d $DIR"/"$i ] ; then
      # directory link
      FILE=$LINK_PREFIX"/"$i"/index.html"
      echo "Category: <a href=\"$FILE\">"$i"</a><br>" >> $OUT
    else 
      # file link
      FILE=$LINK_PREFIX"/"$CATEGORY"/"$(basename $i)
      echo "  <a href=\"$FILE\">"$i"</a><br>" >> $OUT
    fi
  done
  
  echo "</html>" >> $OUT	
}

parseArgs()
{
	DO_CREATE_BRANCH=false
	
	while [ $# != 0 ]
	do case "$1" in
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
	  -forceTaggingCvs) FORCE_TAGCVS=true ;;
	  -buildProduct)  BUILDPRODUCT=$2 ; shift ;;
	  -customBuild)  CUSTOM_BUILD=$2 ; shift ;;
	  -resultDir)     PUBLISH_DOWNLOAD_DIR=$2 ; shift ;;
	  -updatesiteDir) PUBLISH_UPDATESITE_DIR=$2 ; shift ;;
	  -updatesiteFile) PUBLISH_UPDATESITE_FILE=$2 ; shift ;;
	  -noCvs)         NOCVS=true ;;
	  -createBranch)  DO_CREATE_BRANCH=true ;;
	  -branchRootTag) BRANCH_ROOT_TAG=$2 ; shift ;;
	  -forceBuild)    FORCE_BUILD=true ;;
	  -?)             showUsageAndExit ;;
	  --?)            showUsageAndExit ;;
	  -h)             showUsageAndExit ;;
	  --h)            showUsageAndExit ;;
	  -help)          showUsageAndExit ;;
	  --help)         showUsageAndExit ;;
	  -*)             echo wrong parameter: $1 ; showUsageAndExit;;
	  esac
	  shift
	done
	
	if [ "$DO_CREATE_BRANCH" = "true"  ] ; then
      initDefaultParameter
      getFetchTagVersionForBranch $BUILD_VERSION
      createBranch $FETCH_TAG $BRANCH_ROOT_TAG
      exit 0
	fi
	
	# special functinality e.g. create branch
	
}

showUsageAndExit()
{
  echo 'Faktor IPS Release Build Script'
  echo 'usage:  '
  echo $0 '[script options]'
  echo 'script options:'
  echo '  -help, -h              print this message'
  echo 'mandatory:'
  echo '  -version [version]     the version which should be generated and published (e.g. "-version 2.2.0.rc1")'
  echo 'optional:'
  echo '  -customBuild [name]    to use a special set of plugins or features (e.g. name=aok or ergo)'
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
  echo '  -useBranch [branch]    use a given branch instead HEAD (only necessary if the projects should be tagged)'
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
  echo ' -resultDir [dir]        the result (publish) directory'
  echo ' - updatesiteDir [dir]   the updatesite directory'
  echo 'additional functionality '
  echo '  -createBranch          to create a branch, the latest HEAD stand will be branched by default (see -branchRootTag)'
  echo '                         with -version [versionnumber] the name (version) of the base version'
  echo '                         used for this branch must be specified (e.g. "-version 2.2.branch")'
  echo '  -branchRootTag <tag>   optional - use the given tag as starting point for the branch otherwise HEAD will be used'
  echo '                         ' 
  echo 'e.g.: '$0' -version 2.2.0.rc1 -skipTest'
  echo '      builds the release with version 2.2.0.rc1, category 2.2 ' 
  echo '      and skip running the tests during the build'
  exit 1
}

doAsserts()
{
    # check if version is given as parameter
    if [ -z "$BUILD_VERSION" ] ; then
        echo '--> Error no version given!'
        echo '  '
        showUsageAndExit
    fi

    # assert that no other instance of pluginbuilder is running
    if [ ! $(ps xaf | grep prg=pluginbuilder | grep -v "grep" | wc -l) -eq 0 -a ! "$FORCE_BUILD" = "true" ] ; then
      echo "Cancel build: an instance of pluginbuilder is currently running!"
      echo "Due to resource limit, please wait until the other pluginbuilder process has finished."
      echo "If the plugin test are executed, this could take at least 30 minutes..."
      echo "or use -forceBuild if you want to ignore this assert" 
      exit 1;
    fi
    
    # assert environment
    if [ ! -f $ANT_HOME/bin/ant ] ; then
      echo 'Error ant not found '$ANT_HOME/bin/ant' - To overwrite this default, please set DEFAULT_ANT_HOME.'
      echo '  '
      exit 1
    fi
    if [ ! -d $JAVA_HOME ] ; then
      echo 'Error java home not found '$JAVA_HOME' - To overwrite this default, please set DEFAULT_JAVA_HOME.'
      echo '  '
      exit 1
    fi
 
# version check disabled since 2.5
#      
#    #assert version number "micro" only used in branch
#    #  if taging is skipped then it is not necessary to specify a branch
#    MICRO_VERSION=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\3/g")
#    if [ ! "$SKIPTAGCVS" = "true" ] ; then 
#	    if [  -n "$BRANCH" ] ; then
#	      # branch
#	      if [ "$MICRO_VERSION" = "0" ] ; then
#	        echo 'Error: if using a branch then the micro number must be greater than 0! Micro version used: '$MICRO_VERSION
#	        echo 'Please choose a differt number or remove the option -useBranch'
#	        echo '  '
#	        exit 1
#	      fi
#	    else 
#	      # head
#	      if [ "$MICRO_VERSION" != "0" ] ; then
#	        echo 'Error: if using head then the micro number must be 0! Micro version used: '$MICRO_VERSION
#	        echo 'Please choose a different number or specify a branch using: -useBranch <Branchname>'
#	        echo '  '
#	        exit 1
#	      fi
#	    fi
#    fi 
}

assertValidParameters()
{
	# check parameter combinations
	#  fail if cvs should be tagged but no cvs is used
	if [ "$NOCVS" = "true" -a ! "$SKIPTAGCVS" = "true" ] ; then
	  echo '--> Error: cvs should be tagged ('skipTaggingCvs' is not set) but no cvs is used ('noCvs' is set)'
	  echo '  '
	  showUsageAndExit
	fi
	
	# check if buildfile exists or is given as parameter
	#  use a) the given buildfile (-buildfile) 
	#      b) the default in server environment 
	#      c) the default in faktorips project directory
	if [ -z "$BUILDFILE" ] ; then
	  BUILDFILE=$WORKINGDIR/build-faktorips.xml
	  if [ ! -f $BUILDFILE ] ; then
	    BUILDFILE=$PROJECTSROOTDIR/org.faktorips.build/cruisecontrol_config/build-faktorips.xml
	  fi
	  if [ ! -f $BUILDFILE ] ; then
	  	CURR_DIR=$(dirname $0)
        CURR_DIR=$(cd $CURR_DIR; pwd) # convert to absolut path
	    BUILDFILE=$CURR_DIR/build-faktorips.xml
	  fi  	  
	fi
	if [ ! -f $BUILDFILE ] ; then
	  echo '--> Error buildfile not exists:' $BUILDFILE
	  echo '    check the environment or use parameter -buildfile or -projectsrootdir'
	  echo '  '
	  showUsageAndExit
	fi
	
	# extract build category from given version, if no category is given
	if [ -z "$BUILD_CATEGORY" ]
	  then BUILD_CATEGORY=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*).*/\1\.\2/g")
	fi 
	
	# if a branch should be used the cvs must be used
	if [ $NOCVS = "true" -a -n "$BRANCH" ] ; then
	  echo "=> Cancel build: a branch could only be used if cvs is used!"
	  echo "   no cvs is true!"
	  showUsageAndExit
	fi
	
	# check unnecessary parameter cvs
	if [ "$SKIPTAGCVS" = "true" -a -n "$BRANCH" ] ; then
	  echo "=> Cancel build: the branch parameter must not be given (unnecessary) because an existing tag will be used."
	  echo "   (Skip tagging projects is true)"
	  echo "   Note: The tag '$FETCH_TAG' will be used for the build, make sure that this is a tag in the branch tree!" 
	  echo "         And try again without using the branch parameter" 
	  showUsageAndExit
	fi
	
	# check unnecessary parameter overwrite
#	if [ "$OVERWRITE" = "true" -a "$SKIPPUBLISH" = "true" ] ; then
#	  echo "=> Cancel build: the -overwrite parameter must not be given (unnecessary) because -skipPublish is true."
#	  echo "                 use script without -overwrite and try again" 
#	  showUsageAndExit
#	fi
}

showParameter()
{	 
  echo 
  echo Release build parameter:
  echo "  --------------------------------------------------------------------------------------"
  echo -e "  Release Version=\e[35m$BUILD_VERSION\e[0m"
  echo -e "  Major Version=\e[35m$MAJOR_VERSION\e[0m"
  echo -e "  Minor Version=\e[35m$MINOR_VERSION\e[0m"
  echo -e "  Min Version 3=\e[35m$MIN_VERSION_3\e[0m"
  echo -e "  Feature Category=\e[35m$BUILD_CATEGORY\e[0m"
  echo -e "  CVS Tag=\e[35m$FETCH_TAG\e[0m"
  echo "  --------------------------------------------------------------------------------------"
  echo -e "  Build Environment:"
  echo -e "    JAVA_HOME=\e[35m$JAVA_HOME\e[0m"
  echo -e "    ANT_HOME=\e[35m$ANT_HOME\e[0m"
  echo -e "    CVS_ROOT=\e[35m$CVS_ROOT\e[0m"
  echo -e "    cvs logfile=\e[35m$CVS_LOG\e[0m"
  echo "  --------------------------------------------------------------------------------------"
  if [ ! "$SKIPPUBLISH" = "true" ] ; then
    echo -e "  -overwite        : Fail if version exists "$(printBoolean $( negation $OVERWRITE))
  fi
  echo -e "  -skipTest        : Run tests "$(printBoolean $RUNTESTS)
  echo -e "  -skipPublish     : Publish result (to updatesite and to download directory) "$(printBoolean $(negation $SKIPPUBLISH))
  echo -e "  -skipTaggingCvs  : Tag cvs projects "$(printBoolean $(negation $SKIPTAGCVS))
  if [ "$FORCE_TAGCVS" = "true" ] ; then
    echo -e "  -forceTaggingCvs : Force tag cvs projects \e[31mWARNING: the previous release will be overwritten!\e[0m"
  fi
  echo -e "  -noCvs           : Use cvs "$(printBoolean $(negation $NOCVS))
  if [ -n "$BRANCH" ] ; then
    if [ "$SKIPTAGCVS" = "true" ] ; then
      # there is an assert wich checks this case
      echo -e "  -useBranch       : use previous tagged sources (tag: \e[35m$FETCH_TAG\e[0m)"
    else
      echo -e "  -useBranch       : Build using cvs branch \e[32m$BRANCH\e[0m"
    fi
  else
    if [ "$SKIPTAGCVS" = "true" ] ; then
      echo -e "  -useBranch       : Use previous tagged sources (tag: \e[35m$FETCH_TAG\e[0m)"
    else 
      echo -e "  -useBranch       : None, use \e[35mHEAD\e[0m"
    fi
  fi
  if [ -n "$BUILDPRODUCT" ] ; then
    echo -e "  -buildProduct    : Build product \e[35m$BUILDPRODUCT\e[0m"
  fi
  if [ ! "$CUSTOM_BUILD" = "NONE" ] ; then
    echo -e "  -customBuild     : Custom build \e[35m$CUSTOM_BUILD\e[0m"
  fi
  echo -e "  -projectsrootdir : Checkout/source directory \e[35m$PROJECTSROOTDIR\e[0m"
  echo -e "  -workingdir      : Work directory \e[35m$WORKINGDIR\e[0m"
  if [ ! "$SKIPPUBLISH" = "true" ] ; then
    echo    "  Published result"
    echo -e "  -resultDir       : Result/Download directory \e[35m$PUBLISH_DOWNLOAD_DIR\e[0m"
    echo -e "  -updatesiteDir   : Updatesite directory \e[35m$PUBLISH_UPDATESITE_DIR\e[0m"
    echo -e "  -updatesiteFile  : Updatesite Filename \e[35m$PUBLISH_UPDATESITE_FILE\e[0m"
  fi
  echo "  --------------------------------------------------------------------------------------"
  echo -e "=> Start release build (\e[33my\e[0m)es? <="
  echo 
  read ANSWER
  if [ ! "$ANSWER" = "y" ]
   then echo "Cancel"; exit 1
  fi
}

assertBundleVersionAndMigrationClass()
{
 ### NEW in Version 3.2:
 ### Migration Strategy has changed in version 3.2! We do not need a migration strategy for every version anymore
 ### So the migration check have been removed!


  # assert correct bundle version in core plugin
  #   the bundle version stored in the core plugin must be equal to the given version
  if [ ! "$NOCVS" = "true" ] ; then
      # Note: if skipTaggingCvs is used then this assert may be check the wrong versions
      #       because only the latest file will be checked, because the tagging will be performed later
      #  checkout core plugin and check bundle version
      TMP_CHECKOUTDIR1=$PROJECTSROOTDIR/tmp_release_build1
      TMP_CHECKOUTDIR2=$PROJECTSROOTDIR/tmp_release_build2
      
      checkoutModule $TMP_CHECKOUTDIR1 $FETCH_TAG $FAKTORIPS_CORE_PLUGIN_NAME/META-INF $BRANCH
      
      CORE_BUNDLE_VERSION=$(cat $TMP_CHECKOUTDIR1/MANIFEST.MF | grep Bundle-Version | sed -r "s/.*:\ *(.*)/\1/g")

      rm -r $TMP_CHECKOUTDIR1
  else
      #  read bundle version from the core project stored in the projectsrootdir
      CORE_BUNDLE_VERSION=$(cat $PROJECTSROOTDIR/$FAKTORIPS_CORE_PLUGIN_NAME//META-INF/MANIFEST.MF | grep Bundle-Version | sed -r "s/.*:\ *(.*)/\1/g")
  fi

  # compare bundle version with given release version
  if [ ! "$CORE_BUNDLE_VERSION" = "$BUILD_VERSION" ]
    then 
      echo "=> Cancel build: wrong bundle version in plugin '$FAKTORIPS_CORE_PLUGIN_NAME', found '$CORE_BUNDLE_VERSION', but expected '$BUILD_VERSION'"
      echo "   update the core bundle version or restart the release build with version '"$CORE_BUNDLE_VERSION"'"
      exit 1
  fi 
}

createReleaseProperty()
{
  echo "# written by $0" > $RELEASE_PROPERTIES
  echo "# $(date)" >> $RELEASE_PROPERTIES
  echo buildType= >> $RELEASE_PROPERTIES
  echo version=$VERSION  >> $RELEASE_PROPERTIES
  echo fetchTag=$FETCH_TAG  >> $RELEASE_PROPERTIES
  echo version.qualifier=$VERSION_QUALIFIER  >> $RELEASE_PROPERTIES
  echo buildTypePresentation=  >> $RELEASE_PROPERTIES
  
  # 4. checkin (add+commit) generated release.properties
  if [ ! "$NOCVS" = "true" ] ; then
    addAndCommit $RELEASE_PROPERTIES $FETCH_TAG
  fi
}

tagProjects()
{
  if [ "$SKIPTAGCVS" = "true" ] ; then
    return
  fi

  # a) tag pluginbuilder project 
  tagProject $FETCH_TAG $PLUGINBUILDER_PROJECT_NAME $BRANCH
  
  # b) tag integrationtest projetcs
  for project in ${INTEGRATIONTEST_PROJECTS[*]} ; do
    tagProject $FETCH_TAG $project $BRANCH
  done

  # c) tag customer product project
  tagProject $FETCH_TAG $CUSTOMER_PRODUCT_PROJECT $BRANCH

  # c) tag devtarget project
  tagProject $FETCH_TAG $DEVTARGET_PLUGIN_NAME $BRANCH

  # d) tag all projects specified in the pluginbuilder map file (all necessary plugin and feature projects)
  # use custom-build/ci/all.map, because this map file contains all features and plugins
  echo "tag plugins and features defined in: "$PLUGINBUILDER_PROJECT_DIR/custom-build/ci/all.map
  checkoutModule $PLUGINBUILDER_PROJECT_DIR/custom-build $FETCH_TAG $PLUGINBUILDER_PROJECT_NAME/custom-build $BRANCH
  for project in $( cat $PLUGINBUILDER_PROJECT_DIR/custom-build/ci/all.map | sed -r "s/.*COPY,@WORKSPACE@,(.*)/\1/g" ) ; do
    tagProject $FETCH_TAG $project $BRANCH
  done
}

createIndexHtml()
{
  echo "publish: generateIndexHtml"
  LINK_PREFIX="http://update.faktorzehn.org/faktorips/downloads"
  # create index.html on category page 
  generateIndexHtml $LINK_PREFIX $PUBLISH_DOWNLOAD_DIR $BUILD_CATEGORY_PATH
  # create index.html on main page
  generateIndexHtml $LINK_PREFIX $PUBLISH_DOWNLOAD_DIR
  echo "  finished, result: "$LINK_PREFIX"/"$BUILD_CATEGORY_PATH"/index.html"
}

createAndAddLicensePdf(){
  # $1 text file containing filenames or direcory names of all archives files 
  local ARCHIV_FILES=$1
  # ! "$SKIPPUBLISH" = "true" -a
  if [ -f $ARCHIV_FILES ] ; then
    $PROJECTSROOTDIR/$CREATE_LIZENZ_SCRIPT $BUILD_VERSION
    for i in $(cat $ARCHIV_FILES) ; do
      if [ -d $i ] ; then
        # special case add license to all files in given directory
        for j in $(find $i -iname "*.zip") ; do
          addLicensePdfTo $j $PROJECTSROOTDIR/$LIZENZ_PDF 
        done
      else
        addLicensePdfTo $i $PROJECTSROOTDIR/$LIZENZ_PDF 
      fi
    done
  fi
}

addLicensePdfTo(){
  # $1 archive filename
  # $2 license pdf
  local ARCHIVE_FILE=$1
  local PDF_FILE=$2
  if [ ! -f $ARCHIVE_FILE ] ; then 
    # file not found 
    # maybe because of skiped publish step
    return
  fi
  echo "add license to: "$ARCHIVE_FILE
  zip -ujD $ARCHIVE_FILE $PDF_FILE > /dev/null 2>&1
  local RC=$?
  ## note: the Datenschutzbestimmung is not necessary in the archive file
  ## zip -ujD $i $PROJECTSROOTDIR/$DATENSCHUTZ_PDF > /dev/null 2>&1
  if [ ! $RC -eq 0 ] ; then
    echo "error adding lizenz.pdf or datenschutz.pdf to archive: "$i
    exit 1
  fi
}

patchAllCvsMap()
{
  if [ ! -f $PLUGINBUILDER_PROJECT_DIR/maps/all.map ] ; then
  	echo "Error map file not exists: "$PLUGINBUILDER_PROJECT_DIR/maps/all.map
  	exit 1
  fi
  # if using a branch then the all_cvs.map file must be patched (the branch name must be used instead of HEAD), 
  #   -> if no cvs is used then no patching is necessary (but this is not supported, see assert below)
  NOBRANCH=true
  if [ -n "$BRANCH" -a ! "$NOCVS" = "true" ] ; then
    # replace HEAD with given BRANCH
    echo "patch all_cvs.map: add branch"
    NOBRANCH=false
    cat $PLUGINBUILDER_PROJECT_DIR/maps/all.map | sed -r "s|(.*)HEAD(.*)|\1$BRANCH\2|g" > $PLUGINBUILDER_PROJECT_DIR/maps/all_branch.map
  fi

  # if using a different cvs root
  if [ ! "$DEFAULT_CVS_ROOT" = "$CVS_ROOT" ] ; then
    # patch cvs root in map file
    echo "patch all.map: change cvs root"
    cat $PLUGINBUILDER_PROJECT_DIR/maps/all.map | sed -r "s|(.*)$DEFAULT_CVS_ROOT(.*)|\1$CVS_ROOT\2|g" > $PLUGINBUILDER_PROJECT_DIR/maps/all_different_cvsroot.map  
  fi
}

assertProductConfigurationExists()
{
  #if running the product build then the corresponding FaktorIps.product file must be checked out
  #   -> if no cvs is used then no checkout is necessary, because all projects must be checked out in the projectsrootdir 
  if [ ! "$NOCVS" = "true" ] ; then
    checkoutModule $PROJECTSROOTDIR/$BUILDPRODUCT $FETCH_TAG $BUILDPRODUCT/FaktorIps.product
    if [ ! -f $PROJECTSROOTDIR/$BUILDPRODUCT/FaktorIps.product ] ; then
      echo "=> Cancel build: product not found! '"$BUILDPRODUCT/FaktorIps.product"'"
      exit 1
    fi
  fi
}

checkAndCreateReleaseProperty()
{
  RELEASE_PROPERTIES_EXISTS=false
  if [ -f $RELEASE_PROPERTIES ] ; then
    RELEASE_PROPERTIES_EXISTS=true
  fi

  # asserts for release builds (not active if product build, product exists will be checked in the ant build file)
  if [ ! "$SKIPTAGCVS" = "true" -a  ! "$OVERWRITE" = "true" -a "$RELEASE_PROPERTIES_EXISTS" = "true" -a -z "$BUILDPRODUCT" ] ; then 
    echo "=> Cancel build: release already exists ("$RELEASE_PROPERTIES")"
	echo "   delete the previous release build or use parameter -overwrite"
    exit 1
  fi

  # TODO Assert1 check property file in all branches and HEAD!
  # otherwise a build with tagging and no info about branches will move the release tag
  # from the branch to the head
  
  # assert new release property 
  #    - if release property already exists then tagging Cvs is not allowed, must be deleted manually
  if [ "$RELEASE_PROPERTIES_EXISTS" = "true" -a ! "$SKIPTAGCVS" = "true" -a ! "$FORCE_TAGCVS" = "true" ] ; then
    echo "=> Cancel build: tagging is not allowed if the release already exists!"
    echo "   Please use -skipTaggingCvs or remove the release properties in the pluginbuilder project and try again."
    echo "   Optional you can use -forceTaggingCvs to move the tag, but notice that the previous release will be overwritten!"
    echo "   Existing file: "$RELEASE_PROPERTIES
    exit 1
  fi

  # create release property file if not exists
  if [ "$RELEASE_PROPERTIES_EXISTS" = "false" ] ; then
    createReleaseProperty
  else
    echo "Skip creating release property, file already exists. "$RELEASE_PROPERTIES
  fi
}

checkoutPluginbuilderPartsAndDevtarget()
{
  # checkout pluginbuilder parts
  local _FETCH_TAG=$FETCH_TAG
  if [ "$NOCVS" = "true" ] ; then
    return
  fi
  
  if [ -d $PLUGINBUILDER_PROJECT_DIR ] ; then
    # delete previous checkout dir
    rm -r $PLUGINBUILDER_PROJECT_DIR
    mkdir $PLUGINBUILDER_PROJECT_DIR
  fi
	
  # checkout release.properties in branch or head
  if [ ! "$SKIPTAGCVS" = "true" ] ; then 
    _FETCH_TAG=LATEST
  fi
  checkoutModule $RELEASE_PROPERTY_DIR $_FETCH_TAG $PLUGINBUILDER_PROJECT_NAME/releases $BRANCH
  checkoutModule $PLUGINBUILDER_PROJECT_DIR/maps $_FETCH_TAG $PLUGINBUILDER_PROJECT_NAME/maps $BRANCH
  checkoutModule $PLUGINBUILDER_PROJECT_DIR/lizenz $_FETCH_TAG $PLUGINBUILDER_PROJECT_NAME/lizenz $BRANCH

  # checkout devtarget
  DEVTARGET_PLUGIN_PATH=$PROJECTSROOTDIR/$DEVTARGET_PLUGIN_NAME
  checkoutModule $DEVTARGET_PLUGIN_PATH $_FETCH_TAG $DEVTARGET_PLUGIN_NAME $BRANCH
  
  # special case for custom build aok
  if [ "$CUSTOM_BUILD" = "aok" ] ; then
    checkoutModule $DEVTARGET_PLUGIN_PATH/eclipse/dropins/aok $_FETCH_TAG de.aoksystems.omc.tools.feature/dropins $BRANCH
  fi
}

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

######
# init
######

parseArgs $*

initDefaultParameter

initEnvironment

############################
# check and assert parameter 
############################

doAsserts

getFetchTagVersion $BUILD_VERSION

assertValidParameters

showParameter

assertBundleVersionAndMigrationClass

#############################
# perform the pre build steps
#  - tag project
#  _ create release properties
#############################

# change to working directory
if [ ! -d $WORKINGDIR ] ; then 
  mkdir -p $WORKINGDIR
fi
cd $WORKING_DIR

checkoutPluginbuilderPartsAndDevtarget

# assert license script in pluginbuilder project
if [ ! -f $PROJECTSROOTDIR/$CREATE_LIZENZ_SCRIPT ] ; then
  echo "error: the create license script not exists: "$PROJECTSROOTDIR/$CREATE_LIZENZ_SCRIPT
  exit 1
fi   

checkAndCreateReleaseProperty

# 3. tag all projects defined in the pluginbuilder project (move tag if already exists)
#    if skip tag is true then don't tag project, the previous tagged versions are used for the build
tagProjects

if [ -n "$BUILDPRODUCT" ] ; then
  assertProductConfigurationExists
fi

# update all.map onfiguration (eclipse feature and plugin projects path location)
patchAllCvsMap

#################################################
# call ant to perform the specified release build
#################################################
BUILD_CATEGORY_PATH="faktorips-"$BUILD_CATEGORY
DOWNLOAD_DIR=$PUBLISH_DOWNLOAD_DIR"/"$BUILD_CATEGORY_PATH

# create download dir if not exists
if [ ! "$SKIPPUBLISH" = "true" ] ; then
  test -d $DOWNLOAD_DIR || mkdir $DOWNLOAD_DIR
fi 

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
 -DdownloadDir=$DOWNLOAD_DIR \
 -Dupdatesite.path=$PUBLISH_UPDATESITE_DIR \
 -DsiteXmlFileName=$PUBLISH_UPDATESITE_FILE \
 -DproductProject=$BUILDPRODUCT \
 -DcustomBuild=$CUSTOM_BUILD \
 -DnoBranch=$NOBRANCH \
 -Dcvsroot=$CVS_ROOT \
 -DminVersion3=$MIN_VERSION_3
 -Ddevtarget.jar.path=$DEVTARGET_PLUGIN_PATH/eclipse/dropins/deploy/eclipse
 "
echo $EXEC
eval $EXEC
RC=$(echo $?)

####################################################
# add lizenz to all generated and published archives
####################################################
echo ''

if [ -n "$BUILDPRODUCT" ] ; then
  createAndAddLicensePdf $WORKINGDIR/archives_products
else
  createAndAddLicensePdf $WORKINGDIR/archives_features
fi

echo "OK: release build "${BUILD_VERSION}" successful"

