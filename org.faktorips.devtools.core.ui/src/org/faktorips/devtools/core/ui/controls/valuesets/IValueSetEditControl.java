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

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

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
    public ValueSetType getValueSetType();

    /**
     * Sets the new value set being edited in the control.
     * 
     * @param newSet The new set of values.
     * @param valueDatatype The datatype the values in the set are instances of.
     * 
     * @throws NullPointerException if newSet is <code>null</code>.
     */
    public void setValueSet(IValueSet newSet, ValueDatatype valueDatatype);

    /**
     * Returns the value set being edited in the control.
     * 
     * @return
     */
    public IValueSet getValueSet();

    /**
     * Returns <code>true</code> if this control can be used to edit the given value set using the
     * given value datatype. Returns <code>false</code> otherwise.
     * 
     * @param valueSet The value set to test.
     * @param valueDatatype The datatype the values in the set are instances of.
     */
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype);

}
