/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        this.table = viewer.getTable();
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
