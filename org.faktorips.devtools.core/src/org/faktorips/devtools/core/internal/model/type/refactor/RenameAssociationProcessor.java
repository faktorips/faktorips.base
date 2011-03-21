/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.devtools.core.internal.refactor.IpsRenameProcessor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.MessageList;

/**
 * Faktor-IPS "Rename Association" refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameAssociationProcessor extends IpsRenameProcessor {

    private final Set<IAssociation> derivedUnionSubsets;

    private Set<IIpsSrcFile> productCmptSrcFiles;

    private Set<IIpsSrcFile> testCaseTypeSrcFiles;

    public RenameAssociationProcessor(IAssociation association) {
        super(association, association.getName(), association.getTargetRolePlural());

        getIgnoredValidationMessageCodes().add(IPolicyCmptTypeAssociation.MSGCODE_INVERSE_RELATION_MISMATCH);
        getIgnoredValidationMessageCodes().add(IAssociation.MSGCODE_DERIVED_UNION_NOT_FOUND);

        derivedUnionSubsets = new HashSet<IAssociation>();
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        addIpsSrcFile(getAssociation().getIpsSrcFile());

        if (getAssociation() instanceof IPolicyCmptTypeAssociation) {
            IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)getAssociation();
            IPolicyCmptTypeAssociation inverseAssociation = policyCmptTypeAssociation
                    .findInverseAssociation(policyCmptTypeAssociation.getIpsProject());
            if (inverseAssociation != null) {
                addIpsSrcFile(inverseAssociation.getIpsSrcFile());
            }

            testCaseTypeSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.TEST_CASE_TYPE);
            for (IIpsSrcFile ipsSrcFile : testCaseTypeSrcFiles) {
                addIpsSrcFile(ipsSrcFile);
            }

        } else if (getAssociation() instanceof IProductCmptTypeAssociation) {
            productCmptSrcFiles = findReferencingIpsSrcFiles(IpsObjectType.PRODUCT_CMPT);
            for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
                addIpsSrcFile(ipsSrcFile);
            }
        }

        if (getAssociation().isDerivedUnion()) {
            Set<IIpsSrcFile> typeSrcFiles = findReferencingIpsSrcFiles(getAssociation() instanceof IPolicyCmptTypeAssociation ? IpsObjectType.POLICY_CMPT_TYPE
                    : IpsObjectType.PRODUCT_CMPT_TYPE);
            for (IIpsSrcFile ipsSrcFile : typeSrcFiles) {
                IType type = (IType)ipsSrcFile.getIpsObject();
                for (IAssociation association : type.getAssociations()) {
                    if (association.getSubsettedDerivedUnion().equals(getOriginalName())) {
                        derivedUnionSubsets.add(association);
                        addIpsSrcFile(ipsSrcFile);
                    }
                }
            }
        }

    }

    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();

        if (getNewName().isEmpty()) {
            status.addFatalError(Messages.RenameAssociationProcessor_msgNewNameMustNotBeEmpty);
            return status;
        }

        if (getNewName().equals(getOriginalName()) && getNewPluralName().equals(getOriginalPluralName())) {
            status.addFatalError(Messages.RenameAssociationProcessor_msgEitherNameOrPluralNameMustBeChanged);
            return status;
        }

        if (getAssociation().is1ToMany() && getNewPluralName().isEmpty()) {
            status.addFatalError(Messages.RenameAssociationProcessor_msgNewPluralNameMustNotBeEmptyForToManyAssociations);
            return status;
        }

        validateUserInputThis(status, pm);
        return status;
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        getAssociation().setTargetRoleSingular(getNewName());
        getAssociation().setTargetRolePlural(getNewPluralName());

        MessageList validationMessageList = getAssociation().validate(getIpsProject());
        validationMessageList.add(getType().validate(getIpsProject()));
        addValidationMessagesToStatus(validationMessageList, status);

        getAssociation().setTargetRoleSingular(getOriginalName());
        getAssociation().setTargetRolePlural(getOriginalPluralName());
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        updateTargetRoleSingular();
        updateTargetRolePlural();

        if (getAssociation() instanceof IPolicyCmptTypeAssociation) {
            updateInverseAssociation();
            updateTestCaseTypeParameters();
        } else {
            updateProductCmptLinks();
        }
        if (getAssociation().isDerivedUnion()) {
            updateDerivedUnionSubsets();
        }
    }

    private void updateTargetRoleSingular() {
        getAssociation().setTargetRoleSingular(getNewName());
    }

    private void updateTargetRolePlural() {
        getAssociation().setTargetRolePlural(getNewPluralName());
    }

    private void updateInverseAssociation() throws CoreException {
        IPolicyCmptTypeAssociation policyCmptTypeAssociation = (IPolicyCmptTypeAssociation)getAssociation();
        IPolicyCmptTypeAssociation inverseAssociation = policyCmptTypeAssociation
                .findInverseAssociation(policyCmptTypeAssociation.getIpsProject());
        if (inverseAssociation != null) {
            inverseAssociation.setInverseAssociation(getNewName());
        }
    }

    private void updateTestCaseTypeParameters() throws CoreException {
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

    private void updateProductCmptLinks() throws CoreException {
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
            /*
             * Continue if the product component does not reference the product component type of
             * the association to be renamed.
             */
            if (!(productCmpt.getProductCmptType().equals(getType().getQualifiedName()))) {
                continue;
            }

            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(i);
                for (IProductCmptLink link : generation.getLinks(getOriginalName())) {
                    link.setAssociation(getNewName());
                }
            }
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
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameAssociationProcessor"; //$NON-NLS-1$;
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameAssociationProcessor_processorName;
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return false;
    }

    @Override
    public boolean isPluralNameRefactoringRequired() {
        return true;
    }

}
