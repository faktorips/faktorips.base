/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest.core;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

public abstract class AbstractCoreIpsPluginTest extends AbstractIpsPluginTest {

    /**
     * Performs the Faktor-IPS 'Rename' refactoring for the given {@link IIpsObjectPartContainer}
     * and provided new name.
     */
    protected final RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName) {

        return performRenameRefactoring(ipsObjectPartContainer, newName, null, false);
    }

    /**
     * Performs the Faktor-IPS 'Rename' refactoring for the given {@link IIpsPackageFragment} and
     * provided new name.
     */
    protected final RefactoringStatus performRenameRefactoring(IIpsPackageFragment ipsPackageFragment, String newName) {
        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory()
                .createRenameRefactoring(ipsPackageFragment, newName, null, false);

        return performRefactoring(ipsRenameRefactoring);
    }

    /**
     * Performs the Faktor-IPS 'Rename' refactoring for the given {@link IProductCmpt} and provided
     * new name, thereby allowing to adapt the runtime id.
     */
    protected final RefactoringStatus performRenameRefactoring(IProductCmpt productCmpt,
            String newName,
            boolean adaptRuntimeId) {

        return performRenameRefactoring(productCmpt, newName, null, adaptRuntimeId);
    }

    /**
     * Performs the Faktor-IPS 'Rename' refactoring for the given {@link IIpsObjectPartContainer},
     * provided new name and provided new plural name.
     */
    protected final RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName,
            String newPluralName) {

        return performRenameRefactoring(ipsObjectPartContainer, newName, newPluralName, false);
    }

    private RefactoringStatus performRenameRefactoring(IIpsObjectPartContainer ipsObjectPartContainer,
            String newName,
            String newPluralName,
            boolean adaptRuntimeId) {

        printValidationResult(ipsObjectPartContainer);

        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory()
                .createRenameRefactoring(ipsObjectPartContainer, newName, newPluralName, adaptRuntimeId);

        return performRefactoring(ipsRenameRefactoring);
    }

    /**
     * Performs the Faktor-IPS 'Pull Up' refactoring for the given {@link IIpsObjectPart} and target
     * {@link IIpsObjectPartContainer}.
     */
    protected final RefactoringStatus performPullUpRefactoring(IIpsObjectPart ipsObjectPart,
            IIpsObjectPartContainer targetIpsObjectPartContainer) {

        printValidationResult(ipsObjectPart);

        IIpsRefactoring ipsPullUpRefactoring = IpsPlugin.getIpsRefactoringFactory()
                .createPullUpRefactoring(ipsObjectPart, targetIpsObjectPartContainer);

        return performRefactoring(ipsPullUpRefactoring);
    }

    /**
     * Performs the Faktor-IPS 'Move' refactoring for the given {@link IIpsObject} and provided
     * target {@link IIpsPackageFragment}.
     */
    protected final RefactoringStatus performMoveRefactoring(IIpsObject ipsObject,
            IIpsPackageFragment targetIpsPackageFragment) {

        printValidationResult(ipsObject);

        IIpsRefactoring ipsMoveRefactoring = IpsPlugin.getIpsRefactoringFactory().createMoveRefactoring(ipsObject,
                targetIpsPackageFragment);

        return performRefactoring(ipsMoveRefactoring);
    }

    /**
     * Performs a composite Faktor-IPS 'Move' refactoring for the given {@link IIpsObject}s and
     * provided target {@link IIpsPackageFragment}.
     */
    protected final RefactoringStatus performCompositeMoveRefactoring(Set<IIpsObject> ipsObjects,
            IIpsPackageFragment targetIpsPackageFragment) {

        Set<IIpsElement> ipsElemets = new LinkedHashSet<>();
        for (IIpsObject ipsObject : ipsObjects) {
            printValidationResult(ipsObject);
            ipsObject.getIpsSrcFile().save(null);
            ipsElemets.add(ipsObject);
        }

        IIpsRefactoring ipsCompositeMoveRefactoring = IpsPlugin.getIpsRefactoringFactory()
                .createCompositeMoveRefactoring(ipsElemets, targetIpsPackageFragment);

        return performRefactoring(ipsCompositeMoveRefactoring);
    }

    private RefactoringStatus performRefactoring(IIpsRefactoring ipsRefactoring) {
        PerformRefactoringOperation operation = new PerformRefactoringOperation(ipsRefactoring.toLtkRefactoring(),
                CheckConditionsOperation.ALL_CONDITIONS);
        Abstractions.getWorkspace().run(operation, new NullProgressMonitor());
        RefactoringStatus conditionStatus = operation.getConditionStatus();
        return conditionStatus;
    }
}
