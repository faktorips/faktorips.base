#!/usr/bin/perl


########
### START OF CONFIGURABLE AREA
########

my $binaryDir         = "../updateSite/";                                        #directory which contains the feature-build
my $updateSiteModule  = "org.faktorips.updatesite";                              #cvs-module which contains site.xml
my $cvsRoot           = "/usr/local/cvsroot/";                                   #cvsroot
my $projectName       = "org.faktorips.feature";                                 #name of feature

my $releasePublishDir = "/var/www/localhost/htdocs/update/faktorips/";                       #directory where release-updatesites are stored
my $develPublishDir   = "/var/www/localhost/htdocs/update/faktorips/devel/";                   #directory where successfull builds are stored
my $tempDir           = "/tmp/updatesite/";                                      #temporary directory
my $versionMarkerFile = "/etc/build/" . $updateSiteModule . "_versiontag";       #file which stores the versiontag (no changes needed)

########
### END OF CONFIGURABLE AREA
########



createDevelBuild();
createReleaseBuild();





#creates a developer-updatesite
sub createDevelBuild(){

	#get some settings
	my $version = getDevCount();  
	my $jarfile = getJarFile();

	#copy binaries
	copyToDir($develPublishDir);
	
	#write site.xml
	checkoutUpdateSite($develPublishDir);
	
	open(fbuf,"<$develPublishDir/site.xml");
	my @content = <fbuf>;
	close(fbuf);
	
	foreach $line (@content){
		if ($line =~ m/category-def/ig){
			$line =~ s/label="(.*?)"/label="\1-$version"/ig;
			$line =~ s/name="(.*?)"/name="\1-$version"/ig;
		}
	}
	
	unlink("$develPublishDir/site.xml");
	
	open(fbuf, ">$develPublishDir/site.xml");
	print fbuf @content;
	close(fbuf);
}

#creates a release-updatesite
sub createReleaseBuild(){

	my $sitexml = checkoutUpdateSite($tempDir);
	
	if (isReleaseBuild($sitexml)){
		#copy binaries
		copyToDir($releasePublishDir);
		#get site.xml
		checkoutUpdateSite($releasePublishDir);
		#save releaseversion
		
	}

}

#checks if current build is a release build
#returns: 1 or 0
sub isReleaseBuild(){
	my $file = $_[0];
	my $version = -1;
	
	#read site.xml
	open (fbuf, "<$file");
	my @sitexml = <fbuf>;
	close (fbuf);

	#get version from site.xml

	foreach $line (@sitexml){
		if ($line =~ m/feature/ig){
			$line =~ m/version="(.*?)"/ig;
			$version = $1;
			print "found feature version $version in line \"$line\"\n ";
			#only recognize the first feature
			last;
		}
    }
	#get last release version
	system("touch ". $versionMarkerFile);
	my $lastrelease = `cat $versionMarkerFile`;
	$lastrelease =~ s/\n//;

	#compare both
	if ($lastrelease ne $version){
		print "new release ".$version." detected\n"; 
		system ("echo ".$version." > ".$versionMarkerFile);
		return 1;
	}

	return 0;
}


#copy binaries
#parameter: target dir
sub copyToDir(){
	my $target = $_[0];
	my $query = "cp -rf ". $binaryDir . "/* ". $target;
	print "copying: ".$query ."\n";

	system ($query);
}

#returns the feature-jar path
sub getJarFile(){
	my $query = "find ". $binaryDir . " | grep feature | grep jar | grep -v test";
	my $path = `$query`;
	$path =~ s/\.\.\/updateSite\///;
	return $path;
}

#returns current version count and increments same.
#parameter: filename
sub getDevCount(){
	my ($Sekunden, $Minuten, $Stunden, $Monatstag, $Monat, $Jahr, $Wochentag, $Jahrestag, $Sommerzeit) = localtime(time);
	$Jahr = $Jahr + 1900;
	return  $Monatstag .  $Monat . $Jahr . $Stunden . $Minuten. $Sekunden ;
}

#fetches the site.xml from cvs to the given dir
#returns: 
sub checkoutUpdateSite(){
    my $dir = $_[0];
    my $query = "rm -rf ". $dir ."/CVS && rm -f ".$dir . "/site.xml && cvs -d ".$cvsRoot." checkout -d ".$dir. " ".$updateSiteModule."/site.xml";
    print "executing: $query\n";
    system($query);
    return $dir . "/site.xml";
}
