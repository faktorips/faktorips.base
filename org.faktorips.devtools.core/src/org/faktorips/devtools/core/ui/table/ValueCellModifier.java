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
import org.faktorips.devtools.core.IpsPlugin;

/**
 * A cell modifier which supports null presentaion values.
 * 
 * @author Joerg Ortmann
 */
public abstract class ValueCellModifier implements ICellModifier {

    /**
     * {@inheritDoc}
     */
    public Object getValue(Object element, String property) {
        String value = getValueInternal(element, property);
        if (value==null) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    public void modify(Object element, String property, Object value) {
        if (IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(value)){
            value = null;
        }
        modifyInternal(element, property, value);
    }

    /**
     * Get value with null presentation. If the value is null then the null presentation will be
     * returned.
     * 
     * @see ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    protected abstract String getValueInternal(Object element, String property);

    /**
     * Modifier with null presentation. If the value is null then the null presentation will be
     * given.
     * 
     * @see ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    protected abstract void modifyInternal(Object element, String property, Object value);

}
