Nach: http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/ant_contributing_task.htm
dürfen die .class Dateien nicht in der Plugin Jar Datei enthalten sein, da sie sonst im Eclipse Classpath zur Laufzeit
enthalten sind. Dies wird erreicht indem in den Projekteinstellungen für den Source Folder /src ein gesonderter Output Folder
angegeben wird. Auf diesen wird in den build.properties beim bauen eines ant_tasks.jar verwiesen: output.ant_tasks/ant-tasks.jar = ant_tasks_bin/
Die Datei ant_tasks.jar wird in dem Extension Point org.eclipse.ant.core.extraClasspathEntries angegeben. Hierdurch wird
gewährleistet, dass die Instanzen der Klassen in dem ant_tasks.jar Zugriff auf die Eclipse-Laufzeitumgebung haben, umgekehrt
diese Klassen für die Eclipse-Laufzeitumgebung nicht sichtbar sind.

Startet man zu Testzwecken aus Eclipse heraus eine neue Eclipse Instanz so wird nicht automatisch vor dem Start die ant_tasks.jar
Datei neu gebaut. D.h. zur Entwicklungszeit ist es möglich dass die Sources und die .class Dateien in der ant_tasks.jar Datei nicht synchron sind.
Dazu muss das Projekt einmal mit mvn clean install gebaut werden (ggf. vorher auch org.faktorips.devtools.core).