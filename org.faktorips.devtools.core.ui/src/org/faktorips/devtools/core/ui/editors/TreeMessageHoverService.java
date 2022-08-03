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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Class to show hovers for trees.
 */
public abstract class TreeMessageHoverService extends MessageHoverService {

    private Tree tree;

    public TreeMessageHoverService(TreeViewer viewer) {
        super(viewer.getTree());
        tree = viewer.getTree();
    }

    @Override
    public Object getElementAt(Point point) {
        TreeItem item = tree.getItem(point);
        if (item == null) {
            return null;
        }
        return item.getData();
    }

    @Override
    public Rectangle getBoundsAt(Point point) {
        TreeItem item = tree.getItem(point);
        if (item == null) {
            return null;
        }
        return item.getBounds(0);
    }
}
