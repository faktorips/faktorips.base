/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.refactor.IpsRenameProcessor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.message.MessageList;

/**
 * This is the "Rename IPS Object" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameIpsObjectProcessor extends IpsRenameProcessor {

    /**
     * A helper providing functionality shared between the "Rename IPS Object" and "Move IPS Object"
     * refactorings.
     */
    private final MoveRenameIpsObjectHelper renameMoveHelper;

    /**
     * @param toBeRefactored The <tt>IIpsObject</tt> to be renamed.
     */
    public RenameIpsObjectProcessor(IpsObject toBeRefactored) {
        super(toBeRefactored, toBeRefactored.getName());
        renameMoveHelper = new MoveRenameIpsObjectHelper(toBeRefactored);
        renameMoveHelper.addIgnoredValidationMessageCodes(getIgnoredValidationMessageCodes());
    }

    @Override
    protected void addIpsSrcFiles() throws CoreException {
        List<IIpsSrcFile> ipsSrcFiles = renameMoveHelper.addIpsSrcFiles();
        for (IIpsSrcFile ipsSrcFile : ipsSrcFiles) {
            addIpsSrcFile(ipsSrcFile);
        }
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        renameMoveHelper.validateUserInputThis(getIpsObject().getIpsPackageFragment(), getNewName(), status);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        MessageList validationMessageList = renameMoveHelper.checkFinalConditionsThis(getIpsObject()
                .getIpsPackageFragment(), getNewName(), status, pm);
        addValidationMessagesToStatus(validationMessageList, status);
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        renameMoveHelper.refactorIpsModel(getIpsObject().getIpsPackageFragment(), getNewName(), isAdaptRuntimeId(), pm);
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return renameMoveHelper.isSourceFilesSavedRequired();
    }

    private IpsObject getIpsObject() {
        return (IpsObject)getIpsElement();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.RenameIpsObjectProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameIpsObjectProcessor_processorName;
    }

}
