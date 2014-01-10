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

package org.faktorips.devtools.core.ui.wizards.tablestructure;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

public class TableStructurePage extends IpsObjectPage {

    public TableStructurePage(IStructuredSelection selection) {
        super(IpsObjectType.TABLE_STRUCTURE, selection, Messages.TableStructurePage_title);
    }

}
