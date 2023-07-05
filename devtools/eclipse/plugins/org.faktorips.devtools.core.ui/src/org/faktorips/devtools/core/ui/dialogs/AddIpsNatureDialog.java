/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin.ImageHandling;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.eclipse.internal.IpsClasspathContainerInitializer;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.IpsProjectConfigurators;
import org.faktorips.devtools.model.util.IpsProjectCreationProperties;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.runtime.MessageList;

public final class AddIpsNatureDialog extends TitleAreaDialog {

    private static final int MODEL_PROJECT_OPTION = 1;
    private static final int PRODUCT_DEFINITION_PROJECT_OPTION = 2;
    private static final int FULL_PROJECT_OPTION = 3;

    /**
     * Wrapper containing all properties for adding the IPS nature.
     */
    private IpsProjectCreationProperties ipsProjectCreationProperties;

    private IJavaProject javaProject;

    private Text sourceFolderText;
    private Text basePackageText;
    private Text runtimeIdText;

    private Button enableGroovyCheckbox;
    private Button enablePersistenceCheckbox;
    private Button enableJaxbSupport;

    private Combo persistenceSupport;
    private Combo jaxbSelection;

    private Image dlgTitleImage;
    private Button okButton;

    private RadioButtonGroup<Integer> projectSelectionGroup;

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

        ipsProjectCreationProperties = new IpsProjectCreationProperties();
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
        sourceFolderText.setText(ipsProjectCreationProperties.getSourceFolderName());
        sourceFolderText.addModifyListener($ -> sourceFolderModified());

        kit.createLabel(textComposite, Messages.AddIpsNatureDialog_basePackageName, false);
        basePackageText = kit.createText(textComposite);
        basePackageText.setText(ipsProjectCreationProperties.getBasePackageName());
        basePackageText.addModifyListener($ -> basePackageModified());

