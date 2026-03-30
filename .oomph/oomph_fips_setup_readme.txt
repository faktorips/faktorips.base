

1. Oomph installer herunterladen/installieren: https://eclipse.dev/oomph/?file=Eclipse_Installer.md#milestone-installers

2. "faktor_ips_dev_eclipse_configuration.setup" in die Zwischenablage kopieren

3. Installer öffnen und im Burger-Menü rechts oben "Apply Configuration" auswählen

4. Im Dialog "Switch to Advanced Mode" wählen

5. Variablen beliebig angeben*
   Anmerkung: Wird ein vorhandenes FIPS-git-repo als pfad angegeben, wird das vorhandene Repo als Grundlage des Projektimports genutzt und nicht erneut geklont.

6. Next -> Finish -> Installation Startet

7. Eclipse startet im Anschluss selbstständig und weitere Tasks werden durch oomph ausgeführt: u. a. wird das git repo geklont, die Projekte gebaut und importiert und Settings gesetzt.

8. Im Anschluss muss ggf noch manuell die preferences.epf importiert und gewünschte workspace/workbench-settings gesetzt werden. Ggf. muss auch nochmals die target-platform neu gesetzt werden.