/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.EnumField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;

/**
 * Wizard page to create a new child test policy cmpt type parameter.<br>
 * The following fields will be handled: Association, target policy cmpt type, test policy cmpt type
 * parameter name and the test parameter type (input, exp result or combined).
 * 
 * @author Joerg Ortmann
 */
public class NewChildParamWizardPage extends WizardPage implements ValueChangeListener {

    private static final String PAGE_ID = "RootParameterSelection"; //$NON-NLS-1$
    protected static final int PAGE_NUMBER = 1;

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
        this.setDescription(Messages.NewChildParamWizardPage_Description);
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
        editFieldParamType = new EnumField<TestParameterType>(uiToolkit.createCombo(c), TestParameterType.class);
        editFieldParamType.addChangeListener(this);

        setControl(c);
    }

    /**
     * Connects the edit fields with the given controller to the given test parameter
     */
    void connectToModel(IpsObjectUIController controller) {
        controller.add(editFieldAssociation, ITestPolicyCmptTypeParameter.PROPERTY_ASSOCIATION);
        controller.add(editFieldTarget, ITestPolicyCmptTypeParameter.PROPERTY_POLICYCMPTTYPE);
        controller.add(editFieldName, ITestParameter.PROPERTY_NAME);
        controller.add(editFieldParamType, ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == editFieldAssociation) {
            associationChanged(editFieldAssociation.getText());
        }

        wizard.postAsyncRunnable(new Runnable() {
            @Override
            public void run() {
                if (wizard.getShell().isDisposed()) {
                    return;
                }
                updateSetPageComplete();
            }
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
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        postNewTestParameter();
    }

    /**
     * Creates a new test parameter in an asynchronous manner.
     */
    private void postNewTestParameter() {
        wizard.postAsyncRunnable(new Runnable() {
            @Override
            public void run() {
                if (wizard.getShell().isDisposed()) {
                    return;
                }
                if (association != null) {
                    wizard.newTestParameter(association.getName());
                }
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

        IStatus status = JavaConventions.validateFieldName(editFieldName.getText());
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
        boolean completeAllowed = false;
        completeAllowed = validatePage();
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
