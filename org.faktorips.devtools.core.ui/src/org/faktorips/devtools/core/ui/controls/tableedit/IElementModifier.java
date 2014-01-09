/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel;

/**
 * Interface for classes that can access and modify a {@link MultiValueTableModel table model's}
 * elements.
 * 
 * @author Stefan Widmaier
 */
public interface IElementModifier<T, V> {
    /**
     * Returns the value for the given element.
     * 
     * @param element the value to access
     */
    public V getValue(T element);

    /**
     * Sets the given value as value for the given element.
     * 
     * @param element the value to modify
     */
    public void setValue(T element, V value);
}
