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
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsPullUpRefactoringWizard;
import org.faktorips.devtools.core.ui.wizards.refactor.IpsRefactoringWizard;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

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
    protected IpsRefactoringWizard getRefactoringWizard(IIpsRefactoring refactoring) {
        return new IpsPullUpRefactoringWizard(refactoring);
    }

    @Override
    protected IIpsRefactoring getRefactoring(Set<IIpsElement> selectedIpsElements) {
        return IpsPlugin.getIpsRefactoringFactory().createPullUpRefactoring(
                (IIpsObjectPart)selectedIpsElements.toArray(new IIpsElement[selectedIpsElements.size()])[0]);
    }

}
