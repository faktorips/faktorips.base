/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
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
            Refactoring jdtRefactoring = descriptor.createRefactoring(status);

            return new JdtRefactoring(jdtRefactoring);
        }

        /**
         * Added extra behavior in case an {@link IPolicyCmptTypeAttribute} or
         * {@link IEnumAttribute} shall be renamed.
         */
        @Override
        protected List<IJavaElement> initializeJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                StandardBuilderSet builderSet) {
            List<IJavaElement> javaElements = super.initializeJavaElements(ipsObjectPartContainer, builderSet);
            try {
                if (ipsObjectPartContainer instanceof IAttribute) {
                    javaElements.addAll(initializeJavaElements((IAttribute)ipsObjectPartContainer, builderSet));
                } else if (ipsObjectPartContainer instanceof IEnumAttribute) {
                    javaElements.addAll(initializeJavaElements((IEnumAttribute)ipsObjectPartContainer, builderSet));
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return javaElements;
        }

        /**
         * For {@link IPolicyCmptTypeAttribute}s that are overwritten it's necessary to add the Java
         * elements generated by the original attribute.
         */
        private List<IJavaElement> initializeJavaElements(IAttribute attribute, StandardBuilderSet builderSet)
                throws CoreException {
            ArrayList<IJavaElement> result = new ArrayList<IJavaElement>();

            while (attribute.isOverwrite()) {
                attribute = attribute.findOverwrittenAttribute(attribute.getIpsProject());
                result.addAll(builderSet.getGeneratedJavaElements(attribute));
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
        private List<IJavaElement> initializeJavaElements(IEnumAttribute enumAttribute, StandardBuilderSet builderSet)
                throws CoreException {
            ArrayList<IJavaElement> result = new ArrayList<IJavaElement>();
            List<IEnumAttribute> inheritedCopies = enumAttribute.searchInheritedCopies(enumAttribute.getIpsProject());
            for (IEnumAttribute inheritedCopy : inheritedCopies) {
                result.addAll(builderSet.getGeneratedJavaElements(inheritedCopy));
            }
            return result;
        }

    }

}
