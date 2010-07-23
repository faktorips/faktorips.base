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

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.message.MessageList;

/**
 * This is the "Move IPS Object" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class MoveIpsObjectProcessor extends IpsMoveProcessor {

    /**
     * A helper providing functionality shared between the "Rename IPS Object" and "Move IPS Object"
     * refactorings.
     */
    private final MoveRenameIpsObjectHelper renameMoveHelper;

    /**
     * @param toBeMoved The <tt>IIpsObject</tt> to be moved.
     */
    public MoveIpsObjectProcessor(IpsObject toBeMoved) {
        super(toBeMoved);
        renameMoveHelper = new MoveRenameIpsObjectHelper(toBeMoved);
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
        renameMoveHelper.validateUserInputThis(getTargetIpsPackageFragment(), getIpsElement().getName(), status);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {

        MessageList validationMessageList = renameMoveHelper.checkFinalConditionsThis(getTargetIpsPackageFragment(),
                getIpsElement().getName(), status, pm);
        addValidationMessagesToStatus(validationMessageList, status);
    }

    @Override
    protected void refactorIpsModel(IProgressMonitor pm) throws CoreException {
        renameMoveHelper.refactorIpsModel(getTargetIpsPackageFragment(), getIpsElement().getName(), pm);
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.MoveIpsObjectProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.MoveIpsObjectProcessor_processorName;
    }

}
