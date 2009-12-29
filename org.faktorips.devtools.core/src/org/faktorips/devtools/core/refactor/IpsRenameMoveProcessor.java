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

package org.faktorips.devtools.core.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * This is the abstract base class for all Faktor-IPS "Rename" and "Move" refactorings.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRenameMoveProcessor extends IpsRefactoringProcessor {

    /** The original location of the <tt>IIpsElement</tt> to be refactored. */
    private LocationDescriptor originalLocation;

    /** The target location of the <tt>IIpsElement</tt> to be refactored. */
    private LocationDescriptor targetLocation;

    /** Flag indicating whether a move is performed. */
    private final boolean move;

    /**
     * Creates a <tt>IpsRenameMoveProcessor</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be refactored.
     * @param move Flag indicating whether a move is performed.
     */
    protected IpsRenameMoveProcessor(IIpsElement ipsElement, boolean move) {
        super(ipsElement);
        this.move = move;
        originalLocation = initOriginalLocation();
    }

    /**
     * Subclass implementation that must initialize the original location of the
     * <tt>IIpsElement</tt> to be refactored.
     * <p>
     * <strong>Important:</strong> This operation is called by the constructor of
     * <tt>IpsRenameMoveProcessor</tt> and therefore may not assume that the constructor of the
     * subclass has been called. This operation must not return <tt>null</tt>.
     */
    protected abstract LocationDescriptor initOriginalLocation();

    /**
     * {@inheritDoc}
     * <p>
     * This implementation validates the target location and calls the subclass implementation.
     */
    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException, OperationCanceledException {

        RefactoringStatus status = validateTargetLocation(pm);
        checkFinalConditionsThis(status, pm, context);
        return status;
    }

    /**
     * Subclass implementation for final condition checking that is performed in addition to the
     * default final condition checking which validates the target location.
     * 
     * @param status The <tt>RefactoringStatus</tt> to add messages to.
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * @param context A condition checking context to collect shared condition checks.
     * 
     * @throws CoreException May be thrown at any time.
     */
    protected abstract void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException;

    /**
     * Validates the target location and returns a <tt>RefactoringStatus</tt> as result of the
     * validation. This base implementation checks that the name is not empty and that the target
     * location does not equal the original location.
     * 
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * 
     * @throws CoreException If an error occurs while validating the target location.
     */
    public final RefactoringStatus validateTargetLocation(IProgressMonitor pm) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();

        if (targetLocation.getName().length() < 1) {
            status.addFatalError(Messages.IpsRenameMoveProcessor_msgNewNameEmpty);

        } else if (targetLocation.equals(originalLocation)) {
            if (move) {
                status.addFatalError(Messages.IpsRenameMoveProcessor_msgTargetLocationEqualsOriginalLocation);
            } else {
                status.addFatalError(Messages.IpsRenameMoveProcessor_msgNewNameEqualsElementName);
            }

        } else {
            validateTargetLocationThis(status, pm);
        }

        return status;
    }

    /**
     * This operation is called by <tt>validateTargetLocation</tt>. Subclasses must implement
     * special target location validations here.
     * 
     * @param status The <tt>RefactoringStatus</tt> to report messages to.
     * @param pm An <tt>IProgressMonitor</tt> to report progress to.
     * 
     * @throws CoreException May be thrown at any time.
     */
    protected abstract void validateTargetLocationThis(RefactoringStatus status, IProgressMonitor pm)
            throws CoreException;

    @Override
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) throws CoreException {

        if (move) {
            return ParticipantManager.loadMoveParticipants(status, this, getIpsElement(), new MoveArguments(
                    targetLocation, true), new String[] { IIpsProject.NATURE_ID }, sharedParticipants);
        } else {
            return ParticipantManager.loadRenameParticipants(status, this, getIpsElement(), new RenameArguments(
                    targetLocation.getName(), true), new String[] { IIpsProject.NATURE_ID }, sharedParticipants);
        }
    }

    /**
     * Sets the target location of the <tt>IIpsElement</tt> to be refactored.
     * 
     * @param originalLocation A <tt>LocationDescriptor</tt> representing the target location of the
     *            <tt>IIpsElement</tt> to refactor.
     * 
     * @throws NullPointerException If <tt>targetLocation</tt> is <tt>null</tt>.
     */
    public final void setTargetLocation(LocationDescriptor targetLocation) {
        ArgumentCheck.notNull(targetLocation);
        if (move) {
            this.targetLocation = new LocationDescriptor(targetLocation.getIpsPackageFragment(), originalLocation
                    .getName());
        } else {
            this.targetLocation = new LocationDescriptor(originalLocation.getIpsPackageFragment(), targetLocation
                    .getName());
        }
    }

    /**
     * Returns a <tt>LocationDescriptor</tt> representing the original location of the
     * <tt>IIpsElement</tt> to be refactored.
     */
    public final LocationDescriptor getOriginalLocation() {
        return originalLocation;
    }

    /**
     * Returns a <tt>LocationDescriptor</tt> representing the target location of the
     * <tt>IIpsElement</tt> to be refactored.
     */
    public final LocationDescriptor getTargetLocation() {
        return targetLocation;
    }

}
