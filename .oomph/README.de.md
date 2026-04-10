# Faktor-IPS Dev Setup mit Oomph

1. Den Oomph **Milestone Installer** herunterladen und installieren: https://eclipse.dev/oomph/?file=Eclipse_Installer.md#milestone-installers
2. Die URL von [`faktor_ips_dev_eclipse_configuration.setup`](faktor_ips_dev_eclipse_configuration.setup) kopieren
3. Installer öffnen, Burger-Menü (oben rechts) → **Apply Configuration** und die URL einfügen
4. Im Dialog **Switch to Advanced Mode** wählen
5. Variablen beliebig angeben
6. **Next → Finish** klicken — Installation startet
7. Eclipse startet selbstständig, Oomph führt die restlichen Tasks aus (Git-Clone, Projektimport, Build, Settings)
8. Bei Bedarf `preferences.epf` manuell importieren und die Target-Platform ggf. neu setzen

> **HINWEIS:**  Wird ein vorhandenes FIPS-Git-Repo als Pfad angegeben, wird dieses als Grundlage genutzt und nicht erneut geklont.
