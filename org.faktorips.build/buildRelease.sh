#!/bin/bash
TAG=
USERNAME=$USER
REPOSITORY=ssh://${USERNAME}@projekte.faktorzehn.de/projekte/faktorips/faktorips.base.git
BRANCH=master
CREATE_BRACH=
NEXT_VERSION_IN_BRANCH=
NEXT_VERSION=

# integrity check

# check TAG set
# check NEXT_VERSION set
# check NEXT_VERSION_IN_BRANCH set when CREATE_BRANCH

# checkout
git clone ${REPOSITORY} -branch ${BRANCH}
git tag ${TAG}
git push

mvn clean install deploy-updatesite

if [ CREATE_BRANCH ]
then
	git branch ${CREATE_BRACH}
fi

if [ NEXT_VERSIOIN ]
then
	git tycho-version-plugin
fi
