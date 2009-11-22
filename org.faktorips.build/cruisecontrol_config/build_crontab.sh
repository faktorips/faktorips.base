#!/bin/bash
#
# Script to update (generate) the cruise control configuration
#
CVS_ROOT=/usr/local/cvsroot
CVS_CHECKOUT=/opt/cc/work/skel/
CVS_CRUISE_CONTROL_SOURCE_LOCATION=org.faktorips.build/cruisecontrol_config/

INPUT_FILES=(faktorips.projects.xml mailmapping.txt)
OUTPUT_FILE=config.xml

JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun
ANT_HOME=/opt/cc/apache-ant-1.6.5

cvsUpdate()
{
  cvs -d $CVS_ROOT co -d $CVS_CHECKOUT $CRUISE_CONTROL_SOURCE_PATH >> /dev/null 2>&1
}

generateCruiseControlConfig()
{
  $ANT_HOME/bin/ant -buildfile $CVS_CHECKOUT/generate.cc.config.xml generate.config.xml -Dskip.pretty.print=true -Dconfig.xml=$OUTPUT_FILE
}

#the update of this folder will be done using crontab
#cvsUpdate

CHANGE_FOUND="false"
for file in ${INPUT_FILES[*]} ; do
  echo "date check: $file"
  if [ $CVS_CHECKOUT/$OUTPUT_FILE -ot $CVS_CHECKOUT/$file ] ; then
    echo " -> change found"
    CHANGE_FOUND=true
  fi
done

if [ $CHANGE_FOUND = "true" ] ; then
  generateCruiseControlConfig
else
  echo "no change found"
fi

