/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.testcasetype;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;

/**
 * @author Joerg Ortmann
 */
public class OpenNewTestCaseTypeWizardAction extends OpenNewWizardAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public INewWizard createWizard() {
        return new NewTestCaseTypeWizard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        // nothing to do
    }
}
