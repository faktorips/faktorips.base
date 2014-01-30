/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.faktorips.devtools.core.model.tablecontents.IRow;

public class TableSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof IRow && e2 instanceof IRow) {
            return ((IRow)e1).getRowNumber() - ((IRow)e2).getRowNumber();
        }
        return 0;
    }
}
