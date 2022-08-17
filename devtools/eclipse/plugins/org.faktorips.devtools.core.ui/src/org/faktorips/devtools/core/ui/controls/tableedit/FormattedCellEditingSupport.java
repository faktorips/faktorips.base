/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.ColumnViewer;
import org.faktorips.devtools.core.ui.table.CellTrackingEditingSupport;

public abstract class FormattedCellEditingSupport<T, V> extends CellTrackingEditingSupport<T> {

    public static final EditCondition DEFAULT_EDIT_CONDITION = () -> true;

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

    public interface EditCondition {

        boolean isEditable();

    }

}
