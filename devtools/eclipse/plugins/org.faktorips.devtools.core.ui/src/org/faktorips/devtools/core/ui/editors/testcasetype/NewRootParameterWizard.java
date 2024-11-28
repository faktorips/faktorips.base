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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.StringUtil;
import org.faktorips.util.memento.Memento;

/**
 * Wizard to create a root test policy cmpt type param or value datatype. First page selection of
 * value datatype or policy cmpt type. Second page if value datatype: name and type if policy cmpt
 * type: name, type, min instance, max instance and requires product
 *
 * @author Joerg Ortmann
 */
public class NewRootParameterWizard extends Wizard implements IBlockedValidationWizard {

    public static final int TEST_POLICY_CMPT_TYPE_PARAMETER = 0;
    public static final int TEST_VALUE_PARAMETER = 1;
    public static final int TEST_RULE_PARAMETER = 2;

    private UIToolkit uiToolkit = new UIToolkit(null);

    // Wizard pages
    private NewRootParamWizardPage rootParamSelectWizardPage;
    private NewTestParamDetailWizardPage rootParamDetailWizardPage;
    private NewRootParamFirstWizardPage rootParamFirstWizardPage;

    // Model objects
    private ITestCaseType testCaseType;
    private ITestParameter newTestParameter;

    /** Contains a specific state of the test case type */
    private Memento memento;

    /** The maximum wizard page number which was displayed */
    private int pageDisplayedMax = 0;

    /** Binding context to connect the model with the ui */
    private BindingContext bindingContext;

    /**
     * Indicates if a test policy cmpt type parameter is created by the wizard (true) or a test
     * value parameter (false)
     */
    private boolean isTestPolicyCmptTypeParam = false;

    private int kindOfTestParameter = TEST_POLICY_CMPT_TYPE_PARAMETER;

    public NewRootParameterWizard(ITestCaseType testCaseType) {
        super.setWindowTitle(Messages.NewRootParameterWizard_Title);
        this.testCaseType = testCaseType;
    }

    @Override
    public void addPages() {
        super.addPages();

        rootParamFirstWizardPage = new NewRootParamFirstWizardPage(this);
        addPage(rootParamFirstWizardPage);

        rootParamSelectWizardPage = new NewRootParamWizardPage(this);
        addPage(rootParamSelectWizardPage);

        rootParamDetailWizardPage = new NewTestParamDetailWizardPage(this, uiToolkit,
                NewRootParamWizardPage.PAGE_NUMBER + 1);
        addPage(rootParamDetailWizardPage);
    }

    @Override
    public boolean performFinish() {
        if (kindOfTestParameter == TEST_RULE_PARAMETER) {
            connectNewParameterToModel();
        }
        return true;
    }

    @Override
    public BindingContext getBindingContext() {
        return bindingContext;
    }

    /**
     * Returns the test case type.
     */
    public ITestCaseType getTestCaseType() {
        return testCaseType;
    }

    /**
     * Returns the ui toolkit.
     */
    public UIToolkit getUiToolkit() {
        return uiToolkit;
    }

    /**
     * Returns the new created test parameter.
     */
    public ITestParameter getNewCreatedTestParameter() {
        return newTestParameter;
    }

    /**
     * Creates a new test parameter depends on the given datatype. The new test parameter could be a
     * test value parameter or a test policy cmpt type parameter.
     */
    public ITestParameter newTestParameter(Datatype datatype) {
        createMemento();

        isTestPolicyCmptTypeParam = false;
        if (datatype instanceof ValueDatatype) {
            newTestParameter = testCaseType.newInputTestValueParameter();
        } else if (datatype instanceof IPolicyCmptType) {
            newTestParameter = testCaseType.newInputTestPolicyCmptTypeParameter();
            // root parameters have always 1 min and 1 max instances
            ((ITestPolicyCmptTypeParameter)newTestParameter).setMinInstances(1);
            ((ITestPolicyCmptTypeParameter)newTestParameter).setMaxInstances(1);
            isTestPolicyCmptTypeParam = true;
        } else {
            throw new RuntimeException("Instance of selected object not supported!"); //$NON-NLS-1$
        }

        newTestParameter.setDatatype(datatype.getQualifiedName());
        newTestParameter.setName(StringUtil.unqualifiedName(datatype.getQualifiedName()));

        connectNewParameterToModel();

        return newTestParameter;
    }

