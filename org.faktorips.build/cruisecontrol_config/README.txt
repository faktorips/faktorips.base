dieses verzeichnis enthält die grundlegende cc-konfiguration.
und sonstige build scripte

datei				    erläuterung

config.xml			    zentrale konfigurationsdatei - hier werden die projekte definiert
                        ->die config.xml Datei existiert hier nicht mehr<- 
                        da sie auf dem server jedesmal neu aus dem 
faktorips.projects.xml  und 
mailmapping.txt
                        Files generiert wird, möchte man trotzdem sehen wie toll die config.xml
                        Datei aussieht kan man das 
generate.cc.config.xml  ant script manuell ausfuehren
                    
build-faktorips.xml     zentrales ant-buildfile, welches den checkout macht und anschliessend
				    	projektspezifische ant-builder aufruft.
				    	
build_crontab.sh        script welches über crontab aufgerufen wird um die config.xml datei zu
                        generieren

releaseFaktorIps.sh     Hilfs-Script zum Bau von FaktorIps Releases und Hilfe beim Erstellung von
                        Branches
					
die dateien in diesem verzeichnis werden per cron-job alle 5?min aus dem cvs aktualisiert
(in /opt/cc/work/skel ) und sind von dort ins work-verzeichniss verlinkt.

Cruisecontrol muss nicht neugestartet werden, damit die Änderungen übernommen werden.
Die Reaktionszeit liegt also bei ca. 5-7? Minuten.
