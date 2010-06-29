/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
