/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;

public class TableContentsContentProvider implements ILazyContentProvider {

    private TableViewer viewer;
    private Object[] elements;

    public TableContentsContentProvider(TableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof ITableContents) {
            ITableContents table = (ITableContents)newInput;
            elements = table.getTableRows().getRows();
        } else {
            elements = new ITableContents[0];
        }
    }

    @Override
    public void updateElement(int index) {
        viewer.replace(elements[index], index);
    }
}
