/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.util.message.MessageList;

/**
 * Refactoring processor for the "Rename IPS Object" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameIpsObjectProcessor extends IpsRenameProcessor implements IIpsMoveRenameIpsObjectProcessor {

    /**
     * A helper providing functionality shared between the "Rename IPS Object" and "Move IPS Object"
     * refactoring processors.
     */
    private final MoveRenameIpsObjectHelper renameMoveHelper;

    public RenameIpsObjectProcessor(IIpsObject toBeRenamed) {
        super(toBeRenamed, toBeRenamed.getName());
        renameMoveHelper = new MoveRenameIpsObjectHelper(toBeRenamed);
        renameMoveHelper.addIgnoredValidationMessageCodes(getIgnoredValidationMessageCodes());
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        HashSet<IIpsSrcFile> result = new HashSet<IIpsSrcFile>();
        result.addAll(renameMoveHelper.getAffectedIpsSrcFiles());
        return result;
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) throws CoreException {
        renameMoveHelper.validateIpsModel(getIpsObject().getIpsPackageFragment(), getNewName(), validationMessageList);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {
        MessageList validationMessageList = renameMoveHelper.checkFinalConditionsThis(this, status, pm);
        addValidationMessagesToStatus(validationMessageList, status);
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException {
        return renameMoveHelper.refactorIpsModel(getIpsObject().getIpsPackageFragment(), getNewName(),
                isAdaptRuntimeId(), pm);
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

    @Override
    public List<IJavaElement> getTargetJavaElements() {
        return renameMoveHelper.getTargetJavaElements();
    }

}
