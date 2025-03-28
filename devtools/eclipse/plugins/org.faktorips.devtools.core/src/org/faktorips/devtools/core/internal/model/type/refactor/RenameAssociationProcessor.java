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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.runtime.MessageList;

/**
 * Refactoring processor for the "Rename Association" refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameAssociationProcessor extends IpsRenameProcessor {

    private final Set<IAssociation> derivedUnionSubsets;

    private Set<IIpsSrcFile> productCmptSrcFiles;

    private Set<IIpsSrcFile> testCaseTypeSrcFiles;

    public RenameAssociationProcessor(IAssociation association) {
        super(association, association.getName(), association.getTargetRolePlural());

        derivedUnionSubsets = new HashSet<>();
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<>();
        result.add(getAssociation().getIpsSrcFile());
        if (getAssociation() instanceof IPolicyCmptTypeAssociation policyCmptTypeAssociation) {
            IPolicyCmptTypeAssociation inverseAssociation = policyCmptTypeAssociation
                    .findInverseAssociation(policyCmptTypeAssociation.getIpsProject());
            if (inverseAssociation != null) {
                result.add(inverseAssociation.getIpsSrcFile());
            }

            testCaseTypeSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
            for (IIpsSrcFile ipsSrcFile : testCaseTypeSrcFiles) {
                result.add(ipsSrcFile);
            }

        } else if (getAssociation() instanceof IProductCmptTypeAssociation) {
            productCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT,
                    IpsObjectType.PRODUCT_TEMPLATE);
            for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
                result.add(ipsSrcFile);
            }
        }

        if (getAssociation().isDerivedUnion()) {
            Set<IIpsSrcFile> typeSrcFiles = findReferencingIpsSrcFiles(
                    getAssociation() instanceof IPolicyCmptTypeAssociation ? IpsObjectType.POLICY_CMPT_TYPE
                            : IpsObjectType.PRODUCT_CMPT_TYPE);
            for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
                IType type = (IType)ipsSrcFile.getIpsObject();
                for (IAssociation association : type.getAssociations()) {
                    if (association.getSubsettedDerivedUnion().equals(getOriginalName())) {
                        derivedUnionSubsets.add(association);
                        result.add(ipsSrcFile);
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) {
        validationMessageList.add(getAssociation().validate(getIpsProject()));
        validationMessageList.add(getType().validate(getIpsProject()));
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) {
        if (getNewName().isEmpty()) {
            status.addFatalError(Messages.RenameAssociationProcessor_msgNewNameMustNotBeEmpty);
            return;
        }

        if (getNewName().equals(getOriginalName()) && getNewPluralName().equals(getOriginalPluralName())) {
            status.addFatalError(Messages.RenameAssociationProcessor_msgEitherNameOrPluralNameMustBeChanged);
            return;
        }

        if (getAssociation().is1ToMany() && getNewPluralName().isEmpty()) {
            status.addFatalError(
                    Messages.RenameAssociationProcessor_msgNewPluralNameMustNotBeEmptyForToManyAssociations);
        }
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
        IpsRefactoringModificationSet modificationSet = new IpsRefactoringModificationSet(getIpsElement());
        try {
            addAffectedSrcFiles(modificationSet);
            if (getAssociation() instanceof IPolicyCmptTypeAssociation) {
                updateInverseAssociation();
                updateTestCaseTypeParameters();
            } else {
                updateProductCmptLinks();
            }
            if (getAssociation().isDerivedUnion()) {
                updateDerivedUnionSubsets();
            }

            updateTargetRoleSingular();
            updateTargetRolePlural();
        } catch (IpsException e) {
            modificationSet.undo();
            throw e;
        }
        return modificationSet;
    }

    private void updateTargetRoleSingular() {
        getAssociation().setTargetRoleSingular(getNewName());
    }

    private void updateTargetRolePlural() {
        getAssociation().setTargetRolePlural(getNewPluralName());
    }

    private void updateInverseAssociation() {
        IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)getAssociation();
        IPolicyCmptTypeAssociation inverseAssociation = policyCmptTypeAssociation
                .findInverseAssociation(policyCmptTypeAssociation.getIpsProject());
        if (inverseAssociation != null) {
            inverseAssociation.setInverseAssociation(getNewName());
        }
    }

    private void updateTestCaseTypeParameters() {
        for (IIpsSrcFile ipsSrcFile : testCaseTypeSrcFiles) {
            ITestCaseType testCaseType = (ITestCaseType)ipsSrcFile.getIpsObject();
            for (ITestPolicyCmptTypeParameter parameter : testCaseType.getTestPolicyCmptTypeParameters()) {
                /*
                 * Continue if this parameter does not reference the policy component type of the
                 * association to be renamed.
                 */
                if (!(parameter.getPolicyCmptType().equals(getType().getQualifiedName()))) {
                    return;
                }

                updateTestCaseTypeParameterChildren(parameter);
                updateTestCaseTypeParameterReference(parameter);
            }
        }
    }

    private void updateProductCmptLinks() {
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
            /*
             * Continue if the product component does not reference the product component type of
             * the association to be renamed.
             */
            if (!(productCmpt.getProductCmptType().equals(getType().getQualifiedName()))) {
                continue;
            }

            updateProductCmptLinksFor(productCmpt);
        }
    }

    protected void updateProductCmptLinksFor(IProductCmpt productCmpt) {
        updateProductCmptLinksForContainer(productCmpt);
        for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
            IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(i);
            updateProductCmptLinksForContainer(generation);
        }
    }

    protected void updateProductCmptLinksForContainer(IProductCmptLinkContainer container) {
        for (IProductCmptLink link : container.getLinksAsList(getOriginalName())) {
            link.setAssociation(getNewName());
        }
    }

    private void updateTestCaseTypeParameterChildren(ITestPolicyCmptTypeParameter parameter) {
        for (ITestPolicyCmptTypeParameter child : parameter.getTestPolicyCmptTypeParamChilds()) {
            updateTestCaseTypeParameterChildren(child);
            updateTestCaseTypeParameterReference(child);
        }
    }

    private void updateTestCaseTypeParameterReference(ITestPolicyCmptTypeParameter parameter) {
        if (parameter.getAssociation().equals(getOriginalName())) {
            parameter.setAssociation(getNewName());
            if (parameter.getName().equals(getOriginalName())) {
                parameter.setName(getNewName());
            }
        }
    }

    private void updateDerivedUnionSubsets() {
        for (IAssociation subset : derivedUnionSubsets) {
            subset.setSubsettedDerivedUnion(getNewName());
        }
    }

    private IAssociation getAssociation() {
        return (IAssociation)getIpsElement();
    }

    private IType getType() {
        return getAssociation().getType();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameAssociationProcessor"; //$NON-NLS-1$ ;
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameAssociationProcessor_processorName;
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return true;
    }

    @Override
    public boolean isPluralNameRefactoringRequired() {
        return true;
    }

}
