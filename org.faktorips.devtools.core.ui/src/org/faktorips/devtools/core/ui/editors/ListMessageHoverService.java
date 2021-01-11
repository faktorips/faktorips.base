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
