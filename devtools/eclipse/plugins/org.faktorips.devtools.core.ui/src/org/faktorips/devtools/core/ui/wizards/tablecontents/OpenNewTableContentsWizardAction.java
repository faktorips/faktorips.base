/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;

/**
 *
 */
public class OpenNewTableContentsWizardAction extends OpenNewWizardAction {

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public INewWizard createWizard() {
        return new NewTableContentsWizard();
    }

    @Override
    public void dispose() {
        // nothing to do
    }

}
