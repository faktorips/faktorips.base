/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.refactor;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.refactor.IIpsProcessorBasedRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.refactor.IpsMoveProcessor;
import org.faktorips.util.ArgumentCheck;

/**
 * @author Alexander Weickmann
 */
public final class IpsCompositeMoveRefactoring extends IpsCompositeRefactoring implements IIpsCompositeMoveRefactoring {

    private IIpsPackageFragment targetIpsPackageFragment;

    public IpsCompositeMoveRefactoring(Set<IIpsObject> ipsObjects) {
        super(new LinkedHashSet<IIpsElement>(ipsObjects));
    }

    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        clearSkippedElements();

        RefactoringStatus refactoringStatus = new RefactoringStatus();

        if (targetIpsPackageFragment == null) {
            refactoringStatus.addFatalError(Messages.IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentNotSet);
            return refactoringStatus;
        }

        for (IIpsElement ipsElement : getIpsElements()) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            if (ipsObject.getIpsPackageFragment().equals(targetIpsPackageFragment)) {
                refactoringStatus
                        .addWarning(NLS
                                .bind(Messages.IpsCompositeMoveRefactoring_msgTargetIpsPackageFragmentEqualsOriginalIpsPackageFragment,
                                        ipsObject.getName()));
                skipElement(ipsObject);
            }
        }

        return refactoringStatus;
    }

    @Override
    protected IIpsRefactoring createRefactoring(IIpsElement ipsElement) {
        IIpsProcessorBasedRefactoring ipsMoveRefactoring = IpsPlugin.getIpsRefactoringFactory().createMoveRefactoring(
                (IIpsObject)ipsElement);
        IpsMoveProcessor ipsMoveProcessor = (IpsMoveProcessor)ipsMoveRefactoring.getIpsRefactoringProcessor();
        if (targetIpsPackageFragment != null) {
            ipsMoveProcessor.setTargetIpsPackageFragment(targetIpsPackageFragment);
        }
        return ipsMoveRefactoring;
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
