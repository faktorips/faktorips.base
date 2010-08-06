/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.wizards;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;

public class IpsProjectHtmlExportWizardPage extends WizardDataTransferPage implements ValueChangeListener,
        ModifyListener, ICheckStateListener {

    private static final String PAGE_NAME = "IpsProjectHtmlExportWizardPage"; //$NON-NLS-1$

    private static final String STORE_DESTINATION_NAMES = PAGE_NAME + ".DESTINATION_NAMES_ID"; //$NON-NLS-1$

    private IStructuredSelection selection;

    private UIToolkit toolkit = new UIToolkit(null);
    // TODO private Checkbox includeReferencedProjects;
    private Combo destinationNamesCombo;
    private Checkbox showValidationErrorsCheckBox;

    protected IpsProjectHtmlExportWizardPage(IStructuredSelection selection) {
        super(PAGE_NAME);
        this.selection = selection;
        setTitle(Messages.IpsProjectHtmlExportWizardPage_projectName + getProject().getName());
        setDescription(Messages.IpsProjectHtmlExportWizardPage_description);

        setPageComplete(false);
    }

    @Override
    protected boolean allowNewContainerName() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));

        createDestinationGroup(composite);

        // TODO includeReferencedProjects = toolkit.createCheckbox(composite,
        // "Include referenced Projects");

        showValidationErrorsCheckBox = toolkit.createCheckbox(composite, Messages.IpsProjectHtmlExportWizardPage_showValidationErrors);

        restoreWidgetValues();

        setControl(composite);
    }

    private void createDestinationGroup(Composite parent) {
        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL));

        new Label(destinationSelectionGroup, SWT.NONE).setText(Messages.IpsProjectHtmlExportWizardPage_destination);

        // destination name entry field
        destinationNamesCombo = new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
        ComboField destinationNameComboField = new ComboField(destinationNamesCombo);
        destinationNameComboField.addChangeListener(this);
        destinationNameComboField.setText(getDefaultDestinationDirectory());
        destinationNamesCombo.addModifyListener(this);
        destinationNamesCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // destination browse button
        Button destinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
        destinationBrowseButton.setText(Messages.IpsProjectHtmlExportWizardPage_browse);
        destinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        destinationBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDestinationBrowseButtonPressed();
            }
        });
    }

    /**
     * Open an appropriate destination browser
     */
    protected void handleDestinationBrowseButtonPressed() {
        DirectoryDialog directoryDialog = new DirectoryDialog(getContainer().getShell());
        directoryDialog.setText(Messages.IpsProjectHtmlExportWizardPage_directoryDialogText);
        directoryDialog.setFilterPath(getProject().getLocation() + File.separator + "html"); //$NON-NLS-1$

        String selectedDirectoryName = directoryDialog.open();
        if (selectedDirectoryName != null) {
            destinationNamesCombo.setText(selectedDirectoryName);
        }
    }

    private IProject getProject() {
        return (IProject)selection.getFirstElement();
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        canFinish();
    }

    @Override
    public void modifyText(ModifyEvent e) {
        canFinish();
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        canFinish();
    }

    public String getDestinationDirectory() {
        return destinationNamesCombo.getText().trim();
    }

    public boolean getShowValidationErrors() {
        return showValidationErrorsCheckBox.isChecked();
    }

    private String getDefaultDestinationDirectory() {
        IProject firstElement = getProject();
        return firstElement.getLocation().toOSString() + File.separator + "html"; //$NON-NLS-1$

    }

    public boolean isIncludingReferencedProjects() {
        return true; // TODO includeReferencedProjects.isChecked();
    }

    private void canFinish() {
        if (selection.size() != 1) {
            setPageComplete(false);
            return;
        }
        if (StringUtils.isNotBlank(getDestinationDirectory())) {
            setPageComplete(true);
            return;
        }
        setPageComplete(false);
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }

        // restore previous entered destination
        destinationNamesCombo.setText(""); //$NON-NLS-1$
        String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES);
        if (directoryNames == null) {
            return; // ie.- no settings stored
        }
        if (!destinationNamesCombo.getText().equals(directoryNames[0])) {
            destinationNamesCombo.add(destinationNamesCombo.getText());
        }
        for (String directoryName : directoryNames) {
            destinationNamesCombo.add(directoryName);
        }

    }

    @Override
    protected void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }

        // store destination history
        String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES);
        if (directoryNames == null) {
            directoryNames = new String[0];
        }
        directoryNames = addToHistory(directoryNames, getDestinationDirectory());
        settings.put(STORE_DESTINATION_NAMES, directoryNames);
    }

}
