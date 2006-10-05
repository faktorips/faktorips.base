/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
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

    /**
     * {@inheritDoc}
     */
    public Object getElementAt(Point point) {
        int index = getIndexFor(point);
        if (index == -1) {
            return null;
        }

        return list.getItem(index);
    }
    
    /**
     * {@inheritDoc}
     */
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
            index ++;
        }
        return index;
    }
}
