/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor.java;

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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameArguments;
import org.faktorips.devtools.model.builder.IJavaBuilderSet;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.type.IAttribute;

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
    public Change createChange(IProgressMonitor pm) throws IpsException, OperationCanceledException {
        return refactoringHelper.createChange(pm);
    }

    @Override
    protected boolean initialize(Object element) {
        return refactoringHelper.initialize((IpsRefactoringProcessor)getProcessor(), element);
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
        protected JavaRefactoring createJavaRefactoring(IJavaElement originalJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status,
                IProgressMonitor progressMonitor) throws CoreException {

            String oldName = originalJavaElement.getElementName();
            String newName = targetJavaElement.getElementName();

            if (newName.equals(oldName) || newName.isEmpty()) {
                return null;
            }

            String javaRefactoringContributionId = switch (originalJavaElement.getElementType()) {
                case IJavaElement.FIELD -> IJavaRefactorings.RENAME_FIELD;
                case IJavaElement.METHOD -> IJavaRefactorings.RENAME_METHOD;
                case IJavaElement.TYPE -> IJavaRefactorings.RENAME_TYPE;
                default -> throw new RuntimeException(
                        "This kind of Java element is not supported by the rename refactoring participant."); //$NON-NLS-1$
            };
            RefactoringContribution contribution = RefactoringCore
                    .getRefactoringContribution(javaRefactoringContributionId);
            RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor)contribution.createDescriptor();
            descriptor.setJavaElement(originalJavaElement);
            descriptor.setNewName(newName);
            descriptor.setUpdateReferences(getArguments().getUpdateReferences());
            Refactoring jdtRefactoring = descriptor.createRefactoring(status);

            return new JdtRefactoring(jdtRefactoring);
        }

        /**
         * Added extra behavior in case an {@link IPolicyCmptTypeAttribute} or
         * {@link IEnumAttribute} shall be renamed.
         */
        @Override
        public List<IJavaElement> initializeJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                IJavaBuilderSet builderSet) {
            List<IJavaElement> javaElements = super.initializeJavaElements(ipsObjectPartContainer, builderSet);
            if (ipsObjectPartContainer instanceof IAttribute a) {
                javaElements.addAll(initializeJavaElements(a, builderSet));
            } else if (ipsObjectPartContainer instanceof IEnumAttribute ea) {
                javaElements.addAll(initializeJavaElements(ea, builderSet));
            }
            return javaElements;
        }

        /**
         * For {@link IPolicyCmptTypeAttribute}s that are overwritten it's necessary to add the Java
         * elements generated by the original attribute.
         */
        private List<IJavaElement> initializeJavaElements(IAttribute attribute, IJavaBuilderSet builderSet) {
            ArrayList<IJavaElement> result = new ArrayList<>();
            IAttribute currentAttribute = attribute;
            while (currentAttribute.isOverwrite()) {
                currentAttribute = currentAttribute.findOverwrittenAttribute(attribute.getIpsProject());
                result.addAll(builderSet.getGeneratedJavaElements(currentAttribute));
            }
            if (attribute instanceof IPolicyCmptTypeAttribute) {
                IValidationRule valueSetRule = ((IPolicyCmptTypeAttribute)attribute)
                        .findValueSetRule(attribute.getIpsProject());
                if (valueSetRule != null) {
                    result.addAll(builderSet.getGeneratedJavaElements(valueSetRule));
                }
            }
            return result;
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
        private List<IJavaElement> initializeJavaElements(IEnumAttribute enumAttribute, IJavaBuilderSet builderSet) {
            ArrayList<IJavaElement> result = new ArrayList<>();
            List<IEnumAttribute> inheritedCopies = enumAttribute.searchInheritedCopies(enumAttribute.getIpsProject());
            for (IEnumAttribute inheritedCopy : inheritedCopies) {
                result.addAll(builderSet.getGeneratedJavaElements(inheritedCopy));
            }
            return result;
        }

    }

}
