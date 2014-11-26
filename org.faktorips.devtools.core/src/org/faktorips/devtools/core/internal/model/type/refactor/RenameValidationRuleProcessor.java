/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestObject;
import org.faktorips.devtools.core.model.testcase.ITestRule;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.util.message.MessageList;

/**
 * Refactoring processor for the "Rename ValidationRule" refactoring.
 * 
 */
public final class RenameValidationRuleProcessor extends IpsRenameProcessor {

    private List<IProductCmptGeneration> productCmptsGenerations = new ArrayList<IProductCmptGeneration>();
    private List<ITestCase> affectedTestCases = new ArrayList<ITestCase>();

    public RenameValidationRuleProcessor(IValidationRule rule) {
        super(rule, rule.getName());
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getValidationRule());
        addAffectedSrcFiles(modificationSet);
        updateValidationRuleInProductCmpts();
        updateValidationRuleInTestCases();
        updateValidationRuleName();
        return modificationSet;
    }

    private void updateValidationRuleInProductCmpts() {
        if (getValidationRule().isConfigurableByProductComponent()) {
            updateValidationRuleInGenerations();
        }

    }

    private void updateValidationRuleInGenerations() {
        List<IProductCmptGeneration> productCmptGenerations = getProductCmptGenerations();
        for (IProductCmptGeneration productCmptGeneration : productCmptGenerations) {
            IPropertyValue propertyValue = productCmptGeneration.getPropertyValue(getOriginalName());
            if (propertyValue != null) {
                IValidationRuleConfig validationRuleConfig = (IValidationRuleConfig)propertyValue;
                validationRuleConfig.setValidationRuleName(getNewName());
            }
        }
    }

    private List<IProductCmptGeneration> getProductCmptGenerations() {
        return productCmptsGenerations;
    }

    private void updateValidationRuleInTestCases() {
        List<ITestCase> testCases = getAffectedTestCases();
        for (ITestCase testCase : testCases) {
            try {
                ITestObject[] allTestObjects = testCase.getAllTestObjects();
                for (ITestObject testObject : allTestObjects) {
                    if (testObject instanceof ITestRule) {
                        ITestRule testRule = (ITestRule)testObject;
                        String testCaseValidationRule = testRule.getValidationRule();
                        if (getValidationRule().getName().equals(testCaseValidationRule)) {
                            testRule.setValidationRule(getNewName());
                        }
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    private List<ITestCase> getAffectedTestCases() {
        return affectedTestCases;
    }

    private void updateValidationRuleName() {
        getValidationRule().setName(getNewName());
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<IIpsSrcFile>();
        result.add(getValidationRule().getIpsSrcFile());
        searchAffectedProductCmptGenerations(result);
        searchAffectedTestCases(result);
        return result;
    }

    private void searchAffectedProductCmptGenerations(HashSet<IIpsSrcFile> result) {
        if (getValidationRule().isConfigurableByProductComponent()) {
            for (IProductCmptGeneration productCmptGeneration : searchProductCmptGenerations()) {
                result.add(productCmptGeneration.getIpsSrcFile());
            }
        }
    }

    private List<IProductCmptGeneration> searchProductCmptGenerations() {
        try {
            productCmptsGenerations = new ArrayList<IProductCmptGeneration>();
            IProductCmptType productCmptType = getValidationRule().findProductCmptType(getIpsProject());
            if (productCmptType != null) {
                Collection<IIpsSrcFile> productComponents = productCmptType.searchProductComponents(true);
                for (IIpsSrcFile ipsSrcFile : productComponents) {
                    if (ipsSrcFile != null) {
                        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                        productCmptsGenerations.addAll(productCmpt.getProductCmptGenerations());
                    }
                }
            }
            return productCmptsGenerations;
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return Collections.emptyList();
        }
    }

    private void searchAffectedTestCases(HashSet<IIpsSrcFile> result) {
        for (ITestCase testCase : searchTestCases()) {
            result.add(testCase.getIpsSrcFile());
        }
    }

    private List<ITestCase> searchTestCases() {
        affectedTestCases = new ArrayList<ITestCase>();
        List<ITestCase> allTestCases = getAllTestCases();
        for (ITestCase testCase : allTestCases) {
            if (isTestCaseContainingValidationRule(testCase)) {
                affectedTestCases.add(testCase);
            }
        }
        return affectedTestCases;
    }

    /**
     * Returns all existing {@link ITestCase}s that are contained in the {@link IIpsProject} of the
     * renamed validation rule. It also takes all referencing {@link IIpsProject}s into account.
     */
    private List<ITestCase> getAllTestCases() {
        List<ITestCase> allTestCases = getValidationRule().getIpsProject().getAllTestCases();
        addTestCasesFromReferencingIpsProjects(allTestCases);
        return allTestCases;
    }

    private void addTestCasesFromReferencingIpsProjects(List<ITestCase> allTestCases) {
        IIpsProject[] allReferencingIpsProjects = getValidationRule().getIpsProject().findReferencingProjects(true);
        for (IIpsProject ipsProject : allReferencingIpsProjects) {
            allTestCases.addAll((ipsProject.getAllTestCases()));
        }
    }

    private boolean isTestCaseContainingValidationRule(ITestCase testCase) {
        IValidationRule foundValidationRule;
        try {
            foundValidationRule = testCase.findValidationRule(getValidationRule().getName(), testCase.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return foundValidationRule != null && foundValidationRule.equals(getValidationRule());
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) throws CoreException {
        validationMessageList.add(getValidationRule().validate(getIpsProject()));
        validationMessageList.add(getType().validate(getIpsProject()));
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameValidationRuleProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameValidationRuleProcessor_processorName;
    }

    private IValidationRule getValidationRule() {
        return (IValidationRule)getIpsElement();
    }

    private IType getType() {
        return getValidationRule().getType();
    }

}