        kit.createLabel(textComposite, Messages.AddIpsNatureDialog_runtimeIdPrefix, false);
        runtimeIdText = kit.createText(textComposite);
        runtimeIdText.setText(ipsProjectCreationProperties.getRuntimeIdPrefix());
    }

    private void createProjectTypeGroup(UIToolkit kit, Composite parent) {

        LinkedHashMap<Integer, String> radioButtons = new LinkedHashMap<>();
        radioButtons.put(MODEL_PROJECT_OPTION, Messages.AddIpsNatureDialog_modelProject);
        radioButtons.put(PRODUCT_DEFINITION_PROJECT_OPTION, Messages.AddIpsNatureDialog_productDefinitionProject);
        radioButtons.put(FULL_PROJECT_OPTION, Messages.AddIpsNatureDialog_fullProject);

        projectSelectionGroup = kit.createRadioButtonGroup(
                parent, Messages.AddIpsNatureDialog_ProjectType, 1, radioButtons);

        boolean isModelProject = ipsProjectCreationProperties.isModelProject();
        boolean isProductDefinitionProject = ipsProjectCreationProperties.isProductDefinitionProject();

        projectSelectionGroup.getRadioButton(MODEL_PROJECT_OPTION)
                .setSelection(isModelProject && !isProductDefinitionProject);
        projectSelectionGroup.getRadioButton(PRODUCT_DEFINITION_PROJECT_OPTION)
                .setSelection(!isModelProject && isProductDefinitionProject);
        projectSelectionGroup.getRadioButton(FULL_PROJECT_OPTION)
                .setSelection(isModelProject && isProductDefinitionProject);

        if (isGroovyAvailable()) {
            enableGroovyCheckbox = kit.createButton(projectSelectionGroup.getComposite(),
                    Messages.AddIpsNatureDialog_GroovySupport, SWT.CHECK);
            enableGroovyCheckbox.setSelection(ipsProjectCreationProperties.isGroovySupport());
        }

        enablePersistenceCheckbox = kit.createButton(projectSelectionGroup.getComposite(),
                Messages.AddIpsNatureDialog_PersistenceSupport, SWT.CHECK);
        enablePersistenceCheckbox.setSelection(ipsProjectCreationProperties.isPersistentProject());
        enablePersistenceCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (enablePersistenceCheckbox.getSelection()) {
                    persistenceSupport.setEnabled(true);
                    // Generic JPA 2.0 is default
                    persistenceSupport.select(persistenceSupport.getItemCount() - 1);
                } else {
                    persistenceSupport.setEnabled(false);
                    persistenceSupport.select(0);
                }
            }
        });

        persistenceSupport = kit.createCombo(projectSelectionGroup.getComposite());
        persistenceSupport.setEnabled(false);
        PersistenceSupportNames.getAllPersistenceProviderOptions().forEach(persistenceSupport::add);
        persistenceSupport.select(0);

        String jaxbCheckboxText = Messages.AddIpsNatureDialog_JaxbSupport;
        boolean jaxbAvailable = IpsClasspathContainerInitializer.isJaxbSupportAvailable();
        if (!jaxbAvailable) {
            jaxbCheckboxText = jaxbCheckboxText
                    + org.faktorips.devtools.core.ui.Messages.IpsClasspathContainerPage_bundleNotInstalled;
        }
        enableJaxbSupport = kit.createButton(projectSelectionGroup.getComposite(), jaxbCheckboxText, SWT.CHECK);
        enableJaxbSupport.setEnabled(jaxbAvailable);
        enableJaxbSupport.addListener(SWT.Selection, event -> {
            if (enableJaxbSupport.getSelection()) {
                jaxbSelection.setEnabled(true);
                jaxbSelection.select(1);
            } else {
                jaxbSelection.setEnabled(false);
                jaxbSelection.select(0);
            }
        });

        jaxbSelection = kit.createCombo(projectSelectionGroup.getComposite());
        Stream.of(JaxbSupportVariant.values()).map(JaxbSupportVariant::name).forEach(jaxbSelection::add);
        jaxbSelection.setEnabled(false);
        jaxbSelection.select(0);
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
            ipsProjectCreationProperties.setSourceFolderName(sourceFolderText.getText());
            ipsProjectCreationProperties.setRuntimeIdPrefix(runtimeIdText.getText());
            ipsProjectCreationProperties.setBasePackageName(basePackageText.getText());

            boolean isFullProject = projectSelectionGroup.getRadioButton(FULL_PROJECT_OPTION).getSelection();
            boolean isModelProject = isFullProject
                    || projectSelectionGroup.getRadioButton(MODEL_PROJECT_OPTION).getSelection();
            boolean isProductDefinitionProject = isFullProject
                    || projectSelectionGroup.getRadioButton(PRODUCT_DEFINITION_PROJECT_OPTION).getSelection();
            ipsProjectCreationProperties.setModelProject(isModelProject);
            ipsProjectCreationProperties.setProductDefinitionProject(isProductDefinitionProject);

            ipsProjectCreationProperties.setPersistentProject(enablePersistenceCheckbox.getSelection());
            ipsProjectCreationProperties.setPersistenceSupport(persistenceSupport.getText());
            if (isGroovyAvailable()) {
                ipsProjectCreationProperties.setGroovySupport(enableGroovyCheckbox.getSelection());
            }
            ipsProjectCreationProperties.setJaxbEnabled(enableJaxbSupport.getSelection());
            ipsProjectCreationProperties.setJaxbSupport(JaxbSupportVariant.of(jaxbSelection.getText()));
            ipsProjectCreationProperties.setLocales(supportedLanguagesControl.getLocales());
            MessageList messages = ipsProjectCreationProperties
                    .validate(wrap(javaProject).as(AJavaProject.class));
            if (messages.containsErrorMsg()) {
                ErrorDialog.openError(getParentShell(),
                        Messages.bind(Messages.AddIpsNatureDialog_ErrorDialogTitle, javaProject.getElementName()),
                        Messages.AddIpsNatureDialog_ErrorDialogText,
                        IpsStatus.of(messages));
                return;
            }
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

    public IpsProjectCreationProperties getIpsProjectCreationProperties() {
        return ipsProjectCreationProperties;
    }

    private boolean isGroovyAvailable() {
        return IpsClasspathContainerInitializer.isGroovySupportAvailable()
                || IpsProjectConfigurators.isGroovySupported(wrap(javaProject).as(AJavaProject.class));
    }
}
