/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.refactor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.wizards.move.MoveWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsMoveRefactoringWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;

/**
 * Provides the "Move" workbench contribution which opens the appropriate Faktor-IPS refactoring
 * wizard.
 * 
 * @author Thorsten Guenther, Alexander Weickmann
 */
public class IpsMoveHandler extends IpsRefactoringHandler {

    public static final String CONTRIBUTION_ID = "org.eclipse.ui.edit.move"; //$NON-NLS-1$

    @Override
    protected IpsRefactoringWizard getRefactoringWizard(IIpsRefactoring refactoring) {
        return new IpsMoveRefactoringWizard((IIpsCompositeMoveRefactoring)refactoring);
    }

    @Override
    protected MoveWizard getMoveWizard(IStructuredSelection selection) {
        return new MoveWizard(selection, MoveWizard.OPERATION_MOVE);
    }

    @Override
    protected IIpsRefactoring getRefactoring(Set<IIpsElement> selectedIpsElements) {
        Set<IIpsObject> ipsObjects = new HashSet<IIpsObject>(selectedIpsElements.size());
        for (IIpsElement ipsElement : selectedIpsElements) {
            if (!(ipsElement instanceof IIpsObject)) {
                return null;
            }
            ipsObjects.add((IIpsObject)ipsElement);
        }
        return IpsPlugin.getIpsRefactoringFactory().createCompositeMoveRefactoring(ipsObjects);
    }

}
