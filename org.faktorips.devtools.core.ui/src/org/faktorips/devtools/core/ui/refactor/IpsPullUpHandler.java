/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.refactor;

import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsPullUpRefactoringWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;

/**
 * Provides the "Pull Up" workbench contribution which opens the appropriate Faktor-IPS refactoring
 * wizard.
 * 
 * @since 3.4
 * 
 * @author Alexander Weickmann
 */
public class IpsPullUpHandler extends IpsRefactoringHandler {

    public static final String CONTRIBUTION_ID = "org.faktorips.devtools.core.refactor.pullUp"; //$NON-NLS-1$

    @Override
    protected MoveWizard getMoveWizard(IStructuredSelection selection) {
        // TODO AW 27-05-2011: Need to remove old refactoring parts
        return null;
    }

    @Override
    protected IpsRefactoringWizard getRefactoringWizard(IIpsRefactoring refactoring) {
        return new IpsPullUpRefactoringWizard(refactoring);
    }

    @Override
    protected IIpsRefactoring getRefactoring(Set<IIpsElement> selectedIpsElements) {
        return IpsPlugin.getIpsRefactoringFactory().createPullUpRefactoring(
                (IIpsObjectPart)selectedIpsElements.toArray(new IIpsElement[selectedIpsElements.size()])[0]);
    }

}
