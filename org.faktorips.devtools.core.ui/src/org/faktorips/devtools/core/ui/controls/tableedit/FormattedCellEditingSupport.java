/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.ColumnViewer;
import org.faktorips.devtools.core.ui.table.CellTrackingEditingSupport;

public abstract class FormattedCellEditingSupport<T, V> extends CellTrackingEditingSupport<T> {

    public static final EditCondition DEFAULT_EDIT_CONDITION = new EditCondition() {

        @Override
        public boolean isEditable() {
            return true;
        }
    };

    private final IElementModifier<T, V> elementModifier;

    private final EditCondition editCondition;

    public FormattedCellEditingSupport(ColumnViewer viewer, IElementModifier<T, V> elementModifier) {
        this(viewer, elementModifier, DEFAULT_EDIT_CONDITION);
    }

    public FormattedCellEditingSupport(ColumnViewer viewer, IElementModifier<T, V> elementModifier,
            EditCondition editCondition) {
        super(viewer);
        this.elementModifier = elementModifier;
        this.editCondition = editCondition;
    }

    @Override
    protected boolean canEdit(Object element) {
        return editCondition.isEditable();
    }

    @Override
    protected V getValue(Object element) {
        @SuppressWarnings("unchecked")
        // Object is required by super class, but we know that we can only get objects of type T
        // here, because they are the only ones we can put into the cells of a table editor.
        T castElement = (T)element;
        return elementModifier.getValue(castElement);
    }

    @Override
    protected void setValue(Object element, Object value) {
        @SuppressWarnings("unchecked")
        // Object is required by super class, but we know that we can only get objects of type T
        // here, because they are the only ones we can put into the cells of a table editor.
        T castElement = (T)element;

        @SuppressWarnings("unchecked")
        // The same is true for values.
        V castValue = (V)value;

        elementModifier.setValue(castElement, castValue);
        getViewer().refresh();
    }

    /**
     * Returns the string representing the given element. The string is formated depending on the
     * datatype and the current locale.
     * 
     * @param element the element to return a formatted string for
     */
    public abstract String getFormattedValue(T element);

    public static interface EditCondition {

        boolean isEditable();

    }

}
