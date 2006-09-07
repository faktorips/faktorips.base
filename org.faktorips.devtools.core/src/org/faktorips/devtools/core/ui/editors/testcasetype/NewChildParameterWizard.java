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
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.util.memento.Memento;

/**
 * Wizard to create a root test policy cmpt type param or value datatype.
 * First page: selection of value datatype or policy cmpt type and name and type
 * Second page: if value datatype: not enabled (visible)
 *              if policy cmpt type: min instance, max instance and requires product
 * 
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
    private IpsPartUIController controller;

    public NewChildParameterWizard(ITestCaseType testCaseType,
            ITestPolicyCmptTypeParameter parentTestPolicyCmptTypeParameter) {
        super.setWindowTitle(Messages.NewChildParameterWizard_Title);

        this.testCaseType = testCaseType;
        this.parentTestPolicyCmptTypeParameter = parentTestPolicyCmptTypeParameter;
    }

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        super.addPages();

        childParamSelectWizardPage = new NewChildParamWizardPage(this);
        addPage(childParamSelectWizardPage);

        detailWizardPage = new NewTestParamDetailWizardPage(this, uiToolkit);
        addPage(detailWizardPage);
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        return true;
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
        try {
            return parentTestPolicyCmptTypeParameter.findPolicyCmptType();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return null;
        }
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
    public ITestParameter getNewCreatedTestParameter(){
        return newTestParameter;
    }
    
    /**
     * Creates and returns a new child test parameter.<br>
     * The given relation specifies the new child name.
     */
    public ITestParameter newTestParameter(String relation) {
        if (memento != null)
            testCaseType.setState(memento);
        memento = testCaseType.newMemento();

        newTestParameter = parentTestPolicyCmptTypeParameter.newTestPolicyCmptTypeParamChild();

        newTestParameter.setRelation(relation);
        newTestParameter.setName(relation);
        // set the same role like the parent
        ITestPolicyCmptTypeParameter parent = (ITestPolicyCmptTypeParameter)newTestParameter.getParent();
        if (parent != null)
            newTestParameter.setTestParameterType(parent.getTestParameterType());
        
        controller = new IpsPartUIController((IIpsObjectPart)newTestParameter);
        childParamSelectWizardPage.connectToModel(controller, newTestParameter);
        detailWizardPage.connectToModel(controller, newTestParameter);
        controller.updateUI();

        getContainer().updateButtons();

        return newTestParameter;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPageValid(int pageNo) {
        if (pageNo < pageDisplayedMax)
            return true;

        if (newTestParameter == null)
            return false;

        try {
            return newTestParameter.isValid();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void postAsyncRunnable(Runnable r) {
        if (!getShell().isDisposed())
            getShell().getDisplay().asyncExec(r);
    }

    /**
     * {@inheritDoc}
     */
    public void setMaxPageShown(int pageNumber) {
        pageDisplayedMax = pageNumber;
    }

}
