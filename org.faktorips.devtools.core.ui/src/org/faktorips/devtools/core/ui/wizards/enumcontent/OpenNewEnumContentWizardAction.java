/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.enumcontent;

import org.eclipse.ui.INewWizard;
import org.faktorips.devtools.core.ui.wizards.OpenNewWizardAction;

/**
 * This action is responsible for opening a <tt>NewEnumContentWizard</tt>.
 * 
 * @see NewEnumContentWizard
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class OpenNewEnumContentWizardAction extends OpenNewWizardAction {

    @Override
    public INewWizard createWizard() {
        return new NewEnumContentWizard();
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

}
