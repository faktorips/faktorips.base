/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcasetype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.TestParameterType;
import org.faktorips.devtools.core.ui.UIToolkit;

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
        this.testPolicyCmptTypeParameter = parentTestPolicyCmptTypeParameter;
        this.showSubtypeAttributes = showSubtypeAttributes;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }

    private void addTestAttributes(ITestPolicyCmptTypeParameter testPolicyCmptTypeParam,
            IPolicyCmptTypeAttribute[] attributesSelected) {
        ITestAttribute testAttribute = null;
        for (IPolicyCmptTypeAttribute modelAttribute : attributesSelected) {
            try {
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
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            testAttribute.setAttribute(modelAttribute);
            testAttribute.setName(testCaseType.generateUniqueNameForTestAttribute(testAttribute, modelAttribute
                    .getName()));

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

    public void setModelTestAttribute(boolean modelTestAttribute) {

    }

    public IIpsProject getIpsProjekt() {
        return testCaseType.getIpsProject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page instanceof NewTestAttributeWizardPage) {
            if (isBasedOnPolicyCmptTypeAttributes()) {
                return testAttributeSelectionWizardPage;
            } else {
                return testAttributeDefinitionWizardPage;
            }
        } else if (page instanceof TestAttributeSelectionWizardPage) {
            secondPageWasdisplayed = true;
            return null;
        } else if (page instanceof TestAttributeDefinitionWizardPage) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canFinish() {
        if (!isBasedOnPolicyCmptTypeAttributes() && !testAttributeDefinitionWizardPage.isValid()) {
            return false;
        } else if (isBasedOnPolicyCmptTypeAttributes() && !testAttributeSelectionWizardPage.isValid()) {
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
        this.showSubtypeAttributes = showSubtypes;
    }
}
