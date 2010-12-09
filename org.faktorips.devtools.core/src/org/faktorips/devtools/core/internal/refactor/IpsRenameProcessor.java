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
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameArguments;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for all Faktor-IPS "Rename" refactorings.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRenameProcessor extends IpsRefactoringProcessor implements IIpsRenameProcessor {

    /** Original name of the {@link IIpsElement} to be refactored. */
    private final String originalName;

    /** Original plural name of the {@link IIpsElement} to be refactored. */
    private final String originalPluralName;

    /** New name for the {@link IIpsElement} to be refactored. */
    private String newName;

    /** New plural name for the {@link IIpsElement} to be refactored. */
    private String newPluralName;

    private boolean pluralNameRefactoringRequired;

    /**
     * @param ipsElement {@link IIpsElement} to be refactored
     * @param originalName Original name of the {@link IIpsElement} to be refactored
     * @param originalPluralName Original plural name of the {@link IIpsElement} to be refactored
     */
    protected IpsRenameProcessor(IIpsElement ipsElement, String originalName, String originalPluralName) {
        super(ipsElement);
        this.originalName = originalName;
        this.originalPluralName = originalPluralName;
    }

    /**
     * @param ipsElement {@link IIpsElement} to be refactored
     * @param originalName Original name of the {@link IIpsElement} to be refactored
     */
    protected IpsRenameProcessor(IIpsElement ipsElement, String originalName) {
        this(ipsElement, originalName, null);
    }

    /**
     * This implementation validates the element's new name and returns a {@link RefactoringStatus}
     * as result of the validation. It checks that the name is not empty and that the name does not
     * equal the element's original name.
     * <p>
     * If {@link #isPluralNameRefactoringRequired()} is true the same rules apply for the new plural
     * name.
     */
    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();

        if (newName.length() < 1) {
            status.addFatalError(Messages.IpsRenameProcessor_msgNewNameEmpty);
            return status;
        }
        if (newName.equals(originalName)) {
            status.addFatalError(Messages.IpsRenameProcessor_msgNewNameEqualsOriginalName);
            return status;
        }

        if (isPluralNameRefactoringRequired()) {
            if (newPluralName.length() < 1) {
                status.addFatalError(Messages.IpsRenameProcessor_msgNewPluralNameEmpty);
                return status;
            }
            if (newPluralName.equals(originalPluralName)) {
                status.addFatalError(Messages.IpsRenameProcessor_msgNewPluralNameEqualsOriginalPluralName);
                return status;
            }
        }

        validateUserInputThis(status, pm);
        return status;
    }

    /**
     * This operation is called by {@link #validateUserInput(IProgressMonitor)}. Subclasses must
     * implement special user input validations here.
     * 
     * @param status {@link RefactoringStatus} to report messages to
     * @param pm {@link IProgressMonitor} to report progress to
     * 
     * @throws CoreException May be thrown at any time
     */
    protected abstract void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException;

    @Override
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) throws CoreException {

        return ParticipantManager.loadRenameParticipants(status, this, getIpsElement(), new IpsRenameArguments(newName,
                newPluralName, true), new String[] { IIpsProject.NATURE_ID }, sharedParticipants);
    }

    @Override
    public final void setNewName(String newName) {
        ArgumentCheck.notNull(newName);
        this.newName = newName;
    }

    @Override
    public void setNewPluralName(String newPluralName) {
        ArgumentCheck.notNull(newPluralName);
        this.newPluralName = newPluralName;
    }

    @Override
    public void setPluralNameRefactoringRequired(boolean pluralNameRefactoringRequired) {
        this.pluralNameRefactoringRequired = pluralNameRefactoringRequired;
    }

    @Override
    public boolean isPluralNameRefactoringRequired() {
        return pluralNameRefactoringRequired;
    }

    @Override
    public final String getOriginalName() {
        return originalName;
    }

    @Override
    public String getOriginalPluralName() {
        return originalPluralName;
    }

    @Override
    public final String getNewName() {
        return newName;
    }

    @Override
    public String getNewPluralName() {
        return newPluralName;
    }

}