    /**
     * Creates a new test rule parameter
     *
     * @param name the name of the new rule parameter.
     *
     * @return the newly created test rule parameter as an {@link ITestParameter}.
     */
    public ITestParameter newTestRuleParameter(String name) {
        createMemento();

        isTestPolicyCmptTypeParam = false;
        newTestParameter = testCaseType.newExpectedResultRuleParameter();
        newTestParameter.setName(name);

        return newTestParameter;
    }

    /**
     * Connects the new test parameter to the model controller
     */
    private void connectNewParameterToModel() {
        bindingContext = new BindingContext();
        rootParamSelectWizardPage.connectToModel(bindingContext, newTestParameter);
        if (isTestPolicyCmptTypeParam) {
            rootParamDetailWizardPage.connectToModel(bindingContext, newTestParameter);
        }
        bindingContext.updateUI();

        getContainer().updateButtons();
    }

    /**
     * Creates a new memento for the test case type
     */
    private void createMemento() {
        if (memento != null) {
            testCaseType.setState(memento);
        }
        memento = testCaseType.newMemento();
    }

    /**
     * Returns the last wizard page to specify the details of a test policy cmpt type param, only if
     * the selected datatype is kind of a policy cmpt and the current page is the first wizard page.
     * Otherwise return <code>null</code> (=no next page).
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (isTestPolicyCmptTypeParam && page.equals(rootParamSelectWizardPage)) {
            rootParamDetailWizardPage.getControl().setFocus();
            return rootParamDetailWizardPage;
        } else if (page.equals(rootParamFirstWizardPage)) {
            rootParamSelectWizardPage.getControl().setFocus();
            return rootParamSelectWizardPage;
        }
        return null;
    }

    @Override
    public boolean isPageValid(int pageNo) {
        // special check for the last page, valid if no new test policy cmpt type param is chosen,
        // because in this case the page is not necessary
        // the page is valid if the next page was displayed
        if ((pageNo == 3 && kindOfTestParameter != TEST_POLICY_CMPT_TYPE_PARAMETER) || (pageNo < pageDisplayedMax)) {
            return true;
        }

        // if no new test parameter exists, the page is not valid
        if (newTestParameter == null) {
            return false;
        }

        return true;
    }

    @Override
    public void postAsyncRunnable(Runnable r) {
        if (!getShell().isDisposed()) {
            getShell().getDisplay().asyncExec(r);
        }
    }

    @Override
    public void setMaxPageShown(int pageNumber) {
        pageDisplayedMax = pageNumber;
    }

    /**
     * Rezurns the kind of the new test parameter
     */
    public int getKindOfTestParameter() {
        return kindOfTestParameter;
    }

    /**
     * Sets the kind of the new test parameter
     */
    public void setKindOfTestParameter(int kindOfTestParameter) {
        this.kindOfTestParameter = kindOfTestParameter;
    }

    /**
     * Resets the state of the wizard, means clear all fields
     */
    public void resetWizard() {
        if (newTestParameter != null && !newTestParameter.isDeleted()) {
            newTestParameter.delete();
        }

        rootParamSelectWizardPage.resetPage();
        rootParamDetailWizardPage.resetPage();

        setMaxPageShown(0);
    }

    /**
     * Sets the title and the description of the second wizard page.
     */
    public void setTitleAndDescriptionOfSecondPage(String title, String description) {
        rootParamSelectWizardPage.setTitle(title);
        rootParamSelectWizardPage.setDescription(description);
    }
}
