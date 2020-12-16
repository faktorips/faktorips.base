/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;

/**
 * Abstract base class for all Faktor-IPS refactoring wizards.
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsRefactoringWizard extends RefactoringWizard {

    /**
     * @param refactoring The refactoring used by the wizard
     * @param flags Flags specifying the behavior of the wizard, see
     *            {@link RefactoringWizard#RefactoringWizard(Refactoring, int)}
     * 
     * @throws NullPointerException If any parameter is null
     */
    protected IpsRefactoringWizard(IIpsRefactoring refactoring, int flags) {
        super((Refactoring)refactoring, flags);
    }

    /**
     * Returns the {@link IIpsRefactoring} this wizard is associated with.
     */
    public final IIpsRefactoring getIpsRefactoring() {
        return (IIpsRefactoring)getRefactoring();
    }

}
