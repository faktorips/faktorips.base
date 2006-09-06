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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * Wizard page to create a new root test policy cmpt type parameter.<br>
 * The following fields will be handled: Datatype (ValuaDatatype or policy cmpt type), parameter
 * name and the test parameter type (input, exp result or combined).
 * 
 * @author Joerg Ortmann
 */
public class NewRootParamWizardPage extends WizardPage implements ValueChangeListener  {
    private static final String PAGE_ID = "RootParameterSelection"; //$NON-NLS-1$
    private static final int PAGE_NUMBER = 1;
    
    private NewRootParameterWizard wizard;
    
    private EditField editFieldDatatype;
    private EditField editFieldName;
    private EditField editFieldParamType;
    
    private String prevDatatype;
    
    public NewRootParamWizardPage(NewRootParameterWizard wizard){
        super(PAGE_ID, Messages.NewRootParamWizardPage_Title, null);
        this.setDescription(Messages.NewRootParamWizardPage_Description);
        this.wizard = wizard;
    }
    
    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        UIToolkit uiToolkit = wizard.getUiToolkit();

        Composite c = uiToolkit.createLabelEditColumnComposite(parent);
        
        uiToolkit.createLabel(c, Messages.NewRootParamWizardPage_Label_Datatype);
        editFieldDatatype = new TextButtonField(uiToolkit.createDatatypeRefEdit(wizard.getTestCaseType()
                .getIpsProject(), c));
        editFieldDatatype.addChangeListener(this);
        
        uiToolkit.createLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_Name);
        editFieldName = new TextField(uiToolkit.createText(c));
        editFieldName.addChangeListener(this);
        
        uiToolkit.createLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_TestParameterType);
        editFieldParamType = new EnumValueField(uiToolkit.createCombo(c, TestParameterType
                .getEnumType()), TestParameterType.getEnumType());
        editFieldParamType.addChangeListener(this);
        
        setControl(c);
        
        setPageComplete(false);
    }
    
    /**
     * Connects the edit fields with the given controller to the given test parameter
     */    
    void connectToModel(IpsPartUIController controller, ITestParameter testParameter){
        controller.add(editFieldName, ITestParameter.PROPERTY_NAME);
        controller.add(editFieldParamType, ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
    }
    
    /**
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == editFieldDatatype) {
            try {
                datatypeChanged(editFieldDatatype.getText());
            } catch (CoreException ex) {
                IpsPlugin.logAndShowErrorDialog(ex);
            }
        }
        wizard.postAsyncRunnable(new Runnable() {
            public void run() {
                if (wizard.getShell().isDisposed())
                    return;
                updateSetPageComplete();
            }
        });
    }

    /**
     * Datatype has changed.
     */
    private void datatypeChanged(String newDatatype) throws CoreException{
        if (newDatatype.equals(prevDatatype))
            return;
        prevDatatype = newDatatype;
        
        Datatype datatype = findDatatype(newDatatype);
        
        wizard.newTestParameter(datatype);
    }
    
    /**
     * Finds and returns the datatype with the given name.

     * @throws CoreException
     */
    private Datatype findDatatype(String datatype) throws CoreException{
        return wizard.getTestCaseType().getIpsProject().findDatatype(datatype);
    }
    
    /**
     * Validate the page.
     * 
     * @throws CoreException
     */
    private boolean validatePage() throws CoreException {
        setErrorMessage(null);
        String datatype = editFieldDatatype.getText();
        if (findDatatype(datatype)==null) {
            setErrorMessage(NLS.bind(Messages.NewRootParamWizardPage_Error_DatatypeDoesNotExists, datatype));
            return false;
        }
        if ("".equals(editFieldParamType.getText()) || "".equals(editFieldName.getText())){ //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        
        return wizard.isPageValid(PAGE_NUMBER);
    }
    
    /**
     * Updates the page complete status.
     */
    private void updateSetPageComplete() {
        boolean completeAllowed = false;
        try {
            completeAllowed = validatePage();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        super.setPageComplete(completeAllowed);
    }
    
    /**
     * Informs the wizard that this page was displayed.
     * 
     * {@inheritDoc}
     */
    public IWizardPage getNextPage() {
        wizard.setMaxPageShown(PAGE_NUMBER);
        return super.getNextPage();
    }
    
}
