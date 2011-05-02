/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
