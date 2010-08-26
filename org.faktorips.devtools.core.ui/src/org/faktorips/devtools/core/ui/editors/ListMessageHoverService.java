/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.List;

/**
 * Class to show hovers for messages for lists.
 */
public abstract class ListMessageHoverService extends MessageHoverService {

    private List list;

    public ListMessageHoverService(List list) {
        super(list);
        this.list = list;
    }

    @Override
    public Object getElementAt(Point point) {
        int index = getIndexFor(point);
        if (index == -1) {
            return null;
        }

        return list.getItem(index);
    }

    @Override
    public Rectangle getBoundsAt(Point point) {
        int index = getIndexFor(point);
        if (index == -1) {
            return null;
        }
        int itemHeight = list.getItemHeight();
        int topIndex = list.getTopIndex();
        int downShift = (index - topIndex) * itemHeight;
        Rectangle result = list.getClientArea();
        result.y += downShift;
        result.height = itemHeight;
        return result;
    }

    private int getIndexFor(Point point) {
        int index = list.getTopIndex();
        int height = list.getItemHeight();
        for (int i = 0; i * height + height < point.y; i++) {
            index++;
        }
        return index;
    }
}
