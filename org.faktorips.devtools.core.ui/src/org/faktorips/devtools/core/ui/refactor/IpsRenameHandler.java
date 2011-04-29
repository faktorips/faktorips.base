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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRenameRefactoringWizard;

/**
 * Provides the rename workbench contribution which opens the appropriate Faktor-IPS refactoring
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
    protected IpsRefactoringWizard getRefactoringWizard(Refactoring refactoring, IIpsElement selectedIpsElement) {
        return new IpsRenameRefactoringWizard(refactoring, selectedIpsElement);
    }

    @Override
    protected ProcessorBasedRefactoring getRefactoring(IIpsElement selectedElement) {
        return selectedElement.getRenameRefactoring();
    }

}
