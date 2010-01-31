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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.model.type.IType;

/**
 * This is the "Rename Type" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameTypeProcessor extends IpsRenameProcessor {

    /**
     * A helper providing functionality shared between the "Rename Type" and "Move Type"
     * refactorings.
     */
    private final RenameTypeMoveTypeHelper renameMoveHelper;

    /**
     * Creates a <tt>RenameTypeProcessor</tt>.
     * 
     * @param type The <tt>IType</tt> to be renamed.
     */
    public RenameTypeProcessor(IType type) {
        super(type);
        renameMoveHelper = new RenameTypeMoveTypeHelper(this, type);
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
        renameMoveHelper.validateUserInputThis(getType().getIpsPackageFragment(), getNewName(), status, pm);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        renameMoveHelper.checkFinalConditionsThis(getType().getIpsPackageFragment(), getNewName(), status, pm, context);
    }

    @Override
    protected Change refactorIpsModel(IProgressMonitor pm) throws CoreException {
        return renameMoveHelper.refactorIpsModel(getType().getIpsPackageFragment(), getNewName(), pm);
    }

    /** Returns the <tt>IType</tt> to be renamed. */
    private IType getType() {
        return (IType)getIpsElement();
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
