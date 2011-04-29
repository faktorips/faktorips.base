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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.enums.EnumValue;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Wizard page to create a new root test policy cmpt type parameter.<br>
 * The following fields will be handled: Datatype (ValuaDatatype or policy cmpt type), parameter
 * name and the test parameter type (input, exp result or combined).
 * 
 * @author Joerg Ortmann
 */
public class NewRootParamWizardPage extends WizardPage implements ValueChangeListener {

    private static final String PAGE_ID = "RootParameterSelection"; //$NON-NLS-1$
    protected static final int PAGE_NUMBER = 2;

    private NewRootParameterWizard wizard;

    private EditField<String> editFieldDatatypeOrRule;
    private EditField<String> editFieldName;
    private EditField<EnumValue> editFieldParamType;

    private String prevDatatypeOrRule;

    private Composite parentComposite;

    private Composite contentComposite;

    public NewRootParamWizardPage(NewRootParameterWizard wizard) {
        super(PAGE_ID, Messages.NewRootParamWizardPage_Title_TestPolicyCmptParam, null);
        this.setDescription(Messages.NewRootParamWizardPage_Description_TestPolicyCmptParam);
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

        setPageComplete(false);
    }

    /**
     * Connects the edit fields with the given controller to the given test parameter
     */
    protected void connectToModel(IpsObjectUIController controller, ITestParameter testParameter) {
        controller.add(editFieldName, ITestParameter.PROPERTY_NAME);
        controller.add(editFieldParamType, ITestParameter.PROPERTY_TEST_PARAMETER_TYPE);
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == editFieldDatatypeOrRule) {
            try {
                datatypeChanged(editFieldDatatypeOrRule.getText());
            } catch (CoreException ex) {
                IpsPlugin.logAndShowErrorDialog(ex);
            }
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
     * Datatype or rule has changed.
     */
    private void datatypeChanged(String newDatatypeOrRule) throws CoreException {
        if (newDatatypeOrRule.equals(prevDatatypeOrRule) || StringUtils.isEmpty(newDatatypeOrRule)) {
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
     * Finds and returns the datatype with the given name.
     */
    private Datatype findDatatype(String datatype) throws CoreException {
        return wizard.getTestCaseType().getIpsProject().findDatatype(datatype);
    }

    private boolean validatePage() throws CoreException {
        setErrorMessage(null);

        ITestParameter newTestParameter = wizard.getNewCreatedTestParameter();

        String datatypeOrRule = ""; //$NON-NLS-1$
        if (editFieldDatatypeOrRule != null && !editFieldDatatypeOrRule.getControl().isDisposed()) {
            datatypeOrRule = editFieldDatatypeOrRule.getText();
            if (StringUtils.isEmpty(datatypeOrRule)) {
                return false;
            }
        }

        if (wizard.getKindOfTestParameter() != NewRootParameterWizard.TEST_RULE_PARAMETER) {
            if (findDatatype(datatypeOrRule) == null) {
                setErrorMessage(NLS.bind(Messages.NewRootParamWizardPage_Error_DatatypeDoesNotExists, datatypeOrRule));
                return false;
            }
        }

        if ("".equals(editFieldParamType.getText()) || "".equals(editFieldName.getText())) { //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }

        IStatus status = JavaConventions.validateFieldName(editFieldName.getText());
        if (!status.isOK()) {
            setErrorMessage(NLS.bind(Messages.NewRootParamWizardPage_ValidationError_InvalidTestParameterName,
                    editFieldName.getText()));
            return false;
        }

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
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
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
     */
    @Override
    public IWizardPage getNextPage() {
        wizard.setMaxPageShown(PAGE_NUMBER);
        return super.getNextPage();
    }

    protected void resetPage() {
        if (wizard.getController() != null) {
            wizard.getController().remove(editFieldName);
            wizard.getController().remove(editFieldParamType);
        }

        prevDatatypeOrRule = ""; //$NON-NLS-1$
        if (!editFieldDatatypeOrRule.getControl().isDisposed()) {
            editFieldDatatypeOrRule.setText(""); //$NON-NLS-1$
        }
        editFieldName.setText(""); //$NON-NLS-1$

        contentComposite.dispose();
        createControl(parentComposite);

        parentComposite.pack();
        parentComposite.getParent().layout();

        // in case of a rule parameter create the test rule parameter, no further input is necessary
        if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_RULE_PARAMETER) {
            wizard.newTestRuleParameter();
        }

        setErrorMessage(null);
    }

    /**
     * Creates the datattyppe or rule browse control
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
        editFieldParamType = new EnumValueField(wizard.getUiToolkit().createCombo(c, TestParameterType.getEnumType()),
                TestParameterType.getEnumType());
        editFieldParamType.addChangeListener(this);

        // disable type drop down for test rule parameter, because only expected is allowed
        if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_RULE_PARAMETER) {
            editFieldParamType.getControl().setEnabled(false);
            // remove combined type entry in drop down, because combined is not allowed for a test
            // value parameter
            ((Combo)editFieldParamType.getControl()).remove(TestParameterType
                    .getIndexOfType(TestParameterType.COMBINED));
            ((Combo)editFieldParamType.getControl()).remove(TestParameterType.getIndexOfType(TestParameterType.INPUT));
        } else if (wizard.getKindOfTestParameter() == NewRootParameterWizard.TEST_VALUE_PARAMETER) {
            // remove combined type entry in drop down, because combined is not allowed for a test
            // value parameter
            ((Combo)editFieldParamType.getControl()).remove(TestParameterType
                    .getIndexOfType(TestParameterType.COMBINED));
        }
    }
}
