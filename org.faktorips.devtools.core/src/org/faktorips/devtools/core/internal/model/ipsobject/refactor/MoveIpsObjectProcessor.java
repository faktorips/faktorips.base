/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.model.ipsobject.refactor.IIpsMoveRenameIpsObjectProcessor;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.runtime.MessageList;

/**
 * Refactoring processor for the "Move IPS Object" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class MoveIpsObjectProcessor extends IpsMoveProcessor implements IIpsMoveRenameIpsObjectProcessor {

    /**
     * A helper providing functionality shared between the "Rename IPS Object" and "Move IPS Object"
     * refactoring processors.
     */
    private final MoveRenameIpsObjectHelper renameMoveHelper;

    public MoveIpsObjectProcessor(IIpsObject toBeMoved) {
        super(toBeMoved, toBeMoved.getIpsPackageFragment());
        renameMoveHelper = new MoveRenameIpsObjectHelper(toBeMoved);
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        Set<IIpsSrcFile> result = new HashSet<>(renameMoveHelper.getAffectedIpsSrcFiles());
        return result;
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) throws CoreRuntimeException {
        renameMoveHelper.validateIpsModel(getTargetIpsPackageFragment(), getIpsElement().getName(),
                validationMessageList);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreRuntimeException {
        MessageList validationMessageList = renameMoveHelper.checkFinalConditionsThis(this, status, pm);
        addValidationMessagesToStatus(validationMessageList, status);
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreRuntimeException {
        return renameMoveHelper.refactorIpsModel(getTargetIpsPackageFragment(), getIpsElement().getName(), false, pm);
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return renameMoveHelper.isSourceFilesSavedRequired();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.model.type.refactor.MoveIpsObjectProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.MoveIpsObjectProcessor_processorName;
    }

    @Override
    public List<IJavaElement> getTargetJavaElements() {
        return renameMoveHelper.getTargetJavaElements();
    }

}
