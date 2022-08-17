/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;

/**
 * An <code>IEnumValueSet</code> represents a value set of discrete values where each value has to
 * be explicitly defined.
 * 
 * @author Thorsten Guenther
 */
public interface IEnumValueSet extends IValueSet {

    String PROPERTY_VALUES = "values"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "ENUMVALUESET-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value in this value set is duplicate.
     */
    String MSGCODE_DUPLICATE_VALUE = MSGCODE_PREFIX + "DuplicateValue"; //$NON-NLS-1$

    /**
     * Returns an array of all values in the set.
     */
    String[] getValues();

    /**
     * Returns a List of all positions/indexes the given value occurs in the set.
     */
    List<Integer> getPositions(String value);

    /**
     * Adds the value to the set. Duplicate values are allowed but will lead to an error message if
     * validated.
     */
    void addValue(String val);

    /**
     * Adds all values in the list to this set. Duplicate values are allowed but will lead to an
     * error message if validated.
     */
    void addValues(List<String> values);

    /**
     * Removes the value at the given index from the value set.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    void removeValue(int index);

    /**
     * Removes the given value. If the value is not contained, nothing happens.
     */
    void removeValue(String string);

    /**
     * Removes the given values from this value set. If a value is not contained in this value set,
     * it will be ignored.
     */
    void removeValues(List<String> values);

    /**
     * Retrieves the value at the given index.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    String getValue(int index);

    /**
     * Sets the value at the given index.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    void setValue(int index, String value);

    /**
     * Returns the number of values in the set.
     */
    int size();

    /**
     * Returns an array with all values contained in the given other value set but not in this one.
     * 
     * @param otherSet The set to take the values from to find in this one.
     */
    String[] getValuesNotContained(IEnumValueSet otherSet);

    /**
     * Adds all values from the given {@link EnumDatatype} to the value set.
     */
    void addValuesFromDatatype(EnumDatatype datatype);

    /**
     * Checks if the value at the specified position is a valid. Note that if this value is a
     * duplicate, the method returns a list with a message that has the message code
     * {@link #MSGCODE_DUPLICATE_VALUE}. The messager's invalid object properties specify this value
     * set as the invalid object, the property {@link #PROPERTY_VALUES} as invalid property and the
     * index as the position holding the invalid value. So the other duplicates are NOT returned as
     * invalid!
     */
    MessageList validateValue(int index, IIpsProject ipsProject) throws IpsException;

    /**
     * Getting a copy of the list of values
     * 
     * @return A copy of the values list
     */
    List<String> getValuesAsList();

    /**
     * Move the parts on the specified indexes up or down
     * 
     * @param indexes the indexes of the elements that should be moved
     * @param up true to move the elements up, false for down
     * 
     */
    void move(List<Integer> indexes, boolean up);

    /**
     * Move all selected elements to a new index. The move does not maintain gaps in the selection.
     * Moved items will be adjacent to one another.
     * 
     * @param indices The selected items' indices
     * @param targetIndex New position for the moved elements
     * @param insertBelow True if the selection is dropped on the lower edge of the target
     */
    void move(List<Integer> indices, int targetIndex, boolean insertBelow);

}
