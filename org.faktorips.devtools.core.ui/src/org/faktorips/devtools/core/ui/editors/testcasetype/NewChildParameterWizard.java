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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.util.memento.Memento;

/**
 * @author Joerg Ortmann
 */
public class NewChildParameterWizard extends Wizard implements IBlockedValidationWizard {

    private UIToolkit uiToolkit = new UIToolkit(null);

    // Wizard pages
    private NewTestParamDetailWizardPage detailWizardPage;
    private NewChildParamWizardPage childParamSelectWizardPage;

    // Model objects
    private ITestCaseType testCaseType;
    private ITestPolicyCmptTypeParameter parentTestPolicyCmptTypeParameter;
    private ITestPolicyCmptTypeParameter newTestParameter;

    // Contains a specific state of the test case type
    private Memento memento;

    // The maximum wizard page number which was displayed
    private int pageDisplayedMax = 0;

    // Controller to connect the model with the ui
    private IpsObjectUIController controller;

    public NewChildParameterWizard(ITestCaseType testCaseType,
            ITestPolicyCmptTypeParameter parentTestPolicyCmptTypeParameter) {
        super.setWindowTitle(Messages.NewChildParameterWizard_Title);

        this.testCaseType = testCaseType;
        this.parentTestPolicyCmptTypeParameter = parentTestPolicyCmptTypeParameter;
    }

    @Override
    public void addPages() {
        super.addPages();

        childParamSelectWizardPage = new NewChildParamWizardPage(this);
        addPage(childParamSelectWizardPage);

        detailWizardPage = new NewTestParamDetailWizardPage(this, uiToolkit, NewChildParamWizardPage.PAGE_NUMBER + 1);
        addPage(detailWizardPage);
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    @Override
    public IpsObjectUIController getController() {
        return controller;
    }

    /**
     * Returns the the test case type the new parameter will be created for.
     */
    public ITestCaseType getTestCaseType() {
        return testCaseType;
    }

    /**
     * Returns the parent parameter of the new created child parameter.
     */
    public IPolicyCmptType getParentPolicyCmptType() {
        return parentTestPolicyCmptTypeParameter.findPolicyCmptType(testCaseType.getIpsProject());
    }

    /**
     * returns the ui toolkit.
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
     * Creates and returns a new child test parameter.<br>
     * The given association specifies the new child name.
     */
    public ITestParameter newTestParameter(String association) {
        if (memento != null) {
            testCaseType.setState(memento);
        }
        memento = testCaseType.newMemento();

        newTestParameter = parentTestPolicyCmptTypeParameter.newTestPolicyCmptTypeParamChild();

        newTestParameter.setAssociation(association);

        try {
            IPolicyCmptTypeAssociation modelAssociation = newTestParameter
                    .findAssociation(testCaseType.getIpsProject());
            if (modelAssociation != null) {
                IPolicyCmptType pcType = modelAssociation
                        .findTargetPolicyCmptType(parentTestPolicyCmptTypeParameter.getIpsProject());
                if (pcType != null) {
                    newTestParameter.setPolicyCmptType(pcType.getQualifiedName());
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        newTestParameter.setName(association);
        // set the same role like the parent
        ITestPolicyCmptTypeParameter parent = (ITestPolicyCmptTypeParameter)newTestParameter.getParent();
        if (parent != null) {
            newTestParameter.setTestParameterType(parent.getTestParameterType());
        }

        controller = new IpsObjectUIController(newTestParameter);
        childParamSelectWizardPage.connectToModel(controller);
        detailWizardPage.connectToModel(controller, newTestParameter);
        controller.updateUI();

        getContainer().updateButtons();

        return newTestParameter;
    }

    @Override
    public boolean isPageValid(int pageNo) {
        if (pageNo < pageDisplayedMax) {
            return true;
        }

        if (newTestParameter == null) {
            return false;
        }

        try {
            return newTestParameter.isValid(newTestParameter.getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return false;
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

}
