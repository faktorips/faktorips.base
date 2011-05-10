/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productrelease;

import java.net.PasswordAuthentication;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.productrelease.ITargetSystem;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;

public class UsernamePasswordDialog extends Dialog {

    public static final String USERNAME_SETTINGS = "username"; //$NON-NLS-1$

    public static final String PASSWORD_SETTINGS = "password"; //$NON-NLS-1$

    public static final String SAVE_PASSWD_SETTINGS = "savePassword"; //$NON-NLS-1$

    private Text usernameField;

    private Text passwordField;

    private final ITargetSystem targetSystem;

    private Checkbox savePassworField;

    private PasswordAuthentication passwordAuthentication;

    public UsernamePasswordDialog(Shell parentShell, ITargetSystem targetSystem) {
        super(parentShell);
        this.targetSystem = targetSystem;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite comp = (Composite)super.createDialogArea(parent);

        UIToolkit uiToolkit = new UIToolkit(null);

        GridLayout layout = (GridLayout)comp.getLayout();
        layout.numColumns = 2;

        Label prompt = uiToolkit.createLabel(comp,
                NLS.bind(Messages.UsernamePasswordDialog_prompt, targetSystem.getName()));
        GridData promptLayoutData = new GridData();
        promptLayoutData.horizontalSpan = 2;
        prompt.setLayoutData(promptLayoutData);

        uiToolkit.createVerticalSpacer(comp, 5);
        uiToolkit.createVerticalSpacer(comp, 5);

        uiToolkit.createLabel(comp, Messages.UsernamePasswordDialog_username);

        usernameField = uiToolkit.createText(comp, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        usernameField.setLayoutData(data);

        uiToolkit.createLabel(comp, Messages.UsernamePasswordDialog_password);

        passwordField = uiToolkit.createText(comp, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        passwordField.setLayoutData(data);

        uiToolkit.createVerticalSpacer(comp, 0);
        savePassworField = uiToolkit.createCheckbox(comp, Messages.UsernamePasswordDialog_savePassword);

        try {
            loadSettings();
        } catch (StorageException e) {
            IpsPlugin.log(e);
        }
        if (passwordAuthentication == null) {
            passwordAuthentication = new PasswordAuthentication("", new char[0]); //$NON-NLS-1$
        }

        usernameField.setText(passwordAuthentication.getUserName());
        passwordField.setText(new String(passwordAuthentication.getPassword()));

        return comp;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (buttonId == OK) {
            targetSystem.setPasswordAuthentication(new PasswordAuthentication(username, password.toCharArray()));
            saveSettings(username, password);
        }
        super.buttonPressed(buttonId);
    }

    private void loadSettings() throws StorageException {
        ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
        IDialogSettings dialogSettings = IpsUIPlugin.getDefault().getDialogSettings()
                .getSection(ProductReleaserBuilderWizard.DIALOG_SETTINGS);
        String username = dialogSettings.get(USERNAME_SETTINGS + "@" + targetSystem.getName()); //$NON-NLS-1$ 
        String password = securePreferences.get(username + "@" + targetSystem.getName(), ""); //$NON-NLS-1$ //$NON-NLS-2$
        if (username != null) {
            passwordAuthentication = new PasswordAuthentication(username, password.toCharArray());
        }
        boolean savePW = dialogSettings.getBoolean(SAVE_PASSWD_SETTINGS);
        savePassworField.setChecked(savePW);

    }

    private void saveSettings(String username, String password) {
        ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault();
        IDialogSettings dialogSettings = IpsUIPlugin.getDefault().getDialogSettings()
                .getSection(ProductReleaserBuilderWizard.DIALOG_SETTINGS);
        try {
            dialogSettings.put(USERNAME_SETTINGS + "@" + targetSystem.getName(), username); //$NON-NLS-1$ 
            if (savePassworField.isChecked()) {
                securePreferences.put(username + "@" + targetSystem.getName(), password, true); //$NON-NLS-1$
            }
        } catch (StorageException e) {
            IpsPlugin.log(e);
        }
        dialogSettings.put(SAVE_PASSWD_SETTINGS, savePassworField.isChecked());
    }

}