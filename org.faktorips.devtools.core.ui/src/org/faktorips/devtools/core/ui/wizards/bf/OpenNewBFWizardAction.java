/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.bf;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;

// TODO has to be registered
public class OpenNewBFWizardAction extends OpenNewWizardAction {

    @Override
    public INewWizard createWizard() {
        return new NewBFWizard();
    }

    @Override
    public void dispose() {
        // nothing to do
    }

}
