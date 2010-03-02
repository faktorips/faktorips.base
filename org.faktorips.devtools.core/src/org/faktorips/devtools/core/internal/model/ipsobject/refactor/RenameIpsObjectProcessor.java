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

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * This is the "Rename Ips Object" - refactoring.
 * <p>
 * If an {@link IIpsObject} is renamed, the complete workspace is scanned for other
 * {@link IIpsObject}s, depending on the modified one. Every found dependant is refactored to use
 * the {@link IIpsObject} with the new name.
 * 
 * @author Alexander Weickmann
 */
public final class RenameIpsObjectProcessor extends IpsRenameProcessor {

    /**
     * A helper providing functionality shared between the "Rename Type" and "Move Type"
     * refactorings.
     */
    private final MoveRenameIpsObjectHelper renameMoveHelper;

    /**
     * @param toBeRefactored The object to be renamed.
     */
    public RenameIpsObjectProcessor(IpsObject toBeRefactored) {
        super(toBeRefactored);
        renameMoveHelper = new MoveRenameIpsObjectHelper(this, toBeRefactored);
        renameMoveHelper.addIgnoredValidationMessageCodes(getIgnoredValidationMessageCodes());
    }

    @Override
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        renameMoveHelper.checkInitialConditionsThis(status, pm);
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        renameMoveHelper.addIpsSrcFiles();
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        renameMoveHelper.validateUserInputThis(getObject().getIpsPackageFragment(), getNewName(), status, pm);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        renameMoveHelper.checkFinalConditionsThis(getObject().getIpsPackageFragment(), getNewName(), status, pm,
                context);
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        renameMoveHelper.refactorIpsModel(getObject().getIpsPackageFragment(), getNewName(), pm);
    }

    /** Returns the <tt>IType</tt> to be renamed. */
    private IpsObject getObject() {
        return (IpsObject)getIpsElement();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameTypeProcessor";
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameTypeProcessor_processorName;
    }

}
