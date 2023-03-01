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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.ArgumentCheck;

/**
 * @author Alexander Weickmann
 */
public final class IpsCompositeMoveRefactoring extends IpsCompositeRefactoring implements IIpsCompositeMoveRefactoring {

    private IIpsPackageFragment targetIpsPackageFragment;

    public IpsCompositeMoveRefactoring(Set<IIpsElement> ipsObjects) {
        super(new LinkedHashSet<>(ipsObjects));
    }

    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) {
        clearSkippedElements();

        RefactoringStatus refactoringStatus = new RefactoringStatus();

        if (targetIpsPackageFragment == null) {
            refactoringStatus.addFatalError(Messages.IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentNotSet);
            return refactoringStatus;
        }

        for (IIpsElement ipsElement : getIpsElements()) {
            if (ipsElement instanceof IIpsObject ipsObject) {
                if (ipsObject.getIpsPackageFragment().equals(targetIpsPackageFragment)) {
                    refactoringStatus
                            .addWarning(NLS
                                    .bind(Messages.IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentEqualsOriginalIpsPackageFragment,
                                            ipsObject.getName()));
                    skipElement(ipsObject);
                }
            } else if (ipsElement instanceof IIpsPackageFragment packageFragment) {
                if (targetIpsPackageFragment.equals(packageFragment.getParentIpsPackageFragment())) {
                    refactoringStatus
                            .addError(NLS
                                    .bind(Messages.IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentEqualsOriginalIpsPackageFragment,
                                            packageFragment.getName()));
                    skipElement(packageFragment);
                }
                if (packageFragment.isDefaultPackage()
                        && targetIpsPackageFragment.getIpsProject().equals(packageFragment.getIpsProject())) {
                    refactoringStatus.addError(Messages.IpsCompositeMoveRefactoring_msgDefaultPackageInSameProject);
                    skipElement(packageFragment);
                }
            }
        }

        return refactoringStatus;
    }

    @Override
    protected IIpsRefactoring createRefactoring(IIpsElement ipsElement) {
        return IpsPlugin.getIpsRefactoringFactory().createMoveRefactoring(ipsElement, targetIpsPackageFragment);
    }

    @Override
    public String getName() {
        return Messages.IpsCompositeMoveRefactoring_name;
    }

    @Override
    public void setTargetIpsPackageFragment(IIpsPackageFragment targetIpsPackageFragment) {
        ArgumentCheck.notNull(targetIpsPackageFragment);
        this.targetIpsPackageFragment = targetIpsPackageFragment;
    }

    @Override
    public IIpsPackageFragment getTargetIpsPackageFragment() {
        return targetIpsPackageFragment;
    }

}
