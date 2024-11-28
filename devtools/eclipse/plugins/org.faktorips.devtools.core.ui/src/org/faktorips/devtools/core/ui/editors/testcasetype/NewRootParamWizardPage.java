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
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.Datatype;
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
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Wizard page to create a new root test policy cmpt type parameter.<br>
 * The following fields will be handled: Datatype (ValuaDatatype or policy cmpt type), parameter
 * name and the test parameter type (input, exp result or combined).
 *
 * @author Joerg Ortmann
 */
public class NewRootParamWizardPage extends WizardPage implements ValueChangeListener {

    protected static final int PAGE_NUMBER = 2;
    private static final String PAGE_ID = "RootParameterSelection"; //$NON-NLS-1$

    private NewRootParameterWizard wizard;

    private EditField<String> editFieldDatatypeOrRule;
    private EditField<String> editFieldName;
    private EnumField<TestParameterType> editFieldParamType;

    private String prevDatatypeOrRule;
    private String prevName;

    private Composite parentComposite;

    private Composite contentComposite;

    public NewRootParamWizardPage(NewRootParameterWizard wizard) {
        super(PAGE_ID, Messages.NewRootParamWizardPage_Title_TestPolicyCmptParam, null);
        setDescription(Messages.NewRootParamWizardPage_Description_TestPolicyCmptParam);
        this.wizard = wizard;
    }

    @Override
    public void createControl(Composite parent) {
        parentComposite = parent;

        UIToolkit uiToolkit = wizard.getUiToolkit();

        Composite c = uiToolkit.createLabelEditColumnComposite(parent);
        contentComposite = c;

        // in case of a rule parameter no reference browser necessary
        if (wizard.getKindOfTestParameter() != NewRootParameterWizard.TEST_RULE_PARAMETER) {
            uiToolkit.createLabel(c, Messages.NewRootParamWizardPage_Label_Datatype);
            createRefEditControl(c);
        }

        uiToolkit.createLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_Name);
        editFieldName = new TextField(uiToolkit.createText(c));
        editFieldName.addChangeListener(this);

        uiToolkit.createLabel(c, Messages.TestCaseTypeSection_EditFieldLabel_TestParameterType);
        createEditFieldParamType(c);

