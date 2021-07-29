/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcasecopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.wizards.ResizableWizard;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestObject;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.testcasetype.TestParameterType;

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

    private List<IIpsPackageFragment> packageFrgmtsCreatedByWizard = new ArrayList<>(5);

    public TestCaseCopyWizard(ITestCase sourceTestCase) {
        super("TestCaseCopyWizard", IpsUIPlugin.getDefault().getDialogSettings(), 600, 800); //$NON-NLS-1$

        this.sourceTestCase = sourceTestCase;

        toolkit = new UIToolkit(null);

        super.setWindowTitle(Messages.TestCaseCopyWizard_title);
        super.setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewTestCaseCopyWizard.png")); //$NON-NLS-1$
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
    }

    @Override
    public void addPages() {
        testCaseCopyDestinationPage = new TestCaseCopyDesinationPage(toolkit);
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
        final IIpsPackageFragment targetIpsPackageFragment = testCaseCopyDestinationPage.getTargetIpsPackageFragment();
        if (targetIpsPackageFragment == null) {
            throw new RuntimeException("Target package fragment not specified!"); //$NON-NLS-1$
        }
        try {
            createIpsPackageFragment(targetIpsPackageFragment);
        } catch (IpsException e1) {
            throw new RuntimeException("Target package fragment couldn't be created!"); //$NON-NLS-1$
        }

        ICoreRunnable runnable = new CreateNewTargetTestCase(targetIpsPackageFragment);
        IIpsModel.get().runAndQueueChangeEvents(runnable, null);

        testCaseCopyDestinationPage.setNeedRecreateTarget(false);
    }

    /**
     * Replace all product cmpt (including children)
     */
    private void replaceAllProductCmpts(ITestPolicyCmpt testPolicyCmpt, IProductCmpt newProductCmpt) {

        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(newProductCmpt.getQualifiedName());

        String newVersionId = newProductCmpt.getVersionId();
        replaceChildsProductCmpts(testPolicyCmpt, newProductCmpt, newVersionId);
    }

    private void replaceChildsProductCmpts(ITestPolicyCmpt testPolicyCmpt,
            IProductCmpt parentProductCmpt,
            String newVersionId) {
        IIpsProject ipsProject = targetTestCase.getIpsProject();
        IProductCmptNamingStrategy productCmptNamingStrategy = ipsProject.getProductCmptNamingStrategy();
        ITestPolicyCmptLink[] testPolicyCmptlinks = testPolicyCmpt.getTestPolicyCmptLinks();
        for (ITestPolicyCmptLink testPolicyCmptlink : testPolicyCmptlinks) {
            ITestPolicyCmpt testPolicyCmptChild = testPolicyCmptlink.findTarget();
            if (testPolicyCmptChild != null) {
                IProductCmpt productCmptChild = testPolicyCmptChild.findProductCmpt(testPolicyCmpt.getIpsProject());
                if (productCmptChild == null) {
                    continue;
                }
                String kindId = productCmptNamingStrategy.getKindId(productCmptChild.getName());
                String newProductCmptName = productCmptNamingStrategy.getProductCmptName(kindId, newVersionId);

                ITestPolicyCmptTypeParameter parameter = testPolicyCmptChild
                        .findTestPolicyCmptTypeParameter(testPolicyCmptChild.getIpsProject());
                IIpsSrcFile[] allowedProductCmpt = parameter.getAllowedProductCmpt(ipsProject, parentProductCmpt);
                IProductCmpt newProductCmptChild = null;
                for (IIpsSrcFile element : allowedProductCmpt) {
                    IProductCmpt productCmptCandidate = (IProductCmpt)element.getIpsObject();
                    if (productCmptCandidate.getName().equals(newProductCmptName)) {
                        newProductCmptChild = productCmptCandidate;
                        break;
                    }
                }
                if (newProductCmptChild != null) {
                    testPolicyCmptChild.setProductCmptAndNameAfterIfApplicable(newProductCmptChild.getQualifiedName());
                    productCmptChild = newProductCmptChild;
                }
                replaceChildsProductCmpts(testPolicyCmptChild, productCmptChild, newVersionId);
            }
        }
    }

    private void createIpsPackageFragment(IIpsPackageFragment ipsPackageFragment) {
        if (!ipsPackageFragment.exists()) {
            IIpsPackageFragment parentIpsPackageFragment = ipsPackageFragment.getParentIpsPackageFragment();
            createIpsPackageFragment(parentIpsPackageFragment);
            IIpsPackageFragment fragment = parentIpsPackageFragment.createSubPackage(
                    ipsPackageFragment.getLastSegmentName(), true, null);
            packageFrgmtsCreatedByWizard.add(fragment);
        }
    }

    String getTargetTestCaseQualifiedName() {
        IIpsPackageFragment targetIpsPackageFragment = testCaseCopyDestinationPage.getTargetIpsPackageFragment();
        if (targetIpsPackageFragment == null) {
            return null;
        }

        String pckFrgmtName = targetIpsPackageFragment.getName();
        return (pckFrgmtName.length() > 0 ? pckFrgmtName + "." : "") //$NON-NLS-1$ //$NON-NLS-2$
                + testCaseCopyDestinationPage.getTargetTestCaseName();
    }

    @Override
    public boolean performFinish() {
        try {
            deleteUnselectedTestObjects();
            clearTestValues();
            targetTestCase.getIpsSrcFile().save(null);
            IpsUIPlugin.getDefault().openEditor(targetTestCase);
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }

    private void clearTestValues() {
        if (testCaseCopyDestinationPage.isClearExpectedTestValues()) {
            targetTestCase.clearTestValues(TestParameterType.EXPECTED_RESULT);
        }
        if (testCaseCopyDestinationPage.isClearInputTestValues()) {
            targetTestCase.clearTestValues(TestParameterType.INPUT);
        }
    }

    private void deleteUnselectedTestObjects() {
        IIpsModel.get().runAndQueueChangeEvents(new DeleteUnselectedTestObjects(), null);
    }

    @Override
    public boolean performCancel() {
        if (targetTestCase != null) {
            deleteTestCase(targetTestCase);
        }
        return true;
    }

    private void deletePackageFragments() {
        for (IIpsPackageFragment fragment : packageFrgmtsCreatedByWizard) {
            if (fragment.exists()) {
                try {
                    fragment.getEnclosingResource().delete(null);
                } catch (IpsException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
        packageFrgmtsCreatedByWizard.clear();
    }

    void deleteTestCase(ITestCase testCase) {
        try {
            ((IpsModel)testCase.getIpsModel()).removeIpsSrcFileContent(testCase.getIpsSrcFile());
            testCase.getEnclosingResource().delete(null);
            deletePackageFragments();
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    /**
     * Returns <code>true</code> if the product cmpts to replace have changed.
     */
    public boolean isReplaceParameterChanged() {
        return testCaseCopyDestinationPage.isNeedRecreateTarget();
    }

    private final class CreateNewTargetTestCase implements ICoreRunnable {
        private final IIpsPackageFragment targetIpsPackageFragment;

        private CreateNewTargetTestCase(IIpsPackageFragment targetIpsPackageFragment) {
            this.targetIpsPackageFragment = targetIpsPackageFragment;
        }

        @Override
        public void run(IProgressMonitor monitor) {
            IIpsSrcFile targetTestCaseSrcFile = sourceTestCase.createCopy(targetIpsPackageFragment,
                    testCaseCopyDestinationPage.getTargetTestCaseName(), true, null);
            targetTestCase = (ITestCase)targetTestCaseSrcFile.getIpsObject();
            // replace product cmpts of root objects
            ITestObject[] testObjects = sourceTestCase.getTestObjects();
            ITestObject[] testObjectsTarget = targetTestCase.getTestObjects();
            for (int i = 0; i < testObjects.length; i++) {
                if (testObjects[i] instanceof ITestPolicyCmpt) {
                    ITestPolicyCmpt testPolicyCmpt = (ITestPolicyCmpt)testObjects[i];
                    ITestPolicyCmptTypeParameter parameter = testPolicyCmpt
                            .findTestPolicyCmptTypeParameter(testPolicyCmpt.getIpsProject());
                    if (parameter == null || !parameter.isRequiresProductCmpt()) {
                        continue;
                    }
                    IProductCmpt productCmpt = testPolicyCmpt.findProductCmpt(testPolicyCmpt.getIpsProject());
                    if (productCmpt == null) {
                        continue;
                    }
                    IIpsSrcFile newProductCmptScrFile = testCaseCopyDestinationPage
                            .getProductCmptToReplace(testPolicyCmpt);
                    if (newProductCmptScrFile == null) {
                        // no change of product cmpt
                        continue;
                    }
                    IProductCmpt newProductCmpt = (IProductCmpt)newProductCmptScrFile.getIpsObject();
                    if (productCmpt.equals(newProductCmpt)) {
                        // same product cmpt, product cmpt will not be changed
                        continue;
                    }

                    // replace all product cmpts
                    // (because the target is a copy of the source we can use the same
                    // index: testObjectsTarget[i])
                    replaceAllProductCmpts((ITestPolicyCmpt)testObjectsTarget[i], newProductCmpt);
                }
            }
        }
    }

    private final class DeleteUnselectedTestObjects implements ICoreRunnable {
        @Override
        public void run(IProgressMonitor monitor) {
            ITestObject[] testObjects;
            testObjects = targetTestCase.getAllTestObjects();

            List<ITestObject> testObjectsList = new ArrayList<>(testObjects.length);
            testObjectsList.addAll(Arrays.asList(testObjects));

            Object[] checkedObjects = testCaseStructurePage.getCheckedObjects();
            for (Object checkedObject : checkedObjects) {
                if (checkedObject instanceof ITestObject) {
                    testObjectsList.remove(checkedObject);
                }
            }

            for (ITestObject toDeleteTestObj : testObjectsList) {
                if (toDeleteTestObj.getParent() != null) {
                    toDeleteTestObj.delete();
                }
            }
        }
    }
}
