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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.ui.table.ValueCellModifier;

/**
 * A CellModifyer that can modify and retrieve values from <code>IRow</code> objects (<code>ITableContents</code>).
 * 
 * @author Stefan Widmaier
 */
public class TableContentsCellModifier extends ValueCellModifier {
    
    private TableViewer tableViewer;
    private ContentPage page;
    
    public TableContentsCellModifier(TableViewer tableViewer, ContentPage page){
        this.tableViewer= tableViewer;
        this.page = page;
    }
    
    /**
     * Returns the page's data changeable property. 
     * {@inheritDoc}
     */
    public boolean canModify(Object element, String property) {
        return page.isDataChangeable();
    }
    
    /**
     * Returns the value of the property of the given element. May return null.
     * {@inheritDoc}
     */
    protected String getValueInternal(Object element, String property) {
        int columnIndex= getColumnIndexForProperty(property);
        if(columnIndex>=0){
            if(element instanceof IRow){
                IRow row= (IRow)element;
                return row.getValue(columnIndex);
            }            
        }
        return null;
    }

    /**
     * Refreshes the given element in the table after the modification.
     * {@inheritDoc}
     */
    protected void modifyInternal(Object element, String property, Object value) {
        int columnIndex= getColumnIndexForProperty(property);
        if(columnIndex>=0){
            IRow row= null;
            if(element instanceof TableItem){
                row= (IRow) ((TableItem) element).getData();
            }else if(element instanceof IRow){
                row= (IRow) element;
            }
            if(row!=null){
                row.setValue(columnIndex, (String)value);
                tableViewer.refresh(row);
            }
        }
    }

    private int getColumnIndexForProperty(String columnProperty){
        List columnProperties = Arrays.asList(tableViewer.getColumnProperties());
        return columnProperties.indexOf(columnProperty);
    }
    
}
