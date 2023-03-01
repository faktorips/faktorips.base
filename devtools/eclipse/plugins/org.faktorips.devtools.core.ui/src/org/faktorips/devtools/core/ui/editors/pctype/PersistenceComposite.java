/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.model.ipsobject.IIpsObject;

/**
 * Extends the super type with the ability to view tabular data.
 * 
 * @author Roman Grutza
 */
public abstract class PersistenceComposite extends IpsPartsComposite {

    public PersistenceComposite(IIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
        super(ipsObject, parent, null, toolkit);
    }

    /**
     * Returns the names of the columns.
     */
    public abstract String[] getColumnHeaders();

    /**
     * This class redefines the creation of the viewer to apply its own column headers and
     * content/label providers.
     */
    @Override
    protected ContentViewer createViewer(Composite parent, UIToolkit toolkit) {
        ContentViewer viewer = super.createViewer(parent, toolkit);
        if (viewer instanceof TableViewer tableViewer) {
            ILabelProvider labelProvider = createLabelProvider();
            tableViewer.setLabelProvider(labelProvider);
            IContentProvider contentProvider = createContentProvider();
            tableViewer.setContentProvider(contentProvider);
            tableViewer.setInput(getIpsObject());

            Table table = tableViewer.getTable();
            table.setHeaderVisible(true);
            table.setLinesVisible(true);

            TableColumnLayout layout = new TableColumnLayout();
            // FIXME: this pushes the button column out of the visible area!
            table.getParent().setLayout(layout);

            String[] columnHeaders = getColumnHeaders();
            for (String columnHeader : columnHeaders) {
                TableColumn column = new TableColumn(table, SWT.NONE);
                column.setText(columnHeader);

                layout.setColumnData(column, new ColumnWeightData((int)(100 * (1.0f / columnHeaders.length))));
            }
        }

        viewer.refresh();
        return viewer;
    }

    @Override
    public abstract ILabelProvider createLabelProvider();
}
