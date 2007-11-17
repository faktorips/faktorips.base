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

package org.faktorips.devtools.core.ui.wizards.testcasecopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;

/**
 * Wizard to copy a given test case.
 *
 * @author Joerg Ortmann
 */
public class TestCaseCopyWizard extends ResizableWizard {
    private ITestCase sourceTestCase;
    private ITestCase targetTestCase;

    private UIToolkit toolkit;
    private TestCaseCopyDesinationPage testCaseCopyDestinationPage;
    private TestCaseStructurePage testCaseStructurePage;

    private List packageFrgmtsCreatedByWizard = new ArrayList(5);

    public TestCaseCopyWizard(ITestCase sourceTestCase) {
        super("TestCaseCopyWizard", IpsPlugin.getDefault().getDialogSettings(), 600, 800); //$NON-NLS-1$

        this.sourceTestCase = sourceTestCase;

        toolkit = new UIToolkit(null);

        super.setWindowTitle(Messages.TestCaseCopyWizard_title);
        super.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewTestCaseCopyWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
    }

    /**
     * {@inheritDoc}
     */
    public void addPages() {
        testCaseCopyDestinationPage  = new TestCaseCopyDesinationPage(toolkit);
        addPage(testCaseCopyDestinationPage);

        testCaseStructurePage = new TestCaseStructurePage(toolkit, sourceTestCase.getIpsProject());
        addPage(testCaseStructurePage);
    }

    /**
     * @return Returns the sourceTestCase.
     */
    public ITestCase getSourceTestCase() {
        return sourceTestCase;
    }

    /**
     * @param targetTestCase The targetTestCase to set.
     */
    public void setTargetTestCase(ITestCase targetTestCase) {
        this.targetTestCase = targetTestCase;
    }

    /**
     * @return Returns the targetTestCase.
     */
    public ITestCase getTargetTestCase() {
        return targetTestCase;
    }

