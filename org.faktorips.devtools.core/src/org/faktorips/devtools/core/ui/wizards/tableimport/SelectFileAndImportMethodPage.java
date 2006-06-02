/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableimport;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.TableFileFormat;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.FileSelectionControl;
import org.faktorips.devtools.core.ui.controls.Radiobutton;

/**
 * 
 * @author Thorsten Waertel
 */
public class SelectFileAndImportMethodPage extends WizardPage implements
		ValueChangeListener {
	
    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;

    private Combo fileFormatControl;
    
    // edit fields
    private TextButtonField filenameField;
    private CheckboxField importIntoExistingField;
    private CheckboxField importIntoNewField;
    private CheckboxField importExistingReplaceField;
    private CheckboxField importExistingAppendField;
    private ComboField fileFormatField;
    
    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;
    
    // page control as defined by the wizard page class
    private Composite pageControl;
    
	/**
     * 
     * @param selection
     * @throws JavaModelException
	 */
    public SelectFileAndImportMethodPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.SelectFileAndImportMethodPage_title);
        // TODO set selectedResource if selection contains a .xls file
        selectedResource = null;
        setPageComplete(false);
	}
    
    /**
	 * {@inheritDoc}
	 */
	public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == filenameField) {
            filenameChanged();
        }
        if (e.field == importIntoExistingField) {
            importIntoExistingChanged();
        }
        if (e.field == importIntoNewField) {
            importIntoNewChanged();
        }
        if (e.field == importExistingAppendField) {
            importExistingAppendChanged();
        }
        if (e.field == importExistingReplaceField) {
            importExistingReplaceChanged();
        }
        if (e.field==fileFormatField) {
            formatChanged();
        }
        if (validateInput) { // don't validate during control creating!
            try {
                validatePage();    
            } catch (CoreException coreEx) {
                IpsPlugin.logAndShowErrorDialog(coreEx);
            }
            
        }
        updatePageComplete();
	}
    
    protected void filenameChanged() {
        
    }
    
    protected void formatChanged() {
        
    }
    
    protected void importIntoExistingChanged() {
        if (importIntoExistingField.getCheckbox().isChecked()) {
            importIntoNewField.getCheckbox().setChecked(false);
            importExistingReplaceField.getCheckbox().setEnabled(true);
            importExistingAppendField.getCheckbox().setEnabled(true);
            if (!importExistingAppendField.getCheckbox().isChecked() && !importIntoNewField.getCheckbox().isChecked()) {
                importExistingAppendField.getCheckbox().setChecked(true);
            }
        }
    }
    
    protected void importIntoNewChanged() {
        if (importIntoNewField.getCheckbox().isChecked()) {
            importIntoExistingField.getCheckbox().setChecked(false);
            importExistingReplaceField.getCheckbox().setEnabled(false);
            importExistingAppendField.getCheckbox().setEnabled(false);
        }
    }
    
    protected void importExistingAppendChanged() {
        if (importExistingAppendField.getCheckbox().isChecked()) {
            importExistingReplaceField.getCheckbox().setChecked(false);
        }
    }
    
    protected void importExistingReplaceChanged() {
        if (importExistingReplaceField.getCheckbox().isChecked()) {
            importExistingAppendField.getCheckbox().setChecked(false);
        }
    }

    /**
     * Validates the page and generates error messages if needed. 
     * Can be overridden in subclasses to add specific validation logic.s 
     */
    protected void validatePage() throws CoreException {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        validateFilename();
        if (getErrorMessage()!=null) {
            return;
        }
        validateImportMethod();
        if (getErrorMessage()!=null) {
            return;
        }
        validateImportMethod();
        if (getErrorMessage()!=null) {
            return;
        }
        validateFormat();
        if (getErrorMessage()!=null) {
            return;
        }
        validateImportExistingMethod();
    }
    
    protected void validateFormat() {
        String name=fileFormatField.getText(); 
        // must not be empty
        if (name.length() == 0) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgMissingFileFormat);
            return;
        }
    }
    
    private void validateImportExistingMethod() {
        if (importIntoExistingField.getCheckbox().isChecked() && 
                !importExistingAppendField.getCheckbox().isChecked() && !importExistingReplaceField.getCheckbox().isChecked()) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgMissingImportExistingMethod);
        }
    }
    
    private void validateImportMethod() {
        if (!importIntoExistingField.getCheckbox().isChecked() && !importIntoNewField.getCheckbox().isChecked()) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgMissingImportMethod);
        }
    }
    
    private void validateFilename() {
        if (filenameField.getText().length() == 0) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgEmptyFilename);
        }
        // TODO check if selected filename is a valid importable file
    }
    
	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.SelectFileAndImportMethodPage_title);
        
        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);
        
        Composite filenameComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(filenameComposite, Messages.SelectFileAndImportMethodPage_labelName); 
        filenameField = new TextButtonField(new FileSelectionControl(filenameComposite, toolkit));
        filenameField.addChangeListener(this);
        
        toolkit.createFormLabel(filenameComposite, Messages.SelectFileAndImportMethodPage_labelFileFormat);
        fileFormatControl = toolkit.createCombo(filenameComposite, TableFileFormat.getEnumType());
        fileFormatControl.setText(TableFileFormat.getEnumType().getValues()[0].getId());
        fileFormatField = new ComboField(fileFormatControl);
        fileFormatField.addChangeListener(this);
        
        Composite importExistingComposite = new Composite(pageControl, SWT.NONE);
        importExistingComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout importExistingLayout = new GridLayout(1, false);
        importExistingLayout.verticalSpacing = 5;
        importExistingComposite.setLayout(importExistingLayout);
        Radiobutton importExistingRb = toolkit.createRadiobutton(importExistingComposite, 
                Messages.SelectFileAndImportMethodPage_labelImportExisting);
        importIntoExistingField = new CheckboxField(importExistingRb);
        importIntoExistingField.addChangeListener(this);
        
        Group importExistingModeGroup = toolkit.createGroup(importExistingComposite, ""); //$NON-NLS-1$
        importExistingModeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Radiobutton importExistingReplaceRb = toolkit.createRadiobutton(importExistingModeGroup,
                Messages.SelectFileAndImportMethodPage_labelImportExistingReplace);
        importExistingReplaceField = new CheckboxField(importExistingReplaceRb);
        importExistingReplaceField.addChangeListener(this);
        importExistingReplaceRb.setEnabled(false);
        Radiobutton importExistingAppendRb = toolkit.createRadiobutton(importExistingModeGroup,
                Messages.SelectFileAndImportMethodPage_labelImportExistingAppend);
        importExistingAppendField = new CheckboxField(importExistingAppendRb);
        importExistingAppendField.addChangeListener(this);
        importExistingAppendRb.setEnabled(false);
        
        Radiobutton importNewRb = toolkit.createRadiobutton(pageControl,
                Messages.SelectFileAndImportMethodPage_labelImportNew);
        importIntoNewField = new CheckboxField(importNewRb);
        importIntoNewField.addChangeListener(this);
        importNewRb.setChecked(true);

        setDefaults(selectedResource);

        validateInput = true;
	}

    /**
     * Derives the default values for source folder and package from
     * the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when
     * the wizard was opened.
     * @throws CoreException 
     */
    protected void setDefaults(IResource selectedResource) {
        if (selectedResource==null) {
            return;
        }
        if (selectedResource instanceof IFile) {
            // TODO determine somehow if selected resource actually is an importable (excel) file
            setFilename(((IFile) selectedResource).getName());
        }
        try {
            validatePage();    
        } catch (CoreException coreEx) {
            IpsPlugin.logAndShowErrorDialog(coreEx);
        }
        updatePageComplete();
    }

    public String getFilename() {
        return filenameField.getText();
    }
    
    protected void setFilename(String newName) {
        filenameField.setText(newName);
    }
    
    public String getFormat() {
        return fileFormatField.getText();
    }
    
    protected void updatePageComplete() {
        if (getErrorMessage()!=null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(filenameField.getText()) //$NON-NLS-1$
        && !"".equals(fileFormatField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }
    
    public boolean isImportIntoExisting() {
        return importIntoExistingField.getCheckbox().isChecked();
    }
    
    public boolean isImportExistingReplace() {
        if (isImportIntoExisting()) {
            return importExistingReplaceField.getCheckbox().isChecked();
        }
        return false;
    }
    
    public boolean isImportExistingAppend() {
        if (isImportIntoExisting()) {
            return importExistingAppendField.getCheckbox().isChecked();
        }
        return false;
    }
}
