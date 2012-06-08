#!/bin/bash
set -e
#set -x

parseArgs()
{
	while [ $# != 0 ]
	do case "$1" in
	  -checkoutDir)	  CHECKOUT_DIR=$2 ; shift ;;
	  -gitUser)       GIT_USERNAME=$2 ; shift ;;
	  -gitBranch)	  GIT_BRANCH=$2 ; shift ;;
	  -gitHost)	  GIT_HOST=$2 ; shift ;;
	  -gitRepository) GIT_REPOSITORY=$2 ; shift ;;
	  -gitUrl)	  GIT_URL=$2 ; shift ;;
	  -versionType)   VERSION_TYPE=$2 ; shift ;;
	  -mavenCmd)	  MAVEN_CMD=$2 ; shift ;;
	  -pom)		  BUILD_POM=$2 ; shift ;;
	  -skipTag)	  SKIP_TAG=true ;;
	  -skipTests)	  SKIP_TESTS=true ;;
	  -newVersion)   NEW_VERSION=$2 ; shift ;;
	  -onlySetVersion) ONLY_VERSION=true ;;

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
	CHECKOUT_DIR=${CHECKOUT_DIR:-'checkoutDir'}
	BUILD_POM=${BUILD_POM:-'org.faktorips.build/pom.xml'}
	MAVEN_CMD=${MAVEN_CMD:-'mvn'}
	GIT_USERNAME=${GIT_USERNAME:-$USER}
	GIT_BRANCH=${GIT_BRANCH:-'master'}
	GIT_HOST=${GIT_HOST:-'projekte.faktorzehn.de'}
	GIT_REPOSITORY=${GIT_REPOSITORY:-'/projekte/faktorips/faktorips.base.git'}
	GIT_URL=${GIT_URL:-'ssh://'${GIT_USERNAME}'@'${GIT_HOST}${GIT_REPOSITORY}}
	VERSION_TYPE=${VERSION_TYPE:-'rfinal'}
	ONLY_VERSION=${ONLY_VERSION:-'false'}
	SKIP_TAG=${SKIP_TAG:-'false'}
	SKIP_TESTS=${SKIP_TESTS:-'false'}
}

doAsserts()
{
	if [ -z "$NEW_VERSION" ]
	then
		echo "Please specify next version with -nextVersion x.x.x"
	fi
}

getVersionAndTag()
{
	BUILD_VERSION=`sed -n '0,/.*<version>\(.*\)<\/version>/s//\1/p' $BUILD_POM`
 	TAG=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)-SNAPSHOT/v\1_\2_\3_${VERSION_TYPE}/g")
	assertVersionFormat
}

assertVersionFormat ()
{
	if [ -z "$BUILD_VERSION" ] ; then
		echo "No version found in pom.xml"
		exit 1
	fi

 FAIL_MESSAGE="Wrong release version format '$BUILD_VERSION', must be tree numbers followed by -SNAPSHOT (major.minor.micro-SNAPSHOT), e.g. 3.8.0-SNAPSHOT."

 version_test=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)\-SNAPSHOT/\1/g")
 if [ $version_test = $BUILD_VERSION ]
 then
   echo $FAIL_MESSAGE
   exit 1	
 fi
 POINTEXISTS=$( echo $BUILD_VERSION | grep "\." | wc -l )
 if [ -z $BUILD_VERSION -o -z $TAG -o "$BUILD_VERSION" = "$TAG" -o "$POINTEXISTS" -eq 0 ] ; then
   echo $FAIL_MESSAGE
   exit 1
 fi 
}

showUsageAndExit()
{
	echo "Erlaubte parameter..."
	exit 1
}

showParameter()
{	 
  echo 
  echo Release build parameter:
  echo "  --------------------------------------------------------------------------------------"
  echo -e "  Release Version=\e[35m$BUILD_VERSION\e[0m"
  echo -e "  Version Type=\e[35m$VERSION_TYPE\e[0m"
  echo -e "  POM=\e[35m$BUILD_POM\e[0m"
  echo -e "  Git URL=\e[35m$GIT_URL\e[0m"
  echo "  --------------------------------------------------------------------------------------"
    if [ $SKIP_TAG ] ; then
      echo -e "  -skipTag         : Skip tagging git repository"
    else 
      echo -e "  -skipTag         : Tag git repository with \e[35m${TAG}\e[0m"
    fi
  echo "  --------------------------------------------------------------------------------------"
  echo -e "=> Start release build (\e[33my\e[0m)es? <="
  echo 
  read ANSWER
  if [ ! "$ANSWER" = "y" ]
   then echo "Cancel"; exit 1
  fi
}

setVersion()
{
	echo -e "\nSetting new Version ${NEW_VERSION}\n"
	$MAVEN_CMD -f ${BUILD_POM} org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=${NEW_VERSION}-SNAPSHOT
	git commit */pom.xml */META-INF/MANIFEST.MF -m "Setting new version ${NEW_VERSION}"
	git push
}

parseArgs $*
initDefaults
doAsserts

# CHECKOUT
if [ -d $CHECKOUT_DIR ]
then
	echo -e "\n\nThe folder ${CHECKOUT_DIR} already exists. The script assumes it was created in a previous build. Overwrite all changes and continue? (y/n) "
	read ANSWER
	if [ ! "$ANSWER" = "y" ]
	then
		echo "Cancel"; exit 1
	fi
	cd ${CHECKOUT_DIR}
	git checkout ${GIT_BRANCH}
	git reset --hard
	#git pull
else
	echo -e "\nClone git branch: ${GIT_BRANCH}\n"
	git clone $GIT_URL -b $GIT_BRANCH ./${CHECKOUT_DIR}
	cd ${CHECKOUT_DIR}
fi

# START DOING ANYTHING USEFUL

if [ ONLY_SET_VERSION ]
then
	setVersion
else

	getVersionAndTag

	showParameter

	if [ ! $SKIP_TAG ]
	then
		echo -e "\nTagging git with Tag: ${TAG}\n"
		git tag -a $TAG -m 'Tag for '$VERSION_TYPE' Version: '$BUILD_VERSION
		git push origin $TAG
	fi

	MAVEN_OPTIONS='-Dbuild-type='$VERSION_TYPE
	if [ $SKIP_TEST ]
	then
		MAVEN_OPTIONS=${MAVEN_OPTIONS}' -Dmaven.test.skip=true'
	fi

	echo -e "\nBuild...\n"
	${MAVEN_CMD} -f $BUILD_POM $MAVEN_OPTIONS clean install

	setVersion

fi

