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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
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
 * Wizard page to create a new child test policy cmpt type parameter.<br>
 * The following fields will be handled: Relation, target policy cmpt type, test policy cmpt type
 * parameter name and the test parameter type (input, exp result or combined).
 * 
 * @author Joerg Ortmann
 */
public class NewChildParamWizardPage extends WizardPage implements ValueChangeListener  {
    private static final String PAGE_ID = "RootParameterSelection"; //$NON-NLS-1$
    private static final int PAGE_NUMBER = 1;
    
    private NewChildParameterWizard wizard;
    
    private EditField editFieldRelation;
    private EditField editFieldTarget;
    private EditField editFieldName;
    private EditField editFieldParamType;
    
    private String prevRelation;
    private IRelation relation;
    
    private RelationTargetRefControl relTargetRefControl;
    private RelationRefControl relationRefControl;
    
    public NewChildParamWizardPage(NewChildParameterWizard wizard){
        super(PAGE_ID, Messages.NewChildParamWizardPage_Title, null);
        this.setDescription(Messages.NewChildParamWizardPage_Description);
        this.wizard = wizard;
    }
    
    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        UIToolkit uiToolkit = wizard.getUiToolkit();

        Composite c = uiToolkit.createLabelEditColumnComposite(parent);
        
        uiToolkit.createLabel(c, Messages.NewChildParamWizardPage_Label_Relation);
        relationRefControl = new RelationRefControl(c, uiToolkit, wizard.getParentPolicyCmptType());
        editFieldRelation = new TextButtonField(relationRefControl);
        editFieldRelation.addChangeListener(this);

        uiToolkit.createLabel(c, Messages.NewChildParamWizardPage_Label_Target);
        relTargetRefControl = new RelationTargetRefControl(wizard.getTestCaseType().getIpsProject(), c,
                uiToolkit, null);
        editFieldTarget = new TextButtonField(relTargetRefControl);
        editFieldTarget.addChangeListener(this);
        
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
    void connectToModel(IpsPartUIController controller, ITestParameter testParameter) {
        controller.add(editFieldRelation, ITestPolicyCmptTypeParameter.PROPERTY_RELATION);
        controller.add(editFieldTarget, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
        controller.add(editFieldName, ITestParameter.PROPERTY_NAME);
        controller.add(editFieldParamType, ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
    }    
    
    /**
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == editFieldRelation) {
            relationChanged(editFieldRelation.getText());
        }
//        if (e.field == editFieldTarget){
//            targetChanged(editFieldTarget.getText());
//        }
        wizard.postAsyncRunnable(new Runnable() {
            public void run() {
                if (wizard.getShell().isDisposed())
                    return;
                updateSetPageComplete();
            }
        });
    }
    
//    /**
//     * Target has changed.
//     */
//    private void targetChanged(String newTarget) {
//        if (newTarget.equals(prevTarget))
//            return;
//        prevTarget = newTarget;
//        
//        try {
//            policyCmptTypeTarget = relTargetRefControl.findPcType();
//        } catch (CoreException e) {
//            IpsPlugin.logAndShowErrorDialog(e);
//        }
//
//        postNewTestParameter();
//    }

    /**
     * Relation has changed.
     */
    private void relationChanged(String newRelation) {
        if (newRelation.equals(prevRelation))
            return;
        prevRelation = newRelation;
        
        try {
            relation = relationRefControl.findRelation();
            if (relation == null)
                return;

            relTargetRefControl.setPolicyCmptTypeTarget(relation.findTarget());
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        postNewTestParameter();
    }

    /**
     * Creates a new test parameter in an asynchronous manner.
     */
    private void postNewTestParameter(){
        wizard.postAsyncRunnable(new Runnable() {
            public void run() {
                if (wizard.getShell().isDisposed())
                    return;
                if (relation != null)
                    wizard.newTestParameter(relation.getName());
            }
        });
    }
    
    /**
     * Validate the page.
     * 
     * @throws CoreException
     */
    private boolean validatePage() throws CoreException {
        setErrorMessage(null);
        if (relation == null) {
            setErrorMessage(NLS.bind(Messages.NewChildParamWizardPage_Error_RelationDoesNotExists, editFieldRelation.getText()));
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
