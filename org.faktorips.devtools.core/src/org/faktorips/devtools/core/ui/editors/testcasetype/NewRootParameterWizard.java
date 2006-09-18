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
import org.eclipse.jface.wizard.Wizard;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.util.StringUtil;
import org.faktorips.util.memento.Memento;

/**
 * Wizard to create a root test policy cmpt type param or value datatype.
 * First page selection of value datatype or policy cmpt type.
 * Second page if value datatype: name and type
 *             if policy cmpt type: name, type, min instance, max instance and requires product
 * 
 * @author Joerg Ortmann
 */
public class NewRootParameterWizard extends Wizard implements IBlockedValidationWizard{
    private UIToolkit uiToolkit = new UIToolkit(null);

    //  Wizard pages
    private NewRootParamWizardPage rootParamSelectWizardPage;
    private NewTestParamDetailWizardPage rootParamDetailWizardPage;
    
    // Model objects
    private ITestCaseType testCaseType;
    private ITestParameter newTestParameter;
    
    // Contains a specific state of the test case type
    private Memento memento;
    
    // The maximum wizard page number which was displayed
    private int pageDisplayedMax = 0;

    // Controller to connect the model with the ui
    private IpsPartUIController controller;
    
    // Indicates if a test policy cmpt type parameter is created by the wizard (true) or a test
    // value parameter (false)
    boolean isTestPolicyCmptTypeParam = false;
    
    public NewRootParameterWizard(ITestCaseType testCaseType){
        super.setWindowTitle(Messages.NewRootParameterWizard_Title);
        this.testCaseType = testCaseType;
    }
    
    /**
     * {@inheritDoc}
     */
    public void addPages() {
        super.addPages();
        rootParamSelectWizardPage = new NewRootParamWizardPage(this);
        addPage(rootParamSelectWizardPage);
        
        rootParamDetailWizardPage = new NewTestParamDetailWizardPage(this, uiToolkit);
        addPage(rootParamDetailWizardPage);
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        return true;
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
    public ITestParameter getNewCreatedTestParameter(){
        return newTestParameter;
    }
    
    /**
     * Creates a new test parameter depends on the given datatype. The new test parameter could be a
     * test value parameter or a test policy cmpt type parameter.
     */
    public ITestParameter newTestParameter(Datatype newDatatype) {
        if (memento != null)
            testCaseType.setState(memento);
        memento = testCaseType.newMemento();
        
        isTestPolicyCmptTypeParam = false; 
        if (newDatatype instanceof ValueDatatype){
            newTestParameter = testCaseType.newInputTestValueParameter();
        } else if (newDatatype instanceof IPolicyCmptType){
            newTestParameter = testCaseType.newInputTestPolicyCmptTypeParameter();
            // root parameters have always 1 min and 1 max instances
            ((ITestPolicyCmptTypeParameter)newTestParameter).setMinInstances(1);
            ((ITestPolicyCmptTypeParameter)newTestParameter).setMaxInstances(1);
            isTestPolicyCmptTypeParam = true;
        } else{
            return null;
        }
        newTestParameter.setDatatype(newDatatype.getQualifiedName());
        newTestParameter.setName(StringUtil.unqualifiedName(newDatatype.getQualifiedName()));
        
        controller = new IpsPartUIController((IIpsObjectPart)newTestParameter);
        rootParamSelectWizardPage.connectToModel(controller, newTestParameter);
        if (isTestPolicyCmptTypeParam)
            rootParamDetailWizardPage.connectToModel(controller, newTestParameter);
        controller.updateUI();
        
        getContainer().updateButtons();
        
        return newTestParameter;
    }

    /**
     * Returns the last wizard page to specify the details of a test policy cmpt type param, only if
     * the selected datatype is kind of a policy cmpt and the current page is the first wizard page.
     * Otherwise return <code>null</code> (=no next page).
     * 
     * {@inheritDoc}
     */
    public IWizardPage getNextPage(IWizardPage page) {
        if (isTestPolicyCmptTypeParam && page.equals(rootParamSelectWizardPage))
            return rootParamDetailWizardPage;
        
        return null;
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
