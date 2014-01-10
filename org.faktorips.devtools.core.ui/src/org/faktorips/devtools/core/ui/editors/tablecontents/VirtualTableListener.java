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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;

public class VirtualTableListener implements Listener {

    private Table table;
    private ITableContents tableContents;

    /**
     * Creates a listener for virtual Tables.
     */
    public VirtualTableListener(Table table, ITableContents tableContents) {
        this.table = table;
        this.tableContents = tableContents;
    }

    /**
     * Sets the data of as uninitialized tableitem. This method is called when a new tableitem
     * becomes visible in the gui. {@inheritDoc}
     */
    @Override
    public void handleEvent(Event event) {
        TableItem item = (TableItem)event.item;
        // event.index (since eclipse3.2) not used in favour of backwardscompatability
        int index = table.indexOf(item);
        IRow requestedRow = ((ITableContentsGeneration)tableContents.getFirstGeneration()).getRow(index);
        item.setData(requestedRow);
    }

}
