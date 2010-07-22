/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;
import org.faktorips.util.ArgumentCheck;

/**
 * This is the abstract base class for all Faktor-IPS "Rename" refactorings.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRenameProcessor extends IpsRefactoringProcessor implements IIpsRenameProcessor {

    /** The new name for the <tt>IIpsElement</tt> to refactor. */
    private String newName;

    /** The <tt>IIpsElement</tt>'s original name. */
    private final String originalName;

    /**
     * Creates an <tt>IpsRenameProcessor</tt>.
     * 
     * @param ipsElement The <tt>IIpsElement</tt> to be refactored.
     * @param originalName The original name of the <tt>IIpsElement</tt> to be refactored.
     */
    protected IpsRenameProcessor(IIpsElement ipsElement, String originalName) {
        super(ipsElement);
        this.originalName = originalName;
    }

    /**
     * This implementation validates the element's new name and returns a <tt>RefactoringStatus</tt>
     * as result of the validation. It checks that the name is not empty and that the name does not
     * equal the element's original name.
     */
    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        if (newName.length() < 1) {
            status.addFatalError(Messages.IpsRenameProcessor_msgNewNameEmpty);
        } else if (newName.equals(getIpsElement().getName())) {
            status.addFatalError(Messages.IpsRenameProcessor_msgNewNameEqualsElementName);
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
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) throws CoreException {

        return ParticipantManager.loadRenameParticipants(status, this, getIpsElement(), new RenameArguments(newName,
                true), new String[] { IIpsProject.NATURE_ID }, sharedParticipants);
    }

    @Override
    public final void setNewName(String newName) {
        ArgumentCheck.notNull(newName);
        this.newName = newName;
    }

    @Override
    public final String getOriginalName() {
        return originalName;
    }

    @Override
    public final String getNewName() {
        return newName;
    }

}
