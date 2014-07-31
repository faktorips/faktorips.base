/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.devtools.core.refactor.IpsRefactoringModificationSet;
import org.faktorips.util.message.MessageList;

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
    protected void checkInitialConditionsThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        super.checkInitialConditionsThis(status, pm);
        moveRenameHelper.checkInitialConditions(status);
    }

    @Override
    protected void checkFinalConditionsThis(RefactoringStatus status,
            IProgressMonitor pm,
            CheckConditionsContext context) throws CoreException {
        super.checkFinalConditionsThis(status, pm, context);
        IIpsPackageFragment target = getTargetIpsPackageFragment().getSubPackage(
                getOriginalIpsPackageFragment().getLastSegmentName());

        if (target != null && target.exists()) {
            status.addFatalError(NLS.bind(Messages.IpsPackageFragmentProcessor_errorPackageAlreadyContains,
                    target.getName()));
        }
    }

    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        super.validateUserInputThis(status, pm);
        if (getTargetIpsPackageFragment().equals(getOriginalIpsPackageFragment().getParentIpsPackageFragment())) {
            status.addFatalError(NLS.bind(
                    Messages.IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentEqualsOriginalIpsPackageFragment,
                    getOriginalIpsPackageFragment().getName()));
        }
        if (getOriginalIpsPackageFragment().isDefaultPackage()
                && getTargetIpsPackageFragment().getIpsProject()
                        .equals(getOriginalIpsPackageFragment().getIpsProject())) {
            status.addFatalError(Messages.IpsCompositeMoveRefactoring_msgDefaultPackageInSameProject);
        }
    }

    @Override
    public IpsRefactoringModificationSet refactorIpsModel(IProgressMonitor pm) throws CoreException {
        return moveRenameHelper.movePackageFragment(getTargetIpsPackageFragment(), pm);
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
        return true;
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
