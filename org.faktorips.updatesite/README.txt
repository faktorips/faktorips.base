Hier ist die site.xml der Update-Seite gespeichert.
Plugins und Features werden direkt, waehrend des Release bauens, in das Verzeichnis der Updateseite kopiert.

Url der Update-Seite: http://update.faktorzehn.org/faktorips 
Server-Pfad: /var/www/localhost/htdocs/update.faktorzehn.org/faktorips/org.faktorips.updatesite/
             hier sind allerdings nur die symbolischen Links zu der site.xml und den Verzeichnissen features und plugins
             im Verzeichnis:
                /var/www/localhost/htdocs/update.faktorzehn.org/faktorips/org.faktorips.updatesite/
             
Sollte die site.xml manuell angepasst werden, kann sie in diesem Projekt geaendert/eingecheckt werden,
und anschliessend manuell im Updatesite Verzeichnis auf dem Server (mittels CVS) geupdated werden.

als Cruise User:
cvs -d /usr/local/cvsroot co -d /var/www/localhost/htdocs/update.faktorzehn.org/faktorips/org.faktorips.updatesite/  org.faktorips.updatesite/site.xml
