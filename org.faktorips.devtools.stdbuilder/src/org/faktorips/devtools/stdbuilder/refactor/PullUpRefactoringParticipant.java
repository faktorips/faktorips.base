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
import java.util.Arrays;
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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.refactor.IpsPullUpArguments;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.w3c.dom.Element;

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

    // Not needed for now, reserved for later use
    @SuppressWarnings("unused")
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
        protected Refactoring createJdtRefactoring(IJavaElement originalJavaElement,
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

            PullUpRefactoringProcessor processor = new PullUpRefactoringProcessor(new IMember[] { originalJavaMember },
                    JavaPreferencesSettings.getCodeGenerationSettings(originalJavaElement.getJavaProject()));
            processor.resetEnvironment();
            processor.setDestinationType(targetJavaMember.getDeclaringType());
            processor.setMembersToMove(new IMember[] { originalJavaMember });

            IMember[] membersToMove = determineAdditionalRequiredMembers(progressMonitor, originalJavaMember, processor);
            processor.setMembersToMove(membersToMove);
            List<IMethod> deletedMethods = determineDeletedMethods(membersToMove);
            processor.setDeletedMethods(deletedMethods.toArray(new IMethod[deletedMethods.size()]));

            return new ProcessorBasedRefactoring(processor);
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

        private IMember[] determineAdditionalRequiredMembers(IProgressMonitor progressMonitor,
                IMember originalJavaMember,
                PullUpRefactoringProcessor processor) throws JavaModelException {

            IMember[] additionalRequiredMembers = processor.getAdditionalRequiredMembersToPullUp(progressMonitor);
            IMember[] membersToMove = Arrays.copyOf(additionalRequiredMembers, additionalRequiredMembers.length + 1);
            membersToMove[additionalRequiredMembers.length] = originalJavaMember;
            return membersToMove;
        }

        @Override
        protected boolean initializeTargetJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                StandardBuilderSet builderSet) {

            boolean success = false;
            if (ipsObjectPartContainer instanceof IAttribute) {
                success = initializeTargetJavaElementsForAttribute((IAttribute)ipsObjectPartContainer, builderSet);
            }
            return success;
        }

        private boolean initializeTargetJavaElementsForAttribute(IAttribute attribute, StandardBuilderSet builderSet) {
            try {
                IType originalType = attribute.getType();
                IType targetType = originalType.findSupertype(originalType.getIpsProject());
                if (targetType == null) {
                    throw new RuntimeException("There is no supertype to pull up to."); //$NON-NLS-1$
                }
                IAttribute targetAttribute = targetType.newAttribute();
                copyAttributeToSupertype(attribute, targetAttribute); // Temporary copy
                setTargetJavaElements(builderSet.getGeneratedJavaElements(targetAttribute));
                targetAttribute.delete();
                return true;
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        private void copyAttributeToSupertype(IAttribute attribute, IAttribute targetAttribute) {
            Element oldAttributeXml = attribute.toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument());
            targetAttribute.initFromXml(oldAttributeXml);
        }

        /**
         * Forbids the pull up refactoring for members that originate from an interface, as the JDT
         * does not support pulling up members from interfaces to other interfaces.
         */
        @Override
        protected boolean prepareRefactoring(IJavaElement originalJavaElement, IJavaElement targetJavaElement)
                throws CoreException {

            IMember originalJavaMember = (IMember)originalJavaElement;
            org.eclipse.jdt.core.IType originalType = (org.eclipse.jdt.core.IType)originalJavaMember.getParent();
            return !originalType.isInterface();
        }

        /**
         * Handles the members that originate from interfaces and therefore were excluded from the
         * refactoring by {@link #prepareRefactoring(IJavaElement, IJavaElement)}.
         * <p>
         * The members will be copied to the target type and deleted from the original type.
         */
        @Override
        protected void finalizeRefactoring(IJavaElement originalJavaElement, IJavaElement targetJavaElement)
                throws CoreException {

            IMember originalJavaMember = (IMember)originalJavaElement;
            org.eclipse.jdt.core.IType originalType = (org.eclipse.jdt.core.IType)originalJavaMember.getParent();
            if (!originalType.isInterface()) {
                return;
            }

            IMember targetJavaMember = (IMember)targetJavaElement;
            org.eclipse.jdt.core.IType targetType = (org.eclipse.jdt.core.IType)targetJavaMember.getParent();
            originalJavaMember.copy(targetType, null, null, true, null);
            originalJavaMember.delete(true, null);
        }

    }

}
