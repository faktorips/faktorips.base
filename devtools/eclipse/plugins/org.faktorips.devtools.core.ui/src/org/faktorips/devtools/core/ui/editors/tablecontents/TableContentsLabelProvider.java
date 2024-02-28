/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.tablecontents.IRow;
import org.faktorips.runtime.MessageList;

/**
 * LabelProvider for the TableViewer in the TableContents editor. Supports errormarkers for errenous
 * values and the null-representation string for table cells.
 *
 * @author Stefan Widmaier
 */
public class TableContentsLabelProvider implements ITableLabelProvider {

    private ValueDatatype[] datatypes;

    /**
     * Returns an error-icon if the given element has an error at the given columnIndex. Returns
     * <code>null</code> otherwise. {@inheritDoc}
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof IRow) {
            if (hasRowErrorsAt((IRow)element, columnIndex)) {
                return IpsUIPlugin.getImageHandling().getImage(OverlayIcons.ERROR_OVR_DESC);
            }
        }
        return null;
    }

    void setValueDatatypes(ValueDatatype[] datatypes) {
        this.datatypes = datatypes;
    }

    /**
     * Returns <code>true</code> if the given row validation detects an error at the given
     * columnIndex, <code>false</code> otherwise.
     */
    private boolean hasRowErrorsAt(IRow row, int columnIndex) {
        try {
            MessageList messageList = row.validate(row.getIpsProject());
            messageList = messageList.getMessagesFor(row, IRow.PROPERTY_VALUE, columnIndex);
            return !messageList.isEmpty();
        } catch (IpsException e) {
            IpsPlugin.log(e);
        }
        return false;
    }

    /**
     * Supports null-representation strings. If the value retrieved from the given element is
     * <code>null</code> the null-representation string is returned instead.
     * <p>
     * Returns <code>null</code> if the given element is not an <code>IRow</code>. {@inheritDoc}
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof IRow row) {
            if (row.getTableContents().getNumOfColumns() == 0) {
                return null;
            }
            String value = row.getValue(columnIndex);
            return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(datatypes[columnIndex], value);
        }
        return null;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        // Nothing to do
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // Nothing to do
    }

    void setValueDatatypesCount(int n) {
        datatypes = new ValueDatatype[n];
    }

    void setValueDatatype(int i, ValueDatatype dataType) {
        datatypes[i] = dataType;
    }

}
