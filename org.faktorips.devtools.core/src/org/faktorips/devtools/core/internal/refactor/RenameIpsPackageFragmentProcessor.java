/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.runtime.MessageList;

/**
 * Rename processor for {@link IIpsPackageFragment}.
 */
public class RenameIpsPackageFragmentProcessor extends IpsRenameProcessor {

    private final MoveRenamePackageHelper moveRenameHelper;

    public RenameIpsPackageFragmentProcessor(IIpsPackageFragment ipsPackageFragment) {
        super(ipsPackageFragment, ipsPackageFragment.getName());
        moveRenameHelper = new MoveRenamePackageHelper(ipsPackageFragment);
    }

    private IIpsPackageFragment getOriginalIpsPackageFragment() {
        return (IIpsPackageFragment)getIpsElement();
    }

    @Override
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        super.checkInitialConditionsThis(status, pm);
        moveRenameHelper.checkInitialConditions(status);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {
        super.checkFinalConditionsThis(status, pm, context);
        IIpsPackageFragment newPackageFragment = getOriginalIpsPackageFragment().getRoot().getIpsPackageFragment(
                getNewName());
        moveRenameHelper.checkFinalConditions(newPackageFragment, status, pm);
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        super.validateUserInputThis(status, pm);
        IIpsPackageFragment newPackageFragment = getOriginalIpsPackageFragment().getRoot().getIpsPackageFragment(
                getNewName());
        moveRenameHelper.validateUserInput(newPackageFragment, status);
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException {
        return moveRenameHelper.renamePackageFragment(getNewName(), pm);
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        return moveRenameHelper.getAffectedIpsSrcFiles();
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) throws CoreException {
        // nichts zu tun
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return moveRenameHelper.isSourceFilesSavedRequired();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.refactor.RenameIpsPackageFragmentProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.RenameIpsPackageFragmentProcessor_processorName;
    }
}
