#!/bin/bash

parseArgs()
{
	while [ $# != 0 ]
	do case "$1" in
	  -gitUser)       GIT_USERNAME=$2 ; shift ;;
	  -gitBranch)	  GIT_BRANCH=$2 ; shift ;;
	  -gitHost)	  GIT_HOST=$2 ; shift ;;
	  -gitRepository) GIT_REPOSITORY=$2 ; shift ;;
	  -gitUrl)	  GIT_URL=$2 ; shift ;;
	  -buildVersion)  BUILD_VERSION=$2 ; shift ;;
	  -pom)		  BUILD_POM=$2 ; shift ;;

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
      -devtarget)     DEVTARGET_PLUGIN_NAME=$2 ; shift ;;
      -addDropins)    COPY_DROPINS_FROM=$2 ; shift ;;
      -corePlugin)    FAKTORIPS_CORE_PLUGIN_NAME=$2 ; shift ;;
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
}

initDefaults()
{
	BUILD_POM=${BUILD_POM:-'org.faktorips.build/pom.xml'}
	GIT_USERNAME=${GIT_USERNAME:-$USER}
	GIT_BRANCH=${GIT_BRANCH:-'master'}
	GIT_HOST=${GIT_HOST:-'projekte.faktorzehn.de'}
	GIT_REPOSITORY=${GIT_REPOSITORY:-'/projekte/faktorips/faktorips.base.git'}
	GIT_URL=${GIT_URL:-'ssh://'${USERNAME}'@'${GIT_HOST}${GIT_REPOSITORY}}
 	TAG=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/v\1_\2_\3_\4/g")
}

doAsserts()
{
	if [ -z "$BUILD_VERSION" ] ; then
		echo "Parameter -buildVersion muss angeben welche Version gebaut werden soll"
		showUsageAndExit
	fi

	assertVersionFormat $BUILD_VERSION $TAG "Wrong release version format '$INPUT_VERSION', must be tree numbers followed by the qualifier (major.minor.micro.qualifier), e.g. 3.8.0.rfinal"
}

assertVersionFormat ()
{
 VERSION=$1 
 VERSION_QUALIFIER=$(echo $INPUT_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\.(.*)/\4/g")
 FETCH_TAG=$2
 FAIL_MESSAGE=$3
 POINTEXISTS=$( echo $VERSION | grep "\." | wc -l )
 if [ -z $VERSION -o -z $FETCH_TAG -o "$VERSION" = "$FETCH_TAG" -o "$POINTEXISTS" -eq 0 ] ; then
   echo $FAIL_MESSAGE
   exit 1
 fi
}

showUsageAndExit()
{
	Erlaubte parameter...
	exit 1
}

parseArgs $*
initDefaults
doAsserts

echo "Clone git branch: "$GIT_BRANCH
git clone $GIT_URL -b $GIT_BRANCH
echo "Tagging git with Tag: "$TAG
git tag $TAG
git push

mvn3 -f $BUILD_POM clean install 