        setControl(c);
        validatePage();
        setPageComplete(false);
    }

    /**
     * Connects the edit fields with the given controller to the given test parameter
     */
    protected void connectToModel(BindingContext bindingContext, ITestParameter testParameter) {
        bindingContext.bindContent(editFieldName, testParameter, ITestParameter.PROPERTY_NAME);
        bindingContext.bindContent(editFieldParamType, testParameter, ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == editFieldDatatypeOrRule) {
            datatypeChanged(editFieldDatatypeOrRule.getText());
        }

        if (e.field == editFieldName) {
            nameChanged(editFieldName.getText());
        }
        wizard.postAsyncRunnable(() -> {
            Shell shell = wizard.getShell();
            if (shell == null || shell.isDisposed()) {
                return;
            }
            updateSetPageComplete();
        });
    }

    /**
     * Datatype or rule has changed.
     */
    private void datatypeChanged(String newDatatypeOrRule) {
        if (newDatatypeOrRule.equals(prevDatatypeOrRule) || IpsStringUtils.isEmpty(newDatatypeOrRule)) {
            return;
        }
        prevDatatypeOrRule = newDatatypeOrRule;

        if (wizard.getKindOfTestParameter() != NewRootParameterWizard.TEST_RULE_PARAMETER) {
            if (findDatatype(newDatatypeOrRule) == null) {
                return;
            }
            Datatype datatype = findDatatype(newDatatypeOrRule);
            wizard.newTestParameter(datatype);
        }
    }

    /**
     * Name of the rule parameter has changed.
     *
     * @param name the name of the rule parameter.
     */
    private void nameChanged(String name) {
        if (name.equals(prevName) || IpsStringUtils.isEmpty(name)) {
            return;
        }
        prevName = name;

        if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_RULE_PARAMETER) {
            wizard.newTestRuleParameter(name);
        }
    }

    /**
     * Finds and returns the datatype with the given name.
     */
    private Datatype findDatatype(String datatype) {
        return wizard.getTestCaseType().getIpsProject().findDatatype(datatype);
    }

    private boolean validatePage() {
        setErrorMessage(null);

        ITestParameter newTestParameter = wizard.getNewCreatedTestParameter();

        String datatypeOrRule = ""; //$NON-NLS-1$
        if (editFieldDatatypeOrRule != null && !editFieldDatatypeOrRule.getControl().isDisposed()) {
            datatypeOrRule = editFieldDatatypeOrRule.getText();
            if (IpsStringUtils.isEmpty(datatypeOrRule)) {
                setErrorMessage(Messages.NewRootParamWizardPage_Error_DatatypeFieldIsEmpty);
                return false;
            }
        }

        if (wizard.getKindOfTestParameter() != NewRootParameterWizard.TEST_RULE_PARAMETER) {
            if (findDatatype(datatypeOrRule) == null) {
                setErrorMessage(NLS.bind(Messages.NewRootParamWizardPage_Error_DatatypeDoesNotExists, datatypeOrRule));
                return false;
            }
        }

        if (editFieldName.getText().isEmpty()) {
            setErrorMessage(Messages.NewRootParamWizardPage_Error_NameFieldIsEmpty);
            return false;
        }

        IStatus status = JavaConventions.validateFieldName(editFieldName.getText(), "1.3", "1.3"); //$NON-NLS-1$ //$NON-NLS-2$
        if (!status.isOK()) {
            setErrorMessage(NLS.bind(Messages.NewRootParamWizardPage_ValidationError_InvalidTestParameterName,
                    editFieldName.getText()));
            return false;
        }

        if (editFieldParamType.getText().isEmpty()) {
            setErrorMessage(Messages.NewRootParamWizardPage_Error_ParamTypeFieldIsEmpty);
            return false;
        }

        return wizard.getKindOfTestParameter() != NewRootParameterWizard.TEST_POLICY_CMPT_TYPE_PARAMETER
                ? wizard.isPageValid(PAGE_NUMBER)
                : validatePolicyTypeParametersAndPage(newTestParameter);

    }

    private boolean validatePolicyTypeParametersAndPage(ITestParameter newTestParameter) {
        try {
            /*
             * special case for test policy cmpty type params check errors only for displayed
             * attributes, otherwise the next button is disabled if there is an error on the next
             * page (e.g. if the policycmpt is abstract and prod rel is not checked.) In all other
             * cases there is no validation association between the pages, and if the next page was
             * displayed once then the next button on the prev page is always enable (see above)
             */
            MessageList messageList = newTestParameter.validate(newTestParameter.getIpsProject());
            MessageList relevantMessages = messageList.getMessagesFor(newTestParameter,
                    ITestParameter.PROPERTY_DATATYPE);
            relevantMessages.add(messageList.getMessagesFor(newTestParameter, ITestParameter.PROPERTY_NAME));
            relevantMessages.add(messageList.getMessagesFor(newTestParameter,
                    ITestParameter.PROPERTY_TEST_PARAMETER_TYPE));
            if (relevantMessages.containsErrorMsg()) {
                setErrorMessage(relevantMessages.getFirstMessage(Message.ERROR).getText());
                return false;
            } else {
                return true;
            }
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
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

    protected void resetPage() {
        if (wizard.getBindingContext() != null) {
            wizard.getBindingContext().clear();
        }

        prevDatatypeOrRule = ""; //$NON-NLS-1$
        if (!editFieldDatatypeOrRule.getControl().isDisposed()) {
            editFieldDatatypeOrRule.setText(""); //$NON-NLS-1$
        }
        prevName = ""; //$NON-NLS-1$
        if (!editFieldName.getControl().isDisposed()) {
            editFieldName.setText(""); //$NON-NLS-1$
        }

        contentComposite.dispose();
        createControl(parentComposite);

        parentComposite.pack();
        parentComposite.getParent().layout();

        // in case of a rule parameter create the test rule parameter, no further input is necessary
        if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_RULE_PARAMETER) {
            wizard.newTestRuleParameter("");
        }

        setErrorMessage(null);
    }

    /**
     * Creates the datatype or rule browse control
     */
    private void createRefEditControl(Composite c) {
        if (editFieldDatatypeOrRule != null) {
            editFieldDatatypeOrRule.getControl().dispose();
        }
        if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_POLICY_CMPT_TYPE_PARAMETER) {
            editFieldDatatypeOrRule = new TextButtonField(wizard.getUiToolkit().createPcTypeRefControl(
                    wizard.getTestCaseType().getIpsProject(), c));
        } else if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_VALUE_PARAMETER) {
            DatatypeRefControl datatypeRefControl = wizard.getUiToolkit().createDatatypeRefEdit(
                    wizard.getTestCaseType().getIpsProject(), c);
            datatypeRefControl.setOnlyValueDatatypesAllowed(true);
            editFieldDatatypeOrRule = new TextButtonField(datatypeRefControl);
        } else {
            throw new RuntimeException("Unsupported kind of test parameter!"); //$NON-NLS-1$
        }
        editFieldDatatypeOrRule.addChangeListener(this);
    }

    /**
     * Creates the type edit drop down, after creating this edit field the selection is removed
     */
    private void createEditFieldParamType(Composite c) {
        if (editFieldParamType != null) {
            editFieldParamType.getControl().dispose();
        }
        TestParameterType[] allowedValues;
        if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_RULE_PARAMETER) {
            allowedValues = new TestParameterType[] { TestParameterType.EXPECTED_RESULT };
        } else if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_VALUE_PARAMETER) {
            allowedValues = new TestParameterType[] { TestParameterType.INPUT, TestParameterType.EXPECTED_RESULT };
        } else {
            allowedValues = TestParameterType.values();
        }
        editFieldParamType = new EnumField<>(wizard.getUiToolkit().createCombo(c), allowedValues);
        editFieldParamType.addChangeListener(this);
    }
}
