#echo using RDT_BUILD_HOME: ${RDT_BUILD_HOME:?must be set}
eclipseDir=/usr/lib/eclipse-3.1
pdeBuildPluginVersion=3.1.0
buildDirectory=/home/senf/work/
bootstrapDir=${buildDirectory}/org.faktorips.build/bootstrap
verboseAnt=true
#eclipseAutomatedTestHome=${RDT_BUILD_HOME}/eclipse-testing

#docbookRoot=${RDT_BUILD_HOME}/docbook
vm=`java-config -O`/bin/java

os=-linux
ws=-gtk
arch=-x86
#usePserver=${RDT_USE_PSERVER:+-DusePserver=true}
#testClean=${RDT_TEST_CLEAN:+-Dclean=true}
#dontRunTests=${RDT_DONT_RUN_TESTS:+-DdontRunTests=true}

#REM reset ant command line args
ANT_CMD_LINE_ARGS=


buildfile=$eclipseDir/plugins/org.eclipse.pde.build_$pdeBuildPluginVersion/scripts/build.xml

echo Starting eclipse in $eclipseDir, $vm
cmd="$vm -cp $eclipseDir/startup.jar org.eclipse.core.launcher.Main -ws $ws -os $os -application org.eclipse.ant.core.antRunner -verbose  -buildfile $buildfile -data $buildDirectory/workspace $verboseAnt $usePserver $dontRunTests -Dbasews=$ws -Dbaseos=$os -Dbasearch=$arch -Dbuilder=$bootstrapDir  $testClean  -DjavacFailOnError=true -DbuildDirectory=$buildDirectory -DbaseLocation=$eclipseDir  -DeclipseAutomatedTestHome=$eclipseAutomatedTestHome -Drdt.rubyInterpreter="$rubyInterpreter" -Drdt-tests-workspace=$buildDirectory/workspace-rdt-tests -Ddocbook.root=$docbookRoot"
echo $cmd
exec $cmd