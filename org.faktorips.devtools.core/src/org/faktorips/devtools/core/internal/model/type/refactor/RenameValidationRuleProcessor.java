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
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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

    public RenameValidationRuleProcessor(IValidationRule rule) {
        super(rule, rule.getName());
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getValidationRule());
        addAffectedSrcFiles(modificationSet);
        updateValidationRuleInProductCmpts();
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
                validationRuleConfig.setValidationName(getNewName());
            }
        }
    }

    private void updateValidationRuleName() {
        getValidationRule().setName(getNewName());
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<IIpsSrcFile>();
        result.add(getValidationRule().getIpsSrcFile());
        if (getValidationRule().isConfigurableByProductComponent()) {
            for (IProductCmptGeneration productCmptGeneration : searchProductCmptGenerations()) {
                result.add(productCmptGeneration.getIpsSrcFile());
            }
        }
        return result;
    }

    private List<IProductCmptGeneration> getProductCmptGenerations() {
        return productCmptsGenerations;
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
