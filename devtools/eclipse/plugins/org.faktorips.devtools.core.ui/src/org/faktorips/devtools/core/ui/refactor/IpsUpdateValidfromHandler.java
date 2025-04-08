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

import org.eclipse.core.commands.ExecutionEvent;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;
import org.faktorips.devtools.model.IIpsElement;

public class IpsUpdateValidfromHandler extends IpsRefactoringHandler {

    public static final String CONTRIBUTION_ID = "org.faktorips.devtools.core.refactor.updateValidfrom"; //$NON-NLS-1$

    @Override
    protected IpsRefactoringWizard getRefactoringWizard(IIpsRefactoring refactoring) {
        return null;
    }

    @Override
    public Object execute(ExecutionEvent event) {
        System.out.println("start wizard");
        return null;
    }

    @Override
    protected IIpsRefactoring getRefactoring(Set<IIpsElement> selectedIpsElements) {
        // TODO Auto-generated method stub
        return null;
    }

}
