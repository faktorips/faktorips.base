/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Class to show hovers for messages for tables.
 */
public abstract class TableMessageHoverService extends MessageHoverService {

    private TableViewer viewer;
    private Table table;

    public TableMessageHoverService(TableViewer viewer) {
        super(viewer.getTable());
        this.viewer = viewer;
        table = viewer.getTable();
    }

    @Override
    public Object getElementAt(Point point) {
        TableItem item = table.getItem(point);
        if (item == null) {
            return null;
        }
        return viewer.getElementAt(table.indexOf(item));
    }

    @Override
    public Rectangle getBoundsAt(Point point) {
        TableItem item = table.getItem(point);
        if (item == null) {
            return null;
        }
        return item.getBounds(0);
    }
}
