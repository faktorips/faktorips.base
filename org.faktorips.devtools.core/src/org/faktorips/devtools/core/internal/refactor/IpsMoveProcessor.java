/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsMoveProcessor;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for all Faktor-IPS "Move" refactorings.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsMoveProcessor extends IpsRefactoringProcessor implements IIpsMoveProcessor {

    /** Target {@link IIpsPackageFragment} to where the {@link IIpsObject} shall be moved. */
    private IIpsPackageFragment targetIpsPackageFragment;

    /** The {@link IIpsObject}'s original {@link IIpsPackageFragment}. */
    private IIpsPackageFragment originalIpsPackageFragment;

    /**
     * @param ipsObject The {@link IIpsObject} to be moved
     */
    protected IpsMoveProcessor(IIpsObject ipsObject) {
        super(ipsObject);
        originalIpsPackageFragment = ipsObject.getIpsPackageFragment();
        targetIpsPackageFragment = originalIpsPackageFragment;
    }

    /**
     * This implementation validates the target {@link IIpsPackageFragment} and returns a
     * {@link RefactoringStatus} as result of the validation. It checks that it does not equal the
     * {@link IIpsObject}'s original {@link IIpsPackageFragment}.
     */
    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        if (targetIpsPackageFragment.equals(originalIpsPackageFragment)) {
            status.addFatalError(Messages.IpsMoveProcessor_msgTargetLocationEqualsOriginalLocation);
        } else {
            validateUserInputThis(status, pm);
        }
        return status;
    }

    /**
     * Subclasses must implement this operation and provide special user input validations.
     * <p>
     * This operation is called by {@link #validateUserInput(IProgressMonitor)}.
     * 
     * @param status {@link RefactoringStatus} to report messages to
     * @param pm {@link IProgressMonitor} to report progress to
     * 
     * @throws CoreException May be thrown at any time
     */
    protected abstract void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException;

    @Override
    public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
            throws CoreException {

        RefactoringProcessor processor = this;
        Object elementToMove = getIpsElement();
        MoveArguments arguments = new MoveArguments(targetIpsPackageFragment, true);
        String[] affectedNatures = new String[] { IIpsProject.NATURE_ID };
        return ParticipantManager.loadMoveParticipants(status, processor, elementToMove, arguments, affectedNatures,
                sharedParticipants);
    }

    @Override
    public final void setTargetIpsPackageFragment(IIpsPackageFragment targetIpsPackageFragment) {
        ArgumentCheck.notNull(targetIpsPackageFragment);
        this.targetIpsPackageFragment = targetIpsPackageFragment;
    }

    @Override
    public final IIpsPackageFragment getTargetIpsPackageFragment() {
        return targetIpsPackageFragment;
    }

    @Override
    public final IIpsPackageFragment getOriginalIpsPackageFragment() {
        return originalIpsPackageFragment;
    }

}
