/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.refactor.IpsRenameArguments;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * This class is loaded by the Faktor-IPS 'Rename' refactoring to participate in this process by
 * renaming the Java source code.
 * 
 * @author Alexander Weickmann
 */
public final class RenameRefactoringParticipant extends RenameParticipant {

    private final RenameParticipantHelper refactoringHelper;

    public RenameRefactoringParticipant() {
        refactoringHelper = new RenameParticipantHelper();
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {

        return refactoringHelper.checkConditions(pm);
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return refactoringHelper.createChange(pm);
    }

    @Override
    protected boolean initialize(Object element) {
        return refactoringHelper.initialize(element);
    }

    @Override
    public String getName() {
        return "StandardBuilder Rename Participant"; //$NON-NLS-1$
    }

    @Override
    public IpsRenameArguments getArguments() {
        return (IpsRenameArguments)super.getArguments();
    }

    private class RenameParticipantHelper extends RefactoringParticipantHelper {

        @Override
        protected Refactoring createJdtRefactoring(IJavaElement originalJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status,
                IProgressMonitor progressMonitor) throws CoreException {

            String oldName = originalJavaElement.getElementName();
            String newName = targetJavaElement.getElementName();

            if (newName.equals(oldName) || newName.isEmpty()) {
                return null;
            }

            String javaRefactoringContributionId;
            switch (originalJavaElement.getElementType()) {
                case IJavaElement.FIELD:
                    javaRefactoringContributionId = IJavaRefactorings.RENAME_FIELD;
                    break;
                case IJavaElement.METHOD:
                    javaRefactoringContributionId = IJavaRefactorings.RENAME_METHOD;
                    break;
                case IJavaElement.TYPE:
                    javaRefactoringContributionId = IJavaRefactorings.RENAME_TYPE;
                    break;
                default:
                    throw new RuntimeException(
                            "This kind of Java element is not supported by the rename refactoring participant."); //$NON-NLS-1$
            }

            RefactoringContribution contribution = RefactoringCore
                    .getRefactoringContribution(javaRefactoringContributionId);
            RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor)contribution.createDescriptor();
            descriptor.setJavaElement(originalJavaElement);
            descriptor.setNewName(newName);
            descriptor.setUpdateReferences(getArguments().getUpdateReferences());
            return descriptor.createRefactoring(status);
        }

        /**
         * Added extra behavior in case an {@link IPolicyCmptTypeAttribute} or
         * {@link IEnumAttribute} shall be renamed.
         */
        @Override
        protected boolean initializeOriginalJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                StandardBuilderSet builderSet) {

            boolean success = super.initializeOriginalJavaElements(ipsObjectPartContainer, builderSet);
            try {
                if (ipsObjectPartContainer instanceof IPolicyCmptTypeAttribute) {
                    success = initializeOriginalJavaElements((IPolicyCmptTypeAttribute)ipsObjectPartContainer,
                            builderSet);
                } else if (ipsObjectPartContainer instanceof IEnumAttribute) {
                    success = initializeOriginalJavaElements((IEnumAttribute)ipsObjectPartContainer, builderSet);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return success;
        }

        /**
         * For {@link IPolicyCmptTypeAttribute}s that are overwritten it's necessary to add the Java
         * elements generated by the original attribute.
         */
        private boolean initializeOriginalJavaElements(IPolicyCmptTypeAttribute policyCmptTypeAttribute,
                StandardBuilderSet builderSet) throws CoreException {

            if (policyCmptTypeAttribute.isOverwrite()) {
                IPolicyCmptTypeAttribute overwrittenAttribute = policyCmptTypeAttribute
                        .findOverwrittenAttribute(policyCmptTypeAttribute.getIpsProject());
                getOriginalJavaElements().addAll(builderSet.getGeneratedJavaElements(overwrittenAttribute));
            }
            return true;
        }

        /**
         * For {@link IEnumAttribute}s it's necessary to add Java elements from subclasses that
         * semantically reference the {@link IEnumAttribute} to be renamed. This is so, because on
         * the model side the inherited copies in subclassing {@link IEnumType}s will be renamed. In
         * this case the model and the code would be out of sync.
         * <p>
         * A re-generation of the source code will not be sufficient because of protected code
         * regions. These regions would not be reached when renaming one of the inherited copies
         * later on.
         */
        private boolean initializeOriginalJavaElements(IEnumAttribute enumAttribute, StandardBuilderSet builderSet)
                throws CoreException {

            List<IEnumAttribute> inheritedCopies = enumAttribute.searchInheritedCopies(enumAttribute.getIpsProject());
            for (IEnumAttribute inheritedCopy : inheritedCopies) {
                getOriginalJavaElements().addAll(builderSet.getGeneratedJavaElements(inheritedCopy));
            }
            return true;
        }

        @Override
        protected boolean initializeTargetJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                StandardBuilderSet builderSet) {

            if (ipsObjectPartContainer instanceof IAttribute) {
                return initializeTargetJavaElements((IAttribute)ipsObjectPartContainer, builderSet);

            } else if (ipsObjectPartContainer instanceof IAssociation) {
                return initializeTargetJavaElements((IAssociation)ipsObjectPartContainer, builderSet);

            } else if (ipsObjectPartContainer instanceof IIpsObject) {
                IIpsObject ipsObject = (IIpsObject)ipsObjectPartContainer;
                IIpsPackageFragment targetIpsPackageFragment = ipsObject.getIpsPackageFragment();
                String newName = getArguments().getNewName();
                return initTargetJavaElements(ipsObject, targetIpsPackageFragment, newName, builderSet);

            } else if (ipsObjectPartContainer instanceof IEnumAttribute) {
                try {
                    return initializeTargetJavaElements((IEnumAttribute)ipsObjectPartContainer, builderSet);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }

            } else if (ipsObjectPartContainer instanceof IEnumLiteralNameAttributeValue) {
                return initializeTargetJavaElements((IEnumLiteralNameAttributeValue)ipsObjectPartContainer, builderSet);
            }

            throw new RuntimeException("This kind of IPS element is not supported by the rename participant.");
        }

        private boolean initializeTargetJavaElements(IAttribute attribute, StandardBuilderSet builderSet) {
            String oldName = attribute.getName();

            IAttribute overwrittenAttribute = null;
            if (attribute instanceof IPolicyCmptTypeAttribute) {
                IPolicyCmptTypeAttribute policyCmptTypeAttribute = (IPolicyCmptTypeAttribute)attribute;
                if (policyCmptTypeAttribute.isOverwrite()) {
                    try {
                        overwrittenAttribute = policyCmptTypeAttribute.findOverwrittenAttribute(policyCmptTypeAttribute
                                .getIpsProject());
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            attribute.setName(getArguments().getNewName());
            if (overwrittenAttribute != null) {
                overwrittenAttribute.setName(getArguments().getNewName());
            }

            List<IJavaElement> targetJavaElements = builderSet.getGeneratedJavaElements(attribute);
            if (overwrittenAttribute != null) {
                targetJavaElements.addAll(builderSet.getGeneratedJavaElements(overwrittenAttribute));
            }
            setTargetJavaElements(targetJavaElements);

            attribute.setName(oldName);
            if (overwrittenAttribute != null) {
                overwrittenAttribute.setName(oldName);
            }

            return true;
        }

        private boolean initializeTargetJavaElements(IAssociation association, StandardBuilderSet builderSet) {
            String oldTargetRoleSingular = association.getTargetRoleSingular();
            String oldTargetRolePlural = association.getTargetRolePlural();

            association.setTargetRoleSingular(getArguments().getNewName());
            association.setTargetRolePlural(getArguments().getNewPluralName());
            setTargetJavaElements(builderSet.getGeneratedJavaElements(association));

            association.setTargetRoleSingular(oldTargetRoleSingular);
            association.setTargetRolePlural(oldTargetRolePlural);

            return true;
        }

        private boolean initializeTargetJavaElements(IEnumAttribute enumAttribute, StandardBuilderSet builderSet)
                throws CoreException {

            String oldName = enumAttribute.getName();
            String newName = getArguments().getNewName();
            List<IEnumAttribute> inheritedCopies = enumAttribute.searchInheritedCopies(enumAttribute.getIpsProject());

            // 1) Set all enumeration attribute names to the new name
            enumAttribute.setName(newName);
            for (IEnumAttribute inheritedCopy : inheritedCopies) {
                inheritedCopy.setName(newName);
            }

            // 2) Build together the target Java elements
            List<IJavaElement> targetJavaElements = new ArrayList<IJavaElement>(getOriginalJavaElements().size());
            targetJavaElements.addAll(builderSet.getGeneratedJavaElements(enumAttribute));
            for (IEnumAttribute inheritedCopy : inheritedCopies) {
                targetJavaElements.addAll(builderSet.getGeneratedJavaElements(inheritedCopy));
            }
            setTargetJavaElements(targetJavaElements);

            // 3) Reset every enumeration attribute to it's old name
            enumAttribute.setName(oldName);
            for (IEnumAttribute inheritedCopy : inheritedCopies) {
                inheritedCopy.setName(oldName);
            }

            return true;
        }

        private boolean initializeTargetJavaElements(IEnumLiteralNameAttributeValue literalNameAttributeValue,
                StandardBuilderSet builderSet) {

            String oldName = literalNameAttributeValue.getValue();
            literalNameAttributeValue.setValue(getArguments().getNewName());
            setTargetJavaElements(builderSet.getGeneratedJavaElements(literalNameAttributeValue));
            literalNameAttributeValue.setValue(oldName);
            return true;
        }

        @Override
        protected IJavaElement getTargetJavaElementForOriginalJavaElement(IJavaElement originalJavaElement) {
            /*
             * When renaming, the original Java elements map index-to-index to the target Java
             * elements. This might not be the most elegant solution but it will suffice for now.
             */
            int originalIndex = getOriginalJavaElements().indexOf(originalJavaElement);
            return getTargetJavaElements().get(originalIndex);
        }

    }

}
