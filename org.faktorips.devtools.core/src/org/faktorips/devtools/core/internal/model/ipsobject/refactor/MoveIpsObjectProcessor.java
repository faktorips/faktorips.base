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

/**
 * This is the "Move Type" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class MoveIpsObjectProcessor extends IpsMoveProcessor {

    /**
     * A helper providing functionality shared between the "Rename Type" and "Move Type"
     * refactorings.
     */
    private final MoveRenameIpsObjectHelper renameMoveHelper;

    /**
     * @param toBeRefactored The <tt>BaseIpsObject</tt> to be moved.
     */
    public MoveIpsObjectProcessor(IpsObject toBeMoved) {
        super(toBeMoved);
        renameMoveHelper = new MoveRenameIpsObjectHelper(this, toBeMoved);
        renameMoveHelper.addIgnoredValidationMessageCodes(getIgnoredValidationMessageCodes());
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        renameMoveHelper.addIpsSrcFiles();
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        renameMoveHelper.validateUserInputThis(getTargetIpsPackageFragment(), getIpsElement().getName(), status, pm);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        renameMoveHelper.checkFinalConditionsThis(getTargetIpsPackageFragment(), getIpsElement().getName(), status, pm,
                context);
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        renameMoveHelper.refactorIpsModel(getTargetIpsPackageFragment(), getIpsElement().getName(), pm);
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.MoveTypeProcessor";
    }

    @Override
    public String getProcessorName() {
        return Messages.MoveTypeProcessor_processorName;
    }

}
