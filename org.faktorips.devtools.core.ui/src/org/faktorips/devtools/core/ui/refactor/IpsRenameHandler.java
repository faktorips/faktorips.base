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

package org.faktorips.devtools.core.ui.refactor;

import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRenameRefactoringWizard;

/**
 * Provides the "Rename" workbench contribution which opens the appropriate Faktor-IPS refactoring
 * wizard.
 * 
 * @author Thorsten Guenther, Alexander Weickmann
 */
public class IpsRenameHandler extends IpsRefactoringHandler {

    public static final String CONTRIBUTION_ID = "org.eclipse.ui.edit.rename"; //$NON-NLS-1$

    @Override
    protected MoveWizard getMoveWizard(IStructuredSelection selection) {
        return new MoveWizard(selection, MoveWizard.OPERATION_RENAME);
    }

    @Override
    protected IpsRefactoringWizard getRefactoringWizard(IIpsRefactoring refactoring) {
        return new IpsRenameRefactoringWizard(refactoring);
    }

    @Override
    protected IIpsRefactoring getRefactoring(Set<IIpsElement> selectedIpsElements) {
        return IpsPlugin.getIpsRefactoringFactory().createRenameRefactoring(
                selectedIpsElements.toArray(new IIpsElement[selectedIpsElements.size()])[0]);
    }

}
