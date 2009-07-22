das buildfile test.xml wird in den jeweiligen eclipsetest-environmentordner 
verlinkt. (zb. /opt/cc/work/eclipsetest/environment/)

im buildfile werden die per perl-skript generierten testsuiten aufgerufen.
einstiegspunkt ist das all-target


importProjects.xml importiert die darin angegebenen Projekte in einen Eclipse-
Workspace. Dazu muss das org.faktorips.devtools.ant plugin im Eclipse vorhanden sein,
da hierzu eine custom-Task benötigt wird. Dies ist u.a. notwendig, damit der IPSBuilder
gerufen werden kann, welcher Java-Sourcefiles generiert (integrationtest!)