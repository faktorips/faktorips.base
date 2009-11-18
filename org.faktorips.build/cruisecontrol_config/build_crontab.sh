#!/bin/bash
#
# Script to update (generate) the cruise control configuration
#
CVS_ROOT=/usr/local/cvsroot
CVS_CHECKOUT=/opt/cc/work/skel/
CVS_CRUISE_CONTROL_SOURCE_LOCATION=org.faktorips.build/cruisecontrol_config/

ANT_HOME=/opt/cc/apache-ant-1.6.5

cvsUpdate()
{
  cvs -d $CVS_ROOT co -d $CVS_CHECKOUT $CRUISE_CONTROL_SOURCE_PATH >> /dev/null 2>&1
}

generateCruiseControlConfig()
{
  $ANT_HOME/bin/ant -buildfile $CVS_CHECKOUT/generate.cc.config.xml -Dskip.pretty.print=true
}

cvsUpdate

generateCruiseControlConfig
