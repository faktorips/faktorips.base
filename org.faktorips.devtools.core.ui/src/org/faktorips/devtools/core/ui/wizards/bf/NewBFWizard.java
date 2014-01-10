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

package org.faktorips.devtools.core.ui.wizards.bf;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard;

/**
 * A wizard responsible for the creation of a new business function.
 * 
 * @author Peter Erzberger
 */
public class NewBFWizard extends NewIpsObjectWizard {

    @Override
    protected IpsObjectPage createFirstPage(IStructuredSelection selection) throws Exception {
        return new BFPage(selection);
    }

}
