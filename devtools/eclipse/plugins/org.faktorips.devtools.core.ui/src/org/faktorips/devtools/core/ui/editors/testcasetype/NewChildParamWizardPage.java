/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;

/**
 * Wizard page to create a new child test policy cmpt type parameter.<br>
 * The following fields will be handled: Association, target policy cmpt type, test policy cmpt type
 * parameter name and the test parameter type (input, exp result or combined).
 * 
 * @author Joerg Ortmann
 */
public class NewChildParamWizardPage extends WizardPage implements ValueChangeListener {

    protected static final int PAGE_NUMBER = 1;
    private static final String PAGE_ID = "RootParameterSelection"; //$NON-NLS-1$

    private NewChildParameterWizard wizard;

    private EditField<String> editFieldAssociation;
    private EditField<String> editFieldTarget;
    private EditField<String> editFieldName;
    private EnumField<TestParameterType> editFieldParamType;

    private String prevAssociation;
    private IPolicyCmptTypeAssociation association;

    private AssociationTargetRefControl accosiationTargetRefControl;
    private AssociationRefControl accosiationRefControl;

    public NewChildParamWizardPage(NewChildParameterWizard wizard) {
        super(PAGE_ID, Messages.NewChildParamWizardPage_Title, null);
        setDescription(Messages.NewChildParamWizardPage_Description);
        this.wizard = wizard;
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit uiToolkit = wizard.getUiToolkit();

        Composite c = uiToolkit.createLabelEditColumnComposite(parent);

        uiToolkit.createLabel(c, Messages.NewChildParamWizardPage_Label_Association);
        accosiationRefControl = new AssociationRefControl(c, uiToolkit, wizard.getParentPolicyCmptType());
        editFieldAssociation = new TextButtonField(accosiationRefControl);
        editFieldAssociation.addChangeListener(this);

        uiToolkit.createLabel(c, Messages.NewChildParamWizardPage_Label_Target);
        accosiationTargetRefControl = new AssociationTargetRefControl(wizard.getTestCaseType().getIpsProject(), c,
                uiToolkit, null);
        editFieldTarget = new TextButtonField(accosiationTargetRefControl);
        editFieldTarget.addChangeListener(this);

        uiToolkit.createLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_Name);
        editFieldName = new TextField(uiToolkit.createText(c));
        editFieldName.addChangeListener(this);

        uiToolkit.createLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_TestParameterType);
        editFieldParamType = new EnumField<>(uiToolkit.createCombo(c), TestParameterType.class);
        editFieldParamType.addChangeListener(this);

        setControl(c);
    }

    /**
     * Connects the edit fields with the given binding context to the given test parameter
     */
    void connectToModel(BindingContext bindingContext, ITestParameter newTestParameter) {
        bindingContext.bindContent(editFieldAssociation, newTestParameter,
                ITestPolicyCmptTypeParameter.PROPERTY_ASSOCIATION);
        bindingContext.bindContent(editFieldTarget, newTestParameter,
                ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
        bindingContext.bindContent(editFieldName, newTestParameter, ITestParameter.PROPERTY_NAME);
        bindingContext.bindContent(editFieldParamType, newTestParameter, ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == editFieldAssociation) {
            associationChanged(editFieldAssociation.getText());
        }

        wizard.postAsyncRunnable(() -> {
            if (wizard.getShell().isDisposed()) {
                return;
            }
            updateSetPageComplete();
        });
    }

    /**
     * Association has changed.
     */
    private void associationChanged(String newAssociation) {
        if (newAssociation.equals(prevAssociation)) {
            return;
        }
        prevAssociation = newAssociation;

        try {
            association = accosiationRefControl.findAssociation();
            if (association == null) {
                return;
            }

            accosiationTargetRefControl.setPolicyCmptTypeTarget(association.findTargetPolicyCmptType(association
                    .getIpsProject()));
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        postNewTestParameter();
    }

    /**
     * Creates a new test parameter in an asynchronous manner.
     */
    private void postNewTestParameter() {
        wizard.postAsyncRunnable(() -> {
            if (wizard.getShell().isDisposed()) {
                return;
            }
            if (association != null) {
                wizard.newTestParameter(association.getName());
            }
        });
    }

    private boolean validatePage() {
        setErrorMessage(null);
        if (association == null) {
            setErrorMessage(NLS.bind(Messages.NewChildParamWizardPage_Error_AssociationDoesNotExists,
                    editFieldAssociation.getText()));
            return false;
        }
        if ("".equals(editFieldParamType.getText()) || "".equals(editFieldName.getText())) { //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }

        IStatus status = JavaConventions.validateFieldName(editFieldName.getText(), "1.3", "1.3"); //$NON-NLS-1$ //$NON-NLS-2$
        if (!status.isOK()) {
            setErrorMessage(NLS.bind(Messages.NewChildParamWizardPage_ValidationError_InvalidTestParameterName,
                    editFieldName.getText()));
            return false;
        }

        return wizard.isPageValid(PAGE_NUMBER);
    }

    /**
     * Updates the page complete status.
     */
    private void updateSetPageComplete() {
        boolean completeAllowed = validatePage();
        super.setPageComplete(completeAllowed);
    }

    /**
     * Informs the wizard that this page was displayed.
     */
    @Override
    public IWizardPage getNextPage() {
        wizard.setMaxPageShown(PAGE_NUMBER);
        return super.getNextPage();
    }

}
