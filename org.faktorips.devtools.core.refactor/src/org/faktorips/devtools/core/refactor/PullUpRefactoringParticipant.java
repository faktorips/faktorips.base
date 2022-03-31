/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

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
import org.faktorips.devtools.abstraction.exception.IpsException;

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

    public PullUpRefactoringParticipant() {
        refactoringHelper = new PullUpParticipantHelper();
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
    public String getName() {
        return "StandardBuilder Pull Up Participant"; //$NON-NLS-1$
    }

    @Override
    protected boolean initialize(Object element) {
        return refactoringHelper.initialize((IpsRefactoringProcessor)getProcessor(), element);
    }

    @Override
    protected void initialize(RefactoringArguments arguments) {
        // nothing to do
    }

    private static class PullUpParticipantHelper extends RefactoringParticipantHelper {

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
            IMember[] membersToMove = getOriginalJavaMembersByType().get(originalType).stream().distinct()
                    .toArray(IMember[]::new);
            processor.setMembersToMove(membersToMove);
            List<IMethod> deletedMethods = determineDeletedMethods(membersToMove);
            processor.setDeletedMethods(deletedMethods.toArray(new IMethod[deletedMethods.size()]));
            Refactoring jdtRefactoring = new ProcessorBasedRefactoring(processor);

            return new JdtRefactoring(jdtRefactoring);
        }

        private List<IMethod> determineDeletedMethods(IMember[] membersToMove) {
            List<IMethod> deletedMethods = new ArrayList<>(membersToMove.length);
            for (IMember member : membersToMove) {
                if (member instanceof IMethod) {
                    deletedMethods.add((IMethod)member);
                }
            }
            return deletedMethods;
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
        public RefactoringStatus checkAllConditions(IProgressMonitor pm) {
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
