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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;

/**
 * @author Joerg Ortmann
 */
public class NewTestAttributeWizard extends Wizard {

    private UIToolkit uiToolkit = new UIToolkit(null);

    // Wizard pages
    private NewTestAttributeWizardPage newTestAttributePage;

    // Model objects
    private ITestCaseType testCaseType;
    private ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;

    private TestAttributeSelectionWizardPage testAttributeSelectionWizardPage;

    private TestAttributeDefinitionWizardPage testAttributeDefinitionWizardPage;

    private ITestAttribute firstCreatedTestAttribute;

    private boolean secondPageWasdisplayed = false;

    private boolean showSubtypeAttributes;

    public NewTestAttributeWizard(ITestCaseType testCaseType,
            ITestPolicyCmptTypeParameter parentTestPolicyCmptTypeParameter, boolean showSubtypeAttributes) {

        super.setWindowTitle(Messages.NewTestAttributeWizard_wizardTitle);

        this.testCaseType = testCaseType;
        testPolicyCmptTypeParameter = parentTestPolicyCmptTypeParameter;
        this.showSubtypeAttributes = showSubtypeAttributes;
    }

    @Override
    public void addPages() {
        super.addPages();

        newTestAttributePage = new NewTestAttributeWizardPage(this);
        addPage(newTestAttributePage);

        testAttributeSelectionWizardPage = new TestAttributeSelectionWizardPage(this, showSubtypeAttributes);
        addPage(testAttributeSelectionWizardPage);

        testAttributeDefinitionWizardPage = new TestAttributeDefinitionWizardPage(this);
        addPage(testAttributeDefinitionWizardPage);
    }

    @Override
    public boolean performFinish() {
        try {
            if (isBasedOnPolicyCmptTypeAttributes()) {
                IPolicyCmptTypeAttribute[] pctas = testAttributeSelectionWizardPage.getSelection();
                if (pctas.length > 0) {
                    addTestAttributes(testPolicyCmptTypeParameter, pctas);
                }
            } else {
                ITestAttribute testAttribute = null;
                if (testAttributeDefinitionWizardPage.getTestParameterType() == TestParameterType.EXPECTED_RESULT) {
                    testAttribute = testPolicyCmptTypeParameter.newExpectedResultTestAttribute();
                } else {
                    testAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
                }
                testAttribute.setName(testAttributeDefinitionWizardPage.getTestAttributeName());
                testAttribute.setDatatype(testAttributeDefinitionWizardPage.getDatatype());
                firstCreatedTestAttribute = testAttribute;
            }
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }

    private void addTestAttributes(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam,
            IPolicyCmptTypeAttribute[] attributesSelected) {

        ITestAttribute testAttribute = null;
        for (IPolicyCmptTypeAttribute modelAttribute : attributesSelected) {
            if (testPolicyCmptTypeParam.isCombinedParameter()) {
                // if the type of the parent is combined
                // create a new expected if attribute is derived or computed
                if (modelAttribute.isDerived()) {
                    testAttribute = testPolicyCmptTypeParam.newExpectedResultTestAttribute();
                } else {
                    testAttribute = testPolicyCmptTypeParam.newInputTestAttribute();
                }
            } else if (testPolicyCmptTypeParam.isExpextedResultOrCombinedParameter()) {
                testAttribute = testPolicyCmptTypeParam.newExpectedResultTestAttribute();
            } else {
                // default is input
                testAttribute = testPolicyCmptTypeParam.newInputTestAttribute();
            }

            testAttribute.setAttribute(modelAttribute);
            testAttribute.setName(testCaseType.generateUniqueNameForTestAttribute(testAttribute,
                    modelAttribute.getName()));

            if (firstCreatedTestAttribute == null) {
                firstCreatedTestAttribute = testAttribute;
            }
        }
    }

    /**
     * Returns the the test case type the new parameter will be created for.
     */
    public ITestCaseType getTestCaseType() {
        return testCaseType;
    }

    public ITestPolicyCmptTypeParameter geTestPolicyCmptTypeParameter() {
        return testPolicyCmptTypeParameter;
    }

    /**
     * returns the ui toolkit.
     */
    public UIToolkit getUiToolkit() {
        return uiToolkit;
    }

    public IIpsProject getIpsProjekt() {
        return testCaseType.getIpsProject();
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page instanceof NewTestAttributeWizardPage) {
            if (isBasedOnPolicyCmptTypeAttributes()) {
                return testAttributeSelectionWizardPage;
            } else {
                return testAttributeDefinitionWizardPage;
            }
        } else if ((page instanceof TestAttributeSelectionWizardPage)
                || (page instanceof TestAttributeDefinitionWizardPage)) {
            secondPageWasdisplayed = true;
            return null;
        } else {
            return super.getNextPage(page);
        }
    }

    public boolean isBasedOnPolicyCmptTypeAttributes() {
        return newTestAttributePage.isBasedOnPolicyCmptTypeAttributes();
    }

    public ITestAttribute getNewlyCreatedTestAttribute() {
        return firstCreatedTestAttribute;
    }

    @Override
    public boolean canFinish() {
        if ((!isBasedOnPolicyCmptTypeAttributes() && !testAttributeDefinitionWizardPage.isValid())
                || (isBasedOnPolicyCmptTypeAttributes() && !testAttributeSelectionWizardPage.isValid())) {
            return false;
        }
        if (!secondPageWasdisplayed) {
            return false;
        }
        return super.canFinish();
    }

    public void kindOfTestAttrHasChanged() {
        testAttributeSelectionWizardPage.deselectAll();
        getContainer().updateButtons();
    }

    public boolean getShowSubtypeAttributes() {
        return showSubtypeAttributes;
    }

    public void setShowSubtypeAttributes(boolean showSubtypes) {
        showSubtypeAttributes = showSubtypes;
    }
}
