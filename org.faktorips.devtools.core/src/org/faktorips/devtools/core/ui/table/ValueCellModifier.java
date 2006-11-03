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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * A cell modifier which supports null-representaion values.
 * 
 * @author Joerg Ortmann
 */
public abstract class ValueCellModifier implements ICellModifier {

    /**
     * Get value with null-representation. If the value retrieved from the given element is
     * <code>null</code> the null-representation string is returned. 
     * {@inheritDoc}
     */
    public Object getValue(Object element, String property) {
        String value = getValueInternal(element, property);
        if (value==null) {
            value= IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return value;
    }
    
    /**
     * Modify with null-representation. If the given value is the null-representation string, the
     * value of the given element's property will be set to <code>null</code>.
     * <p>
     * For displaying the null-representation string correctly in the table (after editing) it is
     * necessary to make the <code>TableViewer</code>'s <code>ITableLabelProvider</code>
     * support null-representation strings. (The tableviewer reads the cells' values directly from
     * model-objects without using the getValue() method of the cellModifier. In this process a
     * <code>null</code> value is replaced by the empty string ("") for displaying it in the
     * table)
     * 
     * @see ITableLabelProvider#getColumnText(Object, int) {@inheritDoc}
     */
    public void modify(Object element, String property, Object value) {
        if (IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(value)){
            value = null;
        }
        modifyInternal(element, property, value);
    }

    /**
     * Modifies the given element and thereby sets the given property to the given value.
     * <p>
     * The given value may be <code>null</code>.
     * 
     * @see ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    protected abstract String getValueInternal(Object element, String property);

    /**
    /**
     * Returns the value of the property of the given element. May return null.
     * {@inheritDoc}
     * @see ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    protected abstract void modifyInternal(Object element, String property, Object value);

}
