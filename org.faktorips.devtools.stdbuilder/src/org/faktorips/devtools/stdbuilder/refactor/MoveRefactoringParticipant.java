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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * This class is loaded by the Faktor-IPS 'Move' refactoring to participate in this process by
 * moving the Java source code.
 * 
 * @author Alexander Weickmann
 */
public final class MoveRefactoringParticipant extends MoveParticipant {

    private final MoveParticipantHelper refactoringHelper;

    public MoveRefactoringParticipant() {
        refactoringHelper = new MoveParticipantHelper();
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
        return "StandardBuilder Move Participant"; //$NON-NLS-1$
    }

    private final class MoveParticipantHelper extends RefactoringParticipantHelper {

        @Override
        protected JavaRefactoring createJavaRefactoring(IJavaElement originalJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status,
                IProgressMonitor progressMonitor) throws CoreException {

            if (!(originalJavaElement instanceof IType && targetJavaElement instanceof IType)) {
                return null;
            }

            IType originalJavaType = (IType)originalJavaElement;
            IType targetJavaType = (IType)targetJavaElement;

            RefactoringContribution moveContribution = RefactoringCore
                    .getRefactoringContribution(IJavaRefactorings.MOVE);
            MoveDescriptor descriptor = (MoveDescriptor)moveContribution.createDescriptor();
            descriptor.setMoveResources(new IFile[0], new IFolder[0],
                    new ICompilationUnit[] { originalJavaType.getCompilationUnit() });
            descriptor.setDestination(targetJavaType.getPackageFragment());
            descriptor.setProject(targetJavaType.getJavaProject().getElementName());
            descriptor.setUpdateReferences(getArguments().getUpdateReferences());
            Refactoring jdtRefactoring = descriptor.createRefactoring(status);

            return new JdtRefactoring(jdtRefactoring);
        }

        @Override
        protected boolean initializeTargetJavaElements(IIpsObjectPartContainer ipsObjectPartContainer,
                StandardBuilderSet builderSet) {

            if (!(ipsObjectPartContainer instanceof IIpsObject)) {
                return false;
            }

            IIpsObject ipsObject = (IIpsObject)ipsObjectPartContainer;
            IIpsPackageFragment targetIpsPackageFragment = (IIpsPackageFragment)getArguments().getDestination();
            String newName = ipsObject.getName();
            boolean success = initTargetJavaElements(ipsObject, targetIpsPackageFragment, newName, builderSet);
            return success;
        }

    }

}
