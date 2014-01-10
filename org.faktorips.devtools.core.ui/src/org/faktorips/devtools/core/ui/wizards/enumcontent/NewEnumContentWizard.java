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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.wizards.AbstractIpsObjectNewWizardPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;

/**
 * A wizard responsible for the creation of a new <tt>IEnumContent</tt>.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumContent
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class NewEnumContentWizard extends NewIpsObjectWizard {

    @Override
    protected AbstractIpsObjectNewWizardPage createFirstPage(IStructuredSelection selection) throws Exception {
        return new EnumContentPage(selection);
    }

}
