# Faktor-IPS Dev Setup with Oomph

1. Download and install the Oomph **Milestone Installer**: https://eclipse.dev/oomph/?file=Eclipse_Installer.md#milestone-installers
2. Copy the URL of [`faktor_ips_dev_eclipse_configuration.setup`](faktor_ips_dev_eclipse_configuration.setup)
3. Open the installer, click the burger menu (top right) → **Apply Configuration** and paste the URL
4. In the dialog, select **Switch to Advanced Mode**
5. Fill in the variables as desired
6. Click **Next → Finish** — installation starts
7. Eclipse launches automatically and Oomph completes the setup (git clone, project import, build, settings)
8. Optionally import `preferences.epf` manually and re-set the target platform if needed

> **Note:** If you provide an existing FIPS git repo path, that repo will be used instead of cloning a new one.