    /**
     * Creates the new target test case based on the source test case.
     */
    void createNewTargetTestCase() {
        IIpsPackageFragment targetIpsPackageFragment = testCaseCopyDestinationPage.getTargetIpsPackageFragment();
        if (targetIpsPackageFragment == null){
            throw new RuntimeException("Target package fragment not specified!"); //$NON-NLS-1$
        }
        try {
            createIpsPackageFragment(targetIpsPackageFragment);
        } catch (CoreException e1) {
            throw new RuntimeException("Target package fragment couldn't be created!"); //$NON-NLS-1$
        }

        try {
            IIpsSrcFile targetTestCaseSrcFile = targetIpsPackageFragment.createIpsFileFromTemplate(testCaseCopyDestinationPage.getTargetTestCaseName(),
                    sourceTestCase, null, true, null);
            targetTestCase = (ITestCase)targetTestCaseSrcFile.getIpsObject();
            // replace product cmpts of root objetcs
            ITestObject[] testObjects = sourceTestCase.getTestObjects();
            ITestObject[] testObjectsTarget = targetTestCase.getTestObjects();
            for (int i = 0; i < testObjects.length; i++) {
                if (testObjects[i] instanceof ITestPolicyCmpt){
                    ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)testObjects[i];
                    ITestPolicyCmptTypeParameter parameter = testPolicyCmpt.findTestPolicyCmptTypeParameter(testPolicyCmpt.getIpsProject());
                    if (parameter == null || !parameter.isRequiresProductCmpt()){
                        continue;
                    }
                    IProductCmpt productCmpt = testPolicyCmpt.findProductCmpt(testPolicyCmpt.getIpsProject());
                    if (productCmpt == null){
                        continue;
                    }
                    IIpsSrcFile newProductCmptScrFile = testCaseCopyDestinationPage.getProductCmptToReplace(testPolicyCmpt);
                    if (newProductCmptScrFile == null){
                        // no change of product cmpt
                        continue;
                    }
                    IProductCmpt newProductCmpt = (IProductCmpt)newProductCmptScrFile.getIpsObject();
                    if (productCmpt.equals(newProductCmpt)){
                        // same product cmpt, product cmpt will not changed
                        continue;
                    }

                    // replace all product cmpts
                    // (because the target is a copy of the source we could use the same index: testObjectsTarget[i])
                    replaceAllProductCmpts((ITestPolicyCmpt)testObjectsTarget[i], productCmpt, newProductCmpt);
                }
            }
            testCaseCopyDestinationPage.setNeedRecreateTarget(false);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /*
     * Replace all product cmpt (inclusive childs)
     */
    private void replaceAllProductCmpts(ITestPolicyCmpt testPolicyCmpt, IProductCmpt productCmpt, IProductCmpt newProductCmpt) throws CoreException {
        ITestCase testCase = testPolicyCmpt.getTestCase();
        testPolicyCmpt.setProductCmpt(newProductCmpt.getQualifiedName());
        testPolicyCmpt.setName(testCase.generateUniqueNameForTestPolicyCmpt(testPolicyCmpt, newProductCmpt.getName()));

        String newVersionId = newProductCmpt.getVersionId();
        replaceChildsProductCmpts(testPolicyCmpt, newProductCmpt, newVersionId);
    }

    private void replaceChildsProductCmpts(ITestPolicyCmpt testPolicyCmpt, IProductCmpt parentProductCmpt, String newVersionId) throws CoreException{
        ITestCase testCase = testPolicyCmpt.getTestCase();
        IIpsProject ipsProject = targetTestCase.getIpsProject();
        IProductCmptNamingStrategy productCmptNamingStrategy = ipsProject.getProductCmptNamingStrategy();
        ITestPolicyCmptRelation[] testPolicyCmptRelations = testPolicyCmpt.getTestPolicyCmptRelations();
        for (int i = 0; i < testPolicyCmptRelations.length; i++) {
            ITestPolicyCmpt testPolicyCmptChild = testPolicyCmptRelations[i].findTarget();
            if (testPolicyCmptChild != null){
                IProductCmpt productCmptChild = testPolicyCmptChild.findProductCmpt(testPolicyCmpt.getIpsProject());
                if (productCmptChild == null){
                    continue;
                }
                String kindId = productCmptNamingStrategy.getKindId(productCmptChild.getName());
                String newProductCmptName = productCmptNamingStrategy.getProductCmptName(kindId, newVersionId);

                ITestPolicyCmptTypeParameter parameter = testPolicyCmptChild.findTestPolicyCmptTypeParameter(testPolicyCmptChild.getIpsProject());
                IIpsSrcFile[] allowedProductCmpt = parameter.getAllowedProductCmpt(ipsProject, parentProductCmpt);
                IProductCmpt newProductCmptChild = null;
                for (int j = 0; j < allowedProductCmpt.length; j++) {
                    IProductCmpt productCmptCandidate = (IProductCmpt)allowedProductCmpt[i].getIpsObject();
                    if (productCmptCandidate.getName().equals(newProductCmptName)){
                        newProductCmptChild = productCmptCandidate;
                        break;
                    }
                }
                if (newProductCmptChild != null){
                    testPolicyCmptChild.setProductCmpt(newProductCmptChild.getQualifiedName());
                    testPolicyCmptChild.setName(testCase.generateUniqueNameForTestPolicyCmpt(testPolicyCmptChild, newProductCmptChild.getName()));
                }

                replaceChildsProductCmpts(testPolicyCmptChild, productCmptChild, newVersionId);
            }
        }
    }

    private void createIpsPackageFragment(IIpsPackageFragment ipsPackageFragment) throws CoreException {
        if (!ipsPackageFragment.exists()){
            IIpsPackageFragment parentIpsPackageFragment = ipsPackageFragment.getParentIpsPackageFragment();
            createIpsPackageFragment(parentIpsPackageFragment);
            IIpsPackageFragment fragment = parentIpsPackageFragment.createSubPackage(ipsPackageFragment.getLastSegmentName(), true, null);
            packageFrgmtsCreatedByWizard.add(fragment);
        }
    }

    String getTargetTestCaseQualifiedName(){
        IIpsPackageFragment targetIpsPackageFragment = testCaseCopyDestinationPage.getTargetIpsPackageFragment();
        if (targetIpsPackageFragment == null){
            return null;
        }
        return targetIpsPackageFragment.getName() + "." + testCaseCopyDestinationPage.getTargetTestCaseName(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean performFinish() {
        // delete deselected test objects
        try {
            IpsPlugin.getDefault().getIpsModel().runAndQueueChangeEvents(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    ITestObject[] testObjects;
                    testObjects = targetTestCase.getAllTestObjects();

                    List testObjectsList = new ArrayList(testObjects.length);
                    testObjectsList.addAll(Arrays.asList(testObjects));

                    Object[] checkedObjects = testCaseStructurePage.getCheckedObjects();
                    for (int i = 0; i < checkedObjects.length; i++) {
                        if (checkedObjects[i] instanceof ITestObject) {
                            testObjectsList.remove(checkedObjects[i]);
                        }
                    }

                    for (Iterator iter = testObjectsList.iterator(); iter.hasNext();) {
                        ITestObject toDeleteTestObj = (ITestObject)iter.next();
                        if (toDeleteTestObj.getParent() != null) {
                            toDeleteTestObj.delete();
                        }
                    }
                }
            }, null);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        IpsPlugin.getDefault().openEditor(targetTestCase);

        return super.performFinish();
    }

    /**
     * {@inheritDoc}
     */
    public boolean performCancel() {
        if (targetTestCase != null){
            deleteTestCase(targetTestCase);
        }
        return true;
    }

    private void deletePackageFragments() {
        for (Iterator iter = packageFrgmtsCreatedByWizard.iterator(); iter.hasNext();) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)iter.next();
            if (fragment.exists()){
                try {
                    fragment.getEnclosingResource().delete(true, null);
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
        packageFrgmtsCreatedByWizard.clear();
    }

    void deleteTestCase(ITestCase testCase) {
        try {
            ((IpsModel)testCase.getIpsModel()).removeIpsSrcFileContent(testCase.getIpsSrcFile());
            testCase.getEnclosingResource().delete(true, null);
            deletePackageFragments();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns <code>true</code> if the product cmpts to replace have changed.
     */
    public boolean isReplaceParameterChanged() {
        return testCaseCopyDestinationPage.isNeedRecreateTarget();
    }
}
