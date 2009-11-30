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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public abstract class RenameRefactoringProcessor extends RenameProcessor {

    private final IIpsObjectPartContainer ipsObjectPartContainer;

    private String newName;

    protected RenameRefactoringProcessor(IIpsObjectPartContainer ipsObjectPartContainer) {
        super();
        ArgumentCheck.notNull(ipsObjectPartContainer);
        this.ipsObjectPartContainer = ipsObjectPartContainer;
        newName = "";
    }

    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws CoreException, OperationCanceledException {

        return new RefactoringStatus();
    }

    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        return new RefactoringStatus();
    }

    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return null;
    }

    @Override
    public final Change postCreateChange(Change[] participantChanges, IProgressMonitor pm) throws CoreException,
            OperationCanceledException {

        refactorModel(pm);
        return null;
    }

    @Override
    public final Object[] getElements() {
        return new Object[] { ipsObjectPartContainer };
    }

    @Override
    public final boolean isApplicable() throws CoreException {
        return ipsObjectPartContainer.exists();
    }

    public final void setNewName(String newName) {
        this.newName = newName;
    }

    protected final IIpsObjectPartContainer getIpsObjectPartContainer() {
        return ipsObjectPartContainer;
    }

    protected final String getNewName() {
        return newName;
    }

    @Override
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) throws CoreException {

        return ParticipantManager.loadRenameParticipants(status, this, ipsObjectPartContainer, new RenameArguments(
                newName, true), new String[] { IIpsProject.NATURE_ID }, sharedParticipants);
    }

    protected abstract void refactorModel(IProgressMonitor pm) throws CoreException;

}
