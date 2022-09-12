/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestRule;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.MessageList;

/**
 * Refactoring processor for the "Rename ValidationRule" refactoring.
 * 
 */
public final class RenameValidationRuleProcessor extends IpsRenameProcessor {

    private List<IProductCmptGeneration> affectedProductCmptsGenerations = new ArrayList<>();
    private List<ITestCase> affectedTestCases = new ArrayList<>();

    public RenameValidationRuleProcessor(IValidationRule rule) {
        super(rule, rule.getName());
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getValidationRule());
        try {
            addAffectedSrcFiles(modificationSet);
            updateValidationRuleInProductCmpts();
            updateValidationRuleInTestCases();
            updateValidationRuleName();
        } catch (IpsException e) {
            modificationSet.undo();
            throw e;
        }
        return modificationSet;
    }

    protected void updateValidationRuleInProductCmpts() {
        if (getValidationRule().isConfigurableByProductComponent()) {
            updateValidationRuleInGenerations();
        }

    }

    protected void updateValidationRuleInGenerations() {
        List<IProductCmptGeneration> productCmptGenerations = getProductCmptGenerations();
        for (IProductCmptGeneration productCmptGeneration : productCmptGenerations) {
            IValidationRuleConfig propertyValue = productCmptGeneration.getPropertyValue(getOriginalName(),
                    IValidationRuleConfig.class);
            if (propertyValue != null) {
                IValidationRuleConfig validationRuleConfig = propertyValue;
                validationRuleConfig.setValidationRuleName(getNewName());
            }
        }
    }

    private List<IProductCmptGeneration> getProductCmptGenerations() {
        return affectedProductCmptsGenerations;
    }

    protected void updateValidationRuleInTestCases() {
        List<ITestCase> testCases = getAffectedTestCases();
        for (ITestCase testCase : testCases) {
            ITestRule[] allTestRules = testCase.getTestRuleObjects();
            for (ITestRule testRule : allTestRules) {
                if (isAffectedTestRule(testRule)) {
                    testRule.setValidationRule(getNewName());
                }
            }
        }
    }

    private boolean isAffectedTestRule(ITestRule testRule) {
        return getValidationRule().getName().equals(testRule.getValidationRule());
    }

    private List<ITestCase> getAffectedTestCases() {
        return affectedTestCases;
    }

    protected void updateValidationRuleName() {
        getValidationRule().setName(getNewName());
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<>();
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
            affectedProductCmptsGenerations = new ArrayList<>();
            IProductCmptType productCmptType = getValidationRule().findProductCmptType(getIpsProject());
            if (productCmptType != null) {
                Collection<IIpsSrcFile> productComponents = productCmptType.searchProductComponents(true);
                for (IIpsSrcFile ipsSrcFile : productComponents) {
                    if (ipsSrcFile != null) {
                        IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
                        affectedProductCmptsGenerations.addAll(productCmpt.getProductCmptGenerations());
                    }
                }
            }
            return affectedProductCmptsGenerations;
        } catch (IpsException e) {
            IpsPlugin.log(e);
            return Collections.emptyList();
        }
    }

    private void searchAffectedTestCases(HashSet<IIpsSrcFile> result) {
        List<ITestCase> testCases = searchTestCases();
        for (ITestCase testCase : testCases) {
            result.add(testCase.getIpsSrcFile());
        }
    }

    private List<ITestCase> searchTestCases() {
        affectedTestCases = new ArrayList<>();
        List<ITestCase> allTestCases = getAllTestCases();
        for (ITestCase testCase : allTestCases) {
            if (isTestCaseUsingValidationRule(testCase)) {
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
        List<ITestCase> allTestCases = new ArrayList<>();
        IIpsProject[] ipsProjects = getValidationRule().getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject ipsProject : ipsProjects) {
            List<IIpsSrcFile> testCaseSrcFiles = ipsProject.findAllIpsSrcFiles(IpsObjectType.TEST_CASE);
            for (IIpsSrcFile srcFile : testCaseSrcFiles) {
                allTestCases.add((ITestCase)srcFile.getIpsObject());
            }
        }
        return allTestCases;
    }

    private boolean isTestCaseUsingValidationRule(ITestCase testCase) {
        IValidationRule foundValidationRule = testCase.findValidationRule(getValidationRule().getName(),
                testCase.getIpsProject());
        return foundValidationRule != null && foundValidationRule.equals(getValidationRule());
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) {
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
