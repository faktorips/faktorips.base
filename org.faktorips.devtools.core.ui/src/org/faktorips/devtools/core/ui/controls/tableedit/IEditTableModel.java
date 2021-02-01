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

import java.util.List;

import org.faktorips.util.message.MessageList;

/**
 * The model for an {@link EditTableControlViewer}. Provides methods for adding, removing and
 * swapping elements. Those methods are called to add, remove or reorder the table model's elements,
 * e.g. when the user clicks the {@link EditTableControlViewer}'s buttons.
 * 
 * @see EditTableControlViewer
 * @since 3.7
 * 
 * @author Stefan Widmaier
 */
public interface IEditTableModel<T> {

    public void swapElements(int index1, int index2);

    public T addElement();

    public void removeElement(int index);

    public List<T> getElements();

    /**
     * Validates the given element. Returns a message list containing all errors and warnings.
     * 
     * @param elementToValidate the element to be validated
     */
    public MessageList validate(T elementToValidate);
}
