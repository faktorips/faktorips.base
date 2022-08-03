/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * Common base interface for all controls/composite allowing to edit a single value set of a given
 * value set type.
 * 
 * @author Jan Ortmann
 */
public interface IValueSetEditControl {

    /**
     * Returns the type of value set this control can edit.
     */
    ValueSetType getValueSetType();

    /**
     * Sets the new value set being edited in the control.
     * 
     * @param newSet The new set of values.
     * @param valueDatatype The datatype the values in the set are instances of.
     * 
     * @throws NullPointerException if newSet is <code>null</code>.
     */
    void setValueSet(IValueSet newSet, ValueDatatype valueDatatype);

    /**
     * Returns the value set being edited in the control.
     */
    IValueSet getValueSet();

    /**
     * Returns <code>true</code> if this control can be used to edit the given value set using the
     * given value datatype. Returns <code>false</code> otherwise. Returns <code>false</code> if
     * <code>valueSet</code> or <code>valueDatatype</code> is <code>null</code>.
     * 
     * @param valueSet The value set to test.
     * @param valueDatatype The datatype the values in the set are instances of.
     */
    boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype);

    /**
     * Returns the composite this edit control uses to edit the value set. e.g. the
     * {@link RangeEditControl} returns itself but other implementations do not combine UI and model
     * in a single class.
     */
    Composite getComposite();

}
