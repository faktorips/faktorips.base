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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.util.message.MessageList;

/**
 * LabelProvider for the TableViewer in the TableContents editor. Supports errormarkers for errenous
 * values and the null-representation string for table cells.
 * 
 * @author Stefan Widmaier
 */
public class TableContentsLabelProvider implements ITableLabelProvider {

    private Map mappedEditor = new HashMap();
    
    /**
     * The image indicating an error in a table cell.
     */
    private Image errorImage= IpsPlugin.getDefault().getImage("ovr16/error_co.gif"); //$NON-NLS-1$
    
    /**
     * Returns an error-icon if the given element has an error at the given columnIndex.
     * Returns <code>null</code> otherwise.
     * {@inheritDoc}
     */
    public Image getColumnImage(Object element, int columnIndex) {
        if(element instanceof IRow){
            if(hasRowErrorsAt((IRow) element, columnIndex)){
                return errorImage;
            }
        }
        return null;
    }

    /**
     * Adds an cell editor for which the mapping between an id and a displayed text is supported.
     */
    public void addMappedEditor(int columnIdx, TableCellEditor cellEditor){
        mappedEditor.put(""+columnIdx, cellEditor); //$NON-NLS-1$
    }
    
    /**
     * Returns <code>true</code> if the given row validation detects an error at the given columnIndex,
     * <code>false</code> otherwise.
     * 
     * @param row
     * @param columnIndex
     * @return
     */
    private boolean hasRowErrorsAt(IRow row, int columnIndex) {
        try {
            MessageList messageList = row.validate();
            messageList= messageList.getMessagesFor(row, IRow.PROPERTY_VALUE, columnIndex);
            return !messageList.isEmpty();
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
            IRow row = (IRow)element;
            if (row.getTableContents().getNumOfColumns()==0) {
                return null;
            }
            String value= row.getValue(columnIndex);
            if (value==null) {
                value= IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            }

            if (mappedEditor.containsKey(""+columnIndex)){ //$NON-NLS-1$
                // the value inside the cell will be mapped to the specified format as defined in the editor 
                TableCellEditor cellEditor = (TableCellEditor)mappedEditor.get(""+columnIndex); //$NON-NLS-1$
                EnumDatatypeField enumDatatypeField = (EnumDatatypeField)cellEditor.getControl().getData();
                return enumDatatypeField.getValueName(value);
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
