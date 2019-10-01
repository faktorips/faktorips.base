/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin.ImageHandling;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.core.ui.controls.Radiobutton;

public final class AddIpsNatureDialog extends TitleAreaDialog {

    private String sourceFolderName;
    private String basePackageName;
    private String runtimeIdPrefix;

    private boolean isModelProject;
    private boolean isProductDefinitionProject;
    private boolean isPersistentProject;

    private IJavaProject javaProject;

    private Text sourceFolderText;
    private Text basePackageText;
    private Text runtimeIdText;

    private Radiobutton modelProjectButton;
    private Radiobutton productDefinitionProjectButton;
    private Radiobutton fullProjectButton;

    private Checkbox enablePersistenceCheckbox;

    private Image dlgTitleImage;
    private Button okButton;

    private SupportedLanguagesControl supportedLanguagesControl;

    /**
     * The current validation status. Its value can be one of the following:
     * <ul>
     * <li><code>IMessageProvider.NONE</code> (default);</li>
     * <li><code>IMessageProvider.WARNING</code>;</li>
     * <li><code>IMessageProvider.ERROR</code>;</li>
     * </ul>
     * Used when validating the user input.
     */
    private int validationStatus;

    public AddIpsNatureDialog(Shell parentShell, IJavaProject javaProject) {
        super(parentShell);
        this.javaProject = javaProject;

        sourceFolderName = Messages.AddIpsNatureDialog_defaultSourceFolderName;
        basePackageName = Messages.AddIpsNatureDialog_basePackage_default;
        runtimeIdPrefix = Messages.AddIpsNatureDialog_defaultRuntimeIdPrefix;
        isModelProject = true;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.AddIpsNatureDialog_dialogTitle);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite superComposite = (Composite)super.createDialogArea(parent);
        UIToolkit kit = new UIToolkit(null);
        Composite composite = kit.createGridComposite(superComposite, 1, true, true);

        createProjectTypeGroup(kit, composite);
        kit.createVerticalSpacer(composite, 5);
        createNamingGroup(kit, composite);
        kit.createVerticalSpacer(composite, 5);
        createSupportedLanguagesGroup(kit, composite);

        applyDialogFont(composite);

