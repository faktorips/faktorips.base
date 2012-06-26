#!/bin/bash
set -e
#set -x

parseArgs()
{
	while [ $# != 0 ]
	do case "$1" in
	  -checkoutDir)	  CHECKOUT_DIR=$2 ; shift ;;
	  -gitUser)       GIT_USERNAME=$2 ; shift ;;
	  -useBranch)	  BRANCH=$2 ; shift ;;
	  -gitHost)	  GIT_HOST=$2 ; shift ;;
	  -gitRepository) GIT_REPOSITORY=$2 ; shift ;;
	  -gitUrl)	  GIT_URL=$2 ; shift ;;
	  -versionType)   VERSION_KIND=$2 ; shift ;;
	  -mavenCmd)	  MAVEN_CMD=$2 ; shift ;;
	  -pom)		  BUILD_POM=$2 ; shift ;;
	  -skipTag)	  SKIP_TAG=true ;;
	  -skipTests)	  SKIP_TESTS=true ;;
	  -newVersion)   NEW_VERSION=$2 ; shift ;;
	  -onlySetVersion) ONLY_VERSION=true ;;
	  -noNewVersion)  NO_NEW_VERSION=true;;
	  -deployUser)	  DEPLOY_USER=$2 ; shift ;;
	  -deployServer)  DEPLOY_SERVER=$2 ; shift ;;
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
	BUILD_POM=${BUILD_POM:-'org.faktorips.aggregator/pom.xml'}
	MAVEN_CMD=${MAVEN_CMD:-'mvn'}
	GIT_USERNAME=${GIT_USERNAME:-$USER}
	BRANCH=${BRANCH:-'master'}
	GIT_HOST=${GIT_HOST:-'projekte.faktorzehn.de'}
	GIT_REPOSITORY=${GIT_REPOSITORY:-'/projekte/faktorips/faktorips.base.git'}
	GIT_URL=${GIT_URL:-'ssh://'${GIT_USERNAME}'@'${GIT_HOST}${GIT_REPOSITORY}}
	DEPLOY_USER=${DEPLOY_USER:-$USER}
	VERSION_KIND=${VERSION_KIND:-'rfinal'}
	ONLY_VERSION=${ONLY_VERSION:-''}
	NO_NEW_VERSION=${NO_NEW_VERSION:-''}
	SKIP_TAG=${SKIP_TAG:-''}
	SKIP_TESTS=${SKIP_TESTS:-''}
}

doAsserts()
{
	if [ -z "$NEW_VERSION" ]
	then
		echo "Please specify new version with -newVersion x.x.x or use argument -noNewVersion" 
	fi
}

getVersionAndTag()
{
	BUILD_VERSION=`sed -n '0,/.*<version>\(.*\)<\/version>/s//\1/p' $BUILD_POM`
 	TAG=$(echo $BUILD_VERSION | sed -r "s/([0-9]*)\.([0-9]*)\.([0-9]*)-SNAPSHOT/v\1_\2_\3_${VERSION_KIND}/g")
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
  echo -e "  Version Type=\e[35m$VERSION_KIND\e[0m"
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
	git commit -m "Setting new version ${NEW_VERSION}" -- */pom.xml */META-INF/MANIFEST.MF */feature.xml
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
	git reset --hard
	git checkout ${BRANCH}
	git pull
else
	echo -e "\nClone git branch: ${BRANCH}\n"
	git clone $GIT_URL -b $BRANCH ./${CHECKOUT_DIR}
	cd ${CHECKOUT_DIR}
fi

# START DOING ANYTHING USEFUL

if [ $ONLY_VERSION ]
then
	setVersion
else

	getVersionAndTag

	showParameter

	if [ -z $SKIP_TAG ]
	then
		echo -e "\nTagging git with Tag: ${TAG}\n"
		git tag -a $TAG -m 'Tag for '$VERSION_KIND' Version: '$BUILD_VERSION
		git push origin $TAG
	fi

	MAVEN_OPTIONS='-Dversion.kind='$VERSION_KIND' -Ddeploy.user='$DEPLOY_USER' -P release'
	if [ $DEPLOY_SERVER ]
	then
		MAVEN_OPTIONS=${MAVEN_OPTIONS}' -Ddeploy.server='$DEPLOY_SERVER
	fi
	if [ $SKIP_TESTS ]
	then
		MAVEN_OPTIONS=${MAVEN_OPTIONS}' -Dmaven.test.skip=true'
	fi

	echo -e "\nBuild...\n"
	${MAVEN_CMD} -f $BUILD_POM $MAVEN_OPTIONS clean deploy

	if [ -z ${NO_NEW_VERSION} ]
	then
		setVersion
	fi

fi

