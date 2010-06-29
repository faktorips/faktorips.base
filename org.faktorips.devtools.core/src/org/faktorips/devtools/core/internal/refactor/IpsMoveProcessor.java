/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
 * This is the abstract base class for all Faktor-IPS "Move" refactorings.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsMoveProcessor extends IpsRefactoringProcessor implements IIpsMoveProcessor {

    /** The target <tt>IIpsPackageFragment</tt> to where the <tt>IIpsObject</tt> needs to be moved. */
    private IIpsPackageFragment targetIpsPackageFragment;

    /** The <tt>IIpsObject</tt>'s original <tt>IIpsPackageFragment</tt>. */
    private IIpsPackageFragment originalIpsPackageFragment;

    /**
     * @param ipsObject The <tt>IIpsObject</tt> to be moved.
     */
    protected IpsMoveProcessor(IIpsObject ipsObject) {
        super(ipsObject);
        originalIpsPackageFragment = ipsObject.getIpsPackageFragment();
        targetIpsPackageFragment = originalIpsPackageFragment;
    }

    /**
     * This implementation validates the target <tt>IIpsPackageFragment</tt> and returns a
     * <tt>RefactoringStatus</tt> as result of the validation. It checks that it does not equal the
     * <tt>IIpsObject</tt>'s original <tt>IIpsPackageFragment</tt>.
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
     * This operation is called by <tt>validateUserInput</tt>. Subclasses must implement special
     * user input validations here.
     * 
     * @param status The <tt>RefactoringStatus</tt> to report messages to.
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * 
     * @throws CoreException May be thrown at any time.
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
