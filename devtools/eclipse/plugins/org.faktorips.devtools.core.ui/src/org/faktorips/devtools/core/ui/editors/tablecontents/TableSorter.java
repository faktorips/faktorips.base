/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.faktorips.devtools.model.tablecontents.IRow;

public class TableSorter extends ViewerComparator {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof IRow && e2 instanceof IRow) {
            return ((IRow)e1).getRowNumber() - ((IRow)e2).getRowNumber();
        }
        return 0;
    }
}
