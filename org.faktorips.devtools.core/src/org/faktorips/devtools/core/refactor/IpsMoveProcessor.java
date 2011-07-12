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

package org.faktorips.devtools.core.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.internal.refactor.Messages;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for all Faktor-IPS "Move" refactoring processors.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsMoveProcessor extends IpsRefactoringProcessor {

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
    }

    /**
     * This implementation validates the target {@link IIpsPackageFragment}. It checks that it does
     * not equal the {@link IIpsObject}'s original {@link IIpsPackageFragment}.
     */
    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (targetIpsPackageFragment.equals(originalIpsPackageFragment)) {
            status.addFatalError(Messages.IpsMoveProcessor_msgTargetLocationEqualsOriginalLocation);
        }
    }

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

    /**
     * Sets the target {@link IIpsPackageFragment}.
     * 
     * @param targetIpsPackageFragment The target {@link IIpsPackageFragment}
     * 
     * @throws NullPointerException If the parameter is null
     */
    public final void setTargetIpsPackageFragment(IIpsPackageFragment targetIpsPackageFragment) {
        ArgumentCheck.notNull(targetIpsPackageFragment);
        this.targetIpsPackageFragment = targetIpsPackageFragment;
    }

    /**
     * Returns the target {@link IIpsPackageFragment}.
     */
    public final IIpsPackageFragment getTargetIpsPackageFragment() {
        return targetIpsPackageFragment;
    }

    /**
     * Returns the element's original {@link IIpsPackageFragment}.
     */
    public final IIpsPackageFragment getOriginalIpsPackageFragment() {
        return originalIpsPackageFragment;
    }

}