        return composite;
    }

    private void createSupportedLanguagesGroup(UIToolkit kit, Composite parent) {
        Group group = kit.createGroup(parent, Messages.AddIpsNatureDialog_SupportedLanguagesGroup);
        supportedLanguagesControl = new SupportedLanguagesControl(group);
        supportedLanguagesControl.initialize();
    }

    private void createNamingGroup(UIToolkit kit, Composite parent) {
        Group group = kit.createGroup(parent, Messages.AddIpsNatureDialog_NamingGroup);
        Composite textComposite = kit.createLabelEditColumnComposite(group);

        kit.createLabel(textComposite, Messages.AddIpsNatureDialog_sourceFolderName, false);
        sourceFolderText = kit.createText(textComposite);
        sourceFolderText.setText(sourceFolderName);
        sourceFolderText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                sourceFolderModified();
            }
        });

        kit.createLabel(textComposite, Messages.AddIpsNatureDialog_basePackageName, false);
        basePackageText = kit.createText(textComposite);
        basePackageText.setText(basePackageName);
        basePackageText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                basePackageModified();
            }
        });

        kit.createLabel(textComposite, Messages.AddIpsNatureDialog_runtimeIdPrefix, false);
        runtimeIdText = kit.createText(textComposite);
        runtimeIdText.setText(runtimeIdPrefix);
    }

    @SuppressWarnings("deprecation")
    private void createProjectTypeGroup(UIToolkit kit, Composite parent) {
        RadioButtonGroup<?> group = kit.createRadiobuttonGroup(parent, SWT.SHADOW_IN,
                Messages.AddIpsNatureDialog_ProjectType);

        modelProjectButton = group.addRadiobutton(Messages.AddIpsNatureDialog_modelProject);
        modelProjectButton.setChecked(isModelProject && !isProductDefinitionProject);

        productDefinitionProjectButton = group.addRadiobutton(Messages.AddIpsNatureDialog_productDefinitionProject);
        productDefinitionProjectButton.setChecked(isProductDefinitionProject && !isModelProject);

        fullProjectButton = group.addRadiobutton(Messages.AddIpsNatureDialog_fullProject);
        fullProjectButton.setChecked(isModelProject && isProductDefinitionProject);

        enablePersistenceCheckbox = kit.createCheckbox(group.getComposite());
        enablePersistenceCheckbox.setText(Messages.AddIpsNatureDialog_PersistenceSupport);
    }

    /**
     * Fires validations (variable name first) and updates enabled state for the "Ok" button
     * accordingly.
     */
    private void sourceFolderModified() {
        validationStatus = IMessageProvider.NONE;
        okButton.setEnabled(validateSourceFolder());
    }

    /**
     * Fires validations (variable name first) and updates enabled state for the "Ok" button
     * accordingly.
     */
    private void basePackageModified() {
        validationStatus = IMessageProvider.NONE;
        okButton.setEnabled(validateBasePackage());
    }

    /**
     * Validates the current variable name, and updates this dialog's message.
     * 
     * @return true if the name is valid, false otherwise
     */
    private boolean validateSourceFolder() {
        boolean allowFinish = false;

        // if the current validationStatus is ERROR, no additional validation applies
        if (validationStatus == IMessageProvider.ERROR) {
            return false;
        }

        // assumes everything will be ok
        String message = Messages.AddIpsNatureDialog_dialogMessage;
        int newValidationStatus = IMessageProvider.NONE;

        String sourceFolderName = sourceFolderText.getText();

        if (sourceFolderName.length() == 0) {
            // the source folder name is empty
            newValidationStatus = IMessageProvider.ERROR;
            message = Messages.AddIpsNatureDialog_ErrorNoSourceFolderName;
        } else if (!(new Path("").isValidSegment(sourceFolderName))) { //$NON-NLS-1$
            newValidationStatus = IMessageProvider.ERROR;
            message = Messages.AddIpsNatureDialog_TheSourceFolderMustBeADirectChildOfTheProject;
        } else {
            allowFinish = true;
        }
        // overwrite the current validation status / message only if everything is ok (clearing
        // them)
        // or if we have a more serious problem than the current one
        if (validationStatus == IMessageProvider.NONE || newValidationStatus == IMessageProvider.ERROR) {
            validationStatus = newValidationStatus;
        }
        // only set the message here if it is not going to be set in
        // validateVariableValue to avoid flashing.
        setMessage(message, validationStatus);
        return allowFinish;
    }

    /**
     * Validates the current variable name, and updates this dialog's message.
     * 
     * @return true if the name is valid, false otherwise
     */
    private boolean validateBasePackage() {
        boolean allowFinish = false;

        // if the current validationStatus is ERROR, no additional validation applies
        if (validationStatus == IMessageProvider.ERROR) {
            return false;
        }

        // assumes everything will be ok
        String message = Messages.AddIpsNatureDialog_dialogMessage;
        int newValidationStatus = IMessageProvider.NONE;

        String sourceLevel = javaProject == null ? "1.4" : javaProject.getOption(JavaCore.COMPILER_SOURCE, true); //$NON-NLS-1$
        String complianceLevel = javaProject == null ? "1.4" //$NON-NLS-1$
                : javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
        if (!JavaConventions.validatePackageName(basePackageText.getText(), sourceLevel, complianceLevel).isOK()) {
            newValidationStatus = IMessageProvider.ERROR;
            message = Messages.AddIpsNatureDialog_basePackageNameNotValid;
        } else {
            allowFinish = true;
        }
        // overwrite the current validation status / message only if everything is ok (clearing
        // them)
        // or if we have a more serious problem than the current one
        if (validationStatus == IMessageProvider.NONE || newValidationStatus == IMessageProvider.ERROR) {
            validationStatus = newValidationStatus;
        }
        // only set the message here if it is not going to be set in
        // validateVariableValue to avoid flashing.
        setMessage(message, validationStatus);
        return allowFinish;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            sourceFolderName = sourceFolderText.getText();
            runtimeIdPrefix = runtimeIdText.getText();
            basePackageName = basePackageText.getText();
            isModelProject = modelProjectButton.isChecked() || fullProjectButton.isChecked();
            isProductDefinitionProject = productDefinitionProjectButton.isChecked() || fullProjectButton.isChecked();
            isPersistentProject = enablePersistenceCheckbox.isChecked();
        }
        super.buttonPressed(buttonId);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Messages.AddIpsNatureDialog_dialogTitle);
        ImageHandling imageHandling = IpsUIPlugin.getImageHandling();
        dlgTitleImage = imageHandling.getImage(imageHandling.createImageDescriptor("wizards/AddIpsNatureWizard.png")); //$NON-NLS-1$
        setTitleImage(dlgTitleImage);
        setMessage(Messages.AddIpsNatureDialog_dialogMessage);
        return contents;
    }

    public String getSourceFolderName() {
        return sourceFolderName;
    }

    public String getBasePackageName() {
        return basePackageName;
    }

    public String getRuntimeIdPrefix() {
        return runtimeIdPrefix;
    }

    public boolean isModelProject() {
        return isModelProject;
    }

    public boolean isProductDefinitionProject() {
        return isProductDefinitionProject;
    }

    public boolean isPersistentProject() {
        return isPersistentProject;
    }

    public List<Locale> getLocales() {
        return supportedLanguagesControl.getLocales();
    }

}
