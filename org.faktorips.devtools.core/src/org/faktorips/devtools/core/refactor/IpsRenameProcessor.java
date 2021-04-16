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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.internal.refactor.Messages;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for all Faktor-IPS "Rename" refactoring processors.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRenameProcessor extends IpsRefactoringProcessor {

    /** Original name of the {@link IIpsElement} to be refactored. */
    private final String originalName;

    /** Original plural name of the {@link IIpsElement} to be refactored. */
    private final String originalPluralName;

    /** New name for the {@link IIpsElement} to be refactored. */
    private String newName;

    /** New plural name for the {@link IIpsElement} to be refactored. */
    private String newPluralName;

    /** Flag indicating whether the runtime ID of an {@link IProductCmpt} should be adapted. */
    private boolean adaptRuntimeId;

    /**
     * @param ipsElement The {@link IIpsElement} to be refactored
     * @param originalName Original name of the {@link IIpsElement} to be refactored
     * @param originalPluralName Original plural name of the {@link IIpsElement} to be refactored
     */
    protected IpsRenameProcessor(IIpsElement ipsElement, String originalName, String originalPluralName) {
        super(ipsElement);
        this.originalName = originalName;
        this.originalPluralName = originalPluralName;
    }

    /**
     * @param ipsElement The {@link IIpsElement} to be refactored
     * @param originalName Original name of the {@link IIpsElement} to be refactored
     */
    protected IpsRenameProcessor(IIpsElement ipsElement, String originalName) {
        this(ipsElement, originalName, null);
    }

    /**
     * This implementation validates the element's new name. It checks that the name is not empty
     * and that the name does not equal the element's original name.
     */
    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (newName.isEmpty()) {
            status.addFatalError(Messages.IpsRenameProcessor_msgNewNameEmpty);
            return;
        }
        if (newName.equals(originalName)) {
            status.addFatalError(Messages.IpsRenameProcessor_msgNewNameEqualsOriginalName);
        }
    }

    @Override
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) throws CoreException {

        return ParticipantManager.loadRenameParticipants(status, this, getIpsElement(), new IpsRenameArguments(newName,
                newPluralName, true), new String[] { IIpsProject.NATURE_ID }, sharedParticipants);
    }

    /**
     * Sets the new name for the {@link IIpsElement} to be refactored.
     * 
     * @param newName New name for the {@link IIpsElement} to be refactored
     * 
     * @throws NullPointerException If the parameter is null
     */
    public final void setNewName(String newName) {
        ArgumentCheck.notNull(newName);
        this.newName = newName;
    }

    /**
     * Sets the new plural name for the {@link IIpsElement} to be refactored.
     * 
     * @param newPluralName New plural name for the {@link IIpsElement} to be refactored
     * 
     * @throws NullPointerException If the parameter is null
     */
    public final void setNewPluralName(String newPluralName) {
        ArgumentCheck.notNull(newPluralName);
        this.newPluralName = newPluralName;
    }

    /**
     * Sets whether the runtime ID of {@link IProductCmpt} should be adapted.
     * 
     * @param adaptRuntimeId Flag indicating whether to adapt runtime IDs
     */
    public final void setAdaptRuntimeId(boolean adaptRuntimeId) {
        this.adaptRuntimeId = adaptRuntimeId;
    }

    /**
     * Returns whether a plural name refactoring is required.
     */
    public boolean isPluralNameRefactoringRequired() {
        return false;
    }

    /**
     * Returns the element's original name.
     */
    public final String getOriginalName() {
        return originalName;
    }

    /**
     * Returns the element's original plural name.
     */
    public final String getOriginalPluralName() {
        return originalPluralName;
    }

    /**
     * Returns the element's new name.
     */
    public final String getNewName() {
        return newName;
    }

    /**
     * Returns the element's new plural name.
     */
    public final String getNewPluralName() {
        return newPluralName;
    }

    /**
     * Returns whether the runtime ID of {@link IProductCmpt} should be adapted.
     */
    public final boolean isAdaptRuntimeId() {
        return adaptRuntimeId;
    }

}
