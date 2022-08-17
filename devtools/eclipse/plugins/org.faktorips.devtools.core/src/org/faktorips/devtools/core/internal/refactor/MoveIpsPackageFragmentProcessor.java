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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.runtime.MessageList;

/**
 * Move processor for {@link IIpsPackageFragment}.
 */
public class MoveIpsPackageFragmentProcessor extends IpsMoveProcessor {

    private final MoveRenamePackageHelper moveRenameHelper;

    public MoveIpsPackageFragmentProcessor(IIpsPackageFragment ipsPackageFragment) {
        super(ipsPackageFragment, ipsPackageFragment);
        moveRenameHelper = new MoveRenamePackageHelper(ipsPackageFragment);
    }

    @Override
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) {
        super.checkInitialConditionsThis(status, pm);
        moveRenameHelper.checkInitialConditions(status);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) {
        super.checkFinalConditionsThis(status, pm, context);
        IIpsPackageFragment newPackageFragment = getTargetIpsPackageFragment().getSubPackage(
                getOriginalIpsPackageFragment().getLastSegmentName());

        moveRenameHelper.checkFinalConditions(newPackageFragment, status, pm);
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) {
        super.validateUserInputThis(status, pm);
        if (isTargetParentPackage()) {
            status.addFatalError(NLS.bind(
                    Messages.IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentEqualsOriginalIpsPackageFragment,
                    getOriginalIpsPackageFragment().getName()));
            return;
        }
        if (isDefaultPackageMovedToSameProject()) {
            status.addFatalError(Messages.IpsCompositeMoveRefactoring_msgDefaultPackageInSameProject);
            return;
        }
        IIpsPackageFragment newPackageFragment = getTargetIpsPackageFragment().getSubPackage(
                getOriginalIpsPackageFragment().getLastSegmentName());
        moveRenameHelper.validateUserInput(newPackageFragment, status);
    }

    private boolean isTargetParentPackage() {
        return getTargetIpsPackageFragment().equals(getOriginalIpsPackageFragment().getParentIpsPackageFragment());
    }

    private boolean isDefaultPackageMovedToSameProject() {
        return getOriginalIpsPackageFragment().isDefaultPackage()
                && getTargetIpsPackageFragment().getIpsProject()
                        .equals(getOriginalIpsPackageFragment().getIpsProject());
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) {
        return moveRenameHelper.movePackageFragment(getTargetIpsPackageFragment(), pm);
    }

    @Override
    protected Set<IIpsSrcFile> getAffectedIpsSrcFiles() {
        return moveRenameHelper.getAffectedIpsSrcFiles();
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) {
        // nichts zu tun
    }

    @Override
    public boolean isSourceFilesSavedRequired() {
        return moveRenameHelper.isSourceFilesSavedRequired();
    }

    @Override
    public String getIdentifier() {
        return "org.faktorips.devtools.core.internal.refactor.MoveIpsPackageFragmentProcessor"; //$NON-NLS-1$
    }

    @Override
    public String getProcessorName() {
        return Messages.MoveIpsPackageFragmentProcessor_processorName;
    }
}
