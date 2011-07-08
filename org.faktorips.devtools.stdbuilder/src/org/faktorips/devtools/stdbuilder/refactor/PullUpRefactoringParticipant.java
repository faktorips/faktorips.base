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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.refactoring.structure.PullUpRefactoringProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IpsPullUpArguments;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * This class is loaded by the Faktor-IPS 'Pull Up' refactoring to participate in this process by
 * pulling up the Java source code.
 * 
 * @author Alexander Weickmann
 */
// There's no other way to perform the Java pull up refactoring but by accessing non-published API.
@SuppressWarnings("restriction")
public final class PullUpRefactoringParticipant extends RefactoringParticipant {

    private final PullUpParticipantHelper refactoringHelper;

    private IpsPullUpArguments arguments;

    public PullUpRefactoringParticipant() {
        refactoringHelper = new PullUpParticipantHelper();
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
    public String getName() {
        return "StandardBuilder Pull Up Participant"; //$NON-NLS-1$
    }

    @Override
    protected boolean initialize(Object element) {
        return refactoringHelper.initialize(element);
    }

    @Override
    protected void initialize(RefactoringArguments arguments) {
        this.arguments = (IpsPullUpArguments)arguments;
    }

    private class PullUpParticipantHelper extends RefactoringParticipantHelper {

        @Override
        protected JavaRefactoring createJavaRefactoring(IJavaElement originalJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status,
                IProgressMonitor progressMonitor) throws CoreException {

            if (!(originalJavaElement instanceof IMember && targetJavaElement instanceof IMember)) {
                throw new RuntimeException(
                        "This kind of Java element is not supported by the pull up refactoring participant."); //$NON-NLS-1$
            }

            IMember originalJavaMember = (IMember)originalJavaElement;
            IMember targetJavaMember = (IMember)targetJavaElement;
            if (originalJavaMember.getParent().equals(targetJavaMember.getParent())) {
                return null;
            }

            org.eclipse.jdt.core.IType originalType = (org.eclipse.jdt.core.IType)originalJavaMember.getParent();
            if (originalType.isInterface()) {
                return new PullUpFromInterfaceToInterfaceRefactoring(originalJavaMember,
                        (org.eclipse.jdt.core.IType)targetJavaMember.getParent());
            }
            if (originalType.isEnum()) {
                return null;
            }

            PullUpRefactoringProcessor processor = new PullUpRefactoringProcessor(new IMember[] { originalJavaMember },
                    JavaPreferencesSettings.getCodeGenerationSettings(originalJavaElement.getJavaProject()));
            processor.resetEnvironment();

            processor.setDestinationType(targetJavaMember.getDeclaringType());
            // Pull up all members within a type at once to avoid JDT errors
            IMember[] membersToMove = getOriginalJavaMembersByType().get(originalType).toArray(new IMember[0]);
            processor.setMembersToMove(membersToMove);
            List<IMethod> deletedMethods = determineDeletedMethods(membersToMove);
            processor.setDeletedMethods(deletedMethods.toArray(new IMethod[deletedMethods.size()]));
            Refactoring jdtRefactoring = new ProcessorBasedRefactoring(processor);

            return new JdtRefactoring(jdtRefactoring);
        }

        private List<IMethod> determineDeletedMethods(IMember[] membersToMove) {
            List<IMethod> deletedMethods = new ArrayList<IMethod>(membersToMove.length);
            for (IMember member : membersToMove) {
                if (member instanceof IMethod) {
                    deletedMethods.add((IMethod)member);
                }
            }
            return deletedMethods;
        }

        @Override
        protected boolean initializeTargetJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                StandardBuilderSet builderSet) {

            boolean success = false;
            if (ipsObjectPartContainer instanceof IAttribute) {
                success = initializeTargetJavaElementsForAttribute((IAttribute)ipsObjectPartContainer, builderSet);
            } else if (ipsObjectPartContainer instanceof IEnumAttribute) {
                success = initializeTargetJavaElementsForEnumAttribute((IEnumAttribute)ipsObjectPartContainer,
                        builderSet);
            }
            return success;
        }

        private boolean initializeTargetJavaElementsForAttribute(IAttribute attribute, StandardBuilderSet builderSet) {
            IAttribute targetAttribute = ((IType)arguments.getTarget()).newAttribute();
            targetAttribute.copyFrom(attribute); // Temporary copy
            setTargetJavaElements(builderSet.getGeneratedJavaElements(targetAttribute));
            targetAttribute.delete(); // Delete temporary copy
            return true;
        }

        private boolean initializeTargetJavaElementsForEnumAttribute(IEnumAttribute enumAttribute,
                StandardBuilderSet builderSet) {

            try {
                IEnumAttribute targetEnumAttribute = ((IEnumType)arguments.getTarget()).newEnumAttribute();
                targetEnumAttribute.copyFrom(enumAttribute); // Temporary copy
                setTargetJavaElements(builderSet.getGeneratedJavaElements(targetEnumAttribute));
                targetEnumAttribute.delete(); // Delete temporary copy
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

    }

    private static class PullUpFromInterfaceToInterfaceRefactoring extends JavaRefactoring {

        private final IMember memberToPullUp;

        private final org.eclipse.jdt.core.IType targetInterface;

        private PullUpFromInterfaceToInterfaceRefactoring(IMember memberToPullUp,
                org.eclipse.jdt.core.IType targetInterface) {

            this.memberToPullUp = memberToPullUp;
            this.targetInterface = targetInterface;
        }

        @Override
        public RefactoringStatus checkAllConditions(IProgressMonitor pm) throws CoreException {
            return new RefactoringStatus();
        }

        @Override
        public void perform(IProgressMonitor pm) {
            /*
             * Pull up from interface to interface means we just copy the member to the target
             * interface and delete it from the original interface.
             */
            try {
                memberToPullUp.copy(targetInterface, null, null, true, null);
                memberToPullUp.delete(true, null);
            } catch (JavaModelException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
