dieses verzeichnis enthält die grundlegende cc-konfiguration.

datei				erläuterung

config.xml			zentrale konfigurationsdatei - hier werden die projekte definiert
build-faktorips.xml zentrales ant-buildfile, welches den checkout macht und anschliessend
					projektspezifische ant-builder aufruft.
					
					
die dateien in diesem verzeichnis werden per cron-job alle 5min aus dem cvs aktualisiert
(in /opt/cc/work/skel ) und sind von dort ins work-verzeichniss verlinkt.

Cruisecontrol muss nicht neugestartet werden, damit die Änderungen übernommen werden.
Die Reaktionszeit liegt also bei ca. 5-7 Minuten.