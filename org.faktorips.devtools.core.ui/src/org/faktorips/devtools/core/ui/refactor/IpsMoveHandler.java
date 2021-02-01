/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.refactor;

import java.util.Set;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.refactor.IIpsCompositeMoveRefactoring;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsMoveRefactoringWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;
import org.faktorips.devtools.model.IIpsElement;

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
    protected IIpsRefactoring getRefactoring(Set<IIpsElement> selectedIpsElements) {
        return IpsPlugin.getIpsRefactoringFactory().createCompositeMoveRefactoring(selectedIpsElements);
    }

}
