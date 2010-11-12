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
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
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
    private Combo destinationNamesCombo;
    private Combo supportedLanguageCombo;
    private Combo ipsProjectsCombo;

    private Checkbox showValidationErrorsCheckBox;

    private IIpsProject[] ipsProjects;

    private ComboField destinationNameComboField;

    protected IpsProjectHtmlExportWizardPage(IStructuredSelection selection) {
        super(PAGE_NAME);
        this.selection = selection;
        setPageComplete(false);

        initIpsProjects();
    }

    private void initIpsProjects() {
        try {
            ipsProjects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private void updateSelectedProject() {
        IIpsProject project = getSelectedIpsProject();

        if (project == null) {
            setTitle(Messages.IpsProjectHtmlExportWizardPage_htmlExport);
            setMessage(Messages.IpsProjectHtmlExportWizardPage_noProjectSelected, IMessageProvider.WARNING);
            return;
        }

        setMessage(null);

        setTitle(Messages.IpsProjectHtmlExportWizardPage_projectName + project.getName());
        setDescription(Messages.IpsProjectHtmlExportWizardPage_description);

        updateSupportedLanguageGroup();

        destinationNamesCombo.setFocus();
    }

    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // nothing to do
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL));

        Composite grid = new Composite(composite, SWT.NONE);
        grid.setLayout(new GridLayout(2, false));
        grid.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        createIpsProjectsCombo(grid);

        createDestinationGroup(grid);

        createSupportedLanguageGroup(grid);

        showValidationErrorsCheckBox = toolkit.createCheckbox(composite,
                Messages.IpsProjectHtmlExportWizardPage_showValidationErrors);

        restoreWidgetValues();

        setControl(composite);

        updateSelectedProject();

    }

    private void createIpsProjectsCombo(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.IpsProjectHtmlExportWizardPage_project);

        ipsProjectsCombo = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        ipsProjectsCombo.addModifyListener(this);
        ipsProjectsCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ipsProjectsCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleIpsProjectSelectionChanged();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

        for (IIpsProject ipsProject : ipsProjects) {
            ipsProjectsCombo.add(ipsProject.getName());
            if (ipsProject.equals(getIpsProject(selection))) {
                ipsProjectsCombo.select(ipsProjectsCombo.getItemCount() - 1);
            }
        }
    }

    protected void handleIpsProjectSelectionChanged() {
        updateSelectedProject();
    }

    private void createSupportedLanguageGroup(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.IpsProjectHtmlExportWizardPage_supportedLanguage);

        supportedLanguageCombo = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        supportedLanguageCombo.addModifyListener(this);
        supportedLanguageCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        updateSupportedLanguageGroup();
    }

    private void updateSupportedLanguageGroup() {
        IIpsProject ipsProject = getSelectedIpsProject();
        if (ipsProject == null) {
            return;
        }
        supportedLanguageCombo.removeAll();
        for (ISupportedLanguage iSupportedLanguage : ipsProject.getProperties().getSupportedLanguages()) {
            supportedLanguageCombo.add(iSupportedLanguage.getLanguageName());
            if (iSupportedLanguage.isDefaultLanguage()) {
                supportedLanguageCombo.select(supportedLanguageCombo.getItemCount() - 1);
            }
        }
    }

    private void createDestinationGroup(Composite parent) {
        new Label(parent, SWT.NONE).setText(Messages.IpsProjectHtmlExportWizardPage_destination);

        // destination specification group
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_FILL));

        // destination name entry field
        destinationNamesCombo = new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
        destinationNameComboField = new ComboField(destinationNamesCombo);
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
        directoryDialog.setFilterPath(getSelectedIpsProject().getProject().getLocation() + File.separator + "html"); //$NON-NLS-1$

        String selectedDirectoryName = directoryDialog.open();
        if (selectedDirectoryName != null) {
            destinationNamesCombo.setText(selectedDirectoryName);
        }
    }

    protected IIpsProject getSelectedIpsProject() {
        if (ipsProjectsCombo.getSelectionIndex() == -1) {
            return null;
        }
        return ipsProjects[ipsProjectsCombo.getSelectionIndex()];
    }

    private IProject getProject(IStructuredSelection strSelection) {
        Object selected = strSelection.getFirstElement();
        if (selected instanceof IResource) {
            return ((IResource)selected).getProject();
        }
        if (selected instanceof IIpsElement) {
            return ((IIpsElement)selected).getIpsProject().getProject();
        }
        if (selected instanceof IJavaElement) {
            return ((IJavaElement)selected).getJavaProject().getProject();
        }
        return null;
    }

    public IIpsProject getIpsProject(IStructuredSelection selection) {
        IProject project = getProject(selection);
        if (project == null) {
            return null;
        }
        IIpsModel ipsModel = IpsPlugin.getDefault().getIpsModel();
        return ipsModel.getIpsProject(project.getProject());
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

    public Locale getSupportedLanguage() {
        if (supportedLanguageCombo == null) {
            return null;
        }
        for (ISupportedLanguage iSupportedLanguage : getSelectedIpsProject().getProperties().getSupportedLanguages()) {
            if (supportedLanguageCombo.getText().equals(iSupportedLanguage.getLanguageName())) {
                return iSupportedLanguage.getLocale();
            }
        }
        return null;
    }

    private String getDefaultDestinationDirectory() {
        IIpsProject firstElement = getSelectedIpsProject();
        if (firstElement == null) {
            return ""; //$NON-NLS-1$
        }
        return firstElement.getProject().getLocation().toOSString() + File.separator + "html"; //$NON-NLS-1$
    }

    public boolean isIncludingReferencedProjects() {
        return true;
    }

    private void canFinish() {
        if (getSelectedIpsProject() == null) {
            setPageComplete(false);
            return;
        }

        if (getSupportedLanguage() == null) {
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
