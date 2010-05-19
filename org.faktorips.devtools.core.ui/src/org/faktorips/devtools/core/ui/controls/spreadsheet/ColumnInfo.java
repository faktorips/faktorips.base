/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.spreadsheet;

import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 *
 */
public abstract class ColumnInfo {

    public String columnName;
    public int style = SWT.LEFT;
    public int initialWidth = 100;
    public boolean modifiable = false;
    public Comparator<Object> comparator = DEFAULT_COMPARATOR;

    /**
     * 
     */
    public ColumnInfo(String columnName, int style, int initialWidth, boolean modifiable) {
        this.columnName = columnName;
        this.style = style;
        this.initialWidth = initialWidth;
        this.modifiable = modifiable;
    }

    public abstract Object getValue(Object rowElement);

    public abstract String getText(Object rowElement);

    public abstract Image getImage(Object rowElement);

    public abstract void setValue(Object rowElement, Object newValue);

    public abstract EditField createEditField(Table table);

    private final static ToStringComparator DEFAULT_COMPARATOR = new ToStringComparator();

    private static class ToStringComparator implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            return o1.toString().compareTo(o2.toString());
        }

    }

}
