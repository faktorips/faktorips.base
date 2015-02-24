#!/bin/bash
BASEDIR=${1:-./}

IGNORE=${2:-org.faktorips.p2repository/product/pom.xml}

cd $BASEDIR
POMFILES=`find . -name "pom.xml"`
GITSTATUS=`git status -s`

FEATURES=
PLUGINS=
OTHERS=

check() {
	FILE=$1
	if [[ ! $IGNORE == *$FILE* ]]
	then
#		git status -s | grep "M $FILE"
		echo $GITSTATUS | grep "M $FILE" > /dev/null
		if [ $? -ne 0 ]
		then
			echo "$FILE not changed"
			exit 1
		fi
	fi
}

for POMFILE in $POMFILES
do
	RELDIR=`dirname $POMFILE`
	DIR=`echo $RELDIR | cut -d"/" -f2-`
	if [ -e $DIR/feature.xml ]
	then
		FEATURES="$FEATURES $DIR"
	elif [ -e $DIR/META-INF/MANIFEST.MF ]
	then
		PLUGINS="$PLUGINS $DIR"
	else
		OTHERS="$OTHERS $DIR"
	fi
done

for FEATURE in $FEATURES
do
	check "${FEATURE}/feature.xml"
	check "${FEATURE}/pom.xml"
done

for PLUGIN in $PLUGINS
do
	check "${PLUGIN}/META-INF/MANIFEST.MF"
	check "${PLUGIN}/pom.xml"
done

for P2DIR in $OTHERS
do
	check "${P2DIR}/pom.xml"
done

echo "EVERYTHING SEEMS TO BE FINE"
