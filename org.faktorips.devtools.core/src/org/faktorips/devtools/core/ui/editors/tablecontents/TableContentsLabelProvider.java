/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * LabelProvider for the TableViewer in the TableContents editor. Supports errormarkers for errenous
 * values and the null-representation string in table cells.
 * 
 * @author Stefan Widmaier
 */
public class TableContentsLabelProvider implements ITableLabelProvider {

    /**
     * Returns an error-icon if the given element has an error at the given columnIndex.
     * Returns <code>null</code> otherwise.
     * {@inheritDoc}
     */
    public Image getColumnImage(Object element, int columnIndex) {
        if(element instanceof IRow){
            if(hasRowWarningsAt((IRow) element, columnIndex)){
                return IpsPlugin.getDefault().getImage("ovr16/error_co.gif");                
            }
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the given row has an error at the given columnIndex
     * <code>false</code> otherwise.
     * 
     * @param row
     * @param columnIndex
     * @return
     */
    private boolean hasRowWarningsAt(IRow row, int columnIndex) {
        try {
            MessageList messageList = row.getTableContents().validate();
            return messageList.getMessagesFor(row).getSeverity()==Message.ERROR;
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return false;
    }

    /**
     * Supports null-representation strings. If the value retrieved from the given element 
     * is <code>null</code> the null-representation string is returned instead.
     * <p>
     * Returns <code>null</code> if the given element is not an <code>IRow</code>.
     * {@inheritDoc}
     */
    public String getColumnText(Object element, int columnIndex) {
        if(element instanceof IRow){
            String value= ((IRow) element).getValue(columnIndex);
            if (value==null) {
                value= IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            }
            return value;
        }
        return null;
    }

    /**
     * Empty implementation.
     * {@inheritDoc}
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /**
     * Empty implementation.
     * {@inheritDoc}
     */
    public void dispose() {
    }

    /**
     * Empty implementation.
     * {@inheritDoc}
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * Empty implementation.
     * {@inheritDoc}
     */
    public void removeListener(ILabelProviderListener listener) {
    }

}
