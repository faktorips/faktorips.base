/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.valueset;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

/**
 * An <tt>IEnumValueSet</tt> represents a value set of discrete values where each value has to be
 * explicitly defined.
 * 
 * @author Thorsten Guenther
 */
public interface IEnumValueSet extends IValueSet {

    public final static String PROPERTY_VALUES = "values"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "ENUMVALUESET-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value in this value set is duplicate.
     */
    public final static String MSGCODE_DUPLICATE_VALUE = MSGCODE_PREFIX + "DuplicateValue"; //$NON-NLS-1$

    /**
     * Returns an array of all values in the set.
     */
    public String[] getValues();

    /**
     * Returns a List of all positions/indexes the given value occurs in the set.
     */
    public List<Integer> getPositions(String value);

    /**
     * Adds the value to the set. Duplicate values are allowed but will lead to a message if
     * validated.
     */
    public void addValue(String val);

    /**
     * Removes the value at the given index from the value set.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    public void removeValue(int index);

    /**
     * Removes the given value. If the value is not contained, nothing happens.
     */
    public void removeValue(String string);

    /**
     * Retrieves the value at the given index.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    public String getValue(int index);

    /**
     * Sets the value at the given index.
     * 
     * @throws IndexOutOfBoundsException If the given index is out of bounds.
     */
    public void setValue(int index, String value);

    /**
     * Returns the number of values in the set.
     */
    public int size();

    /**
     * Returns an array with all values contained in the given other value set but not in this one.
     * 
     * @param otherSet The set to take the values from to find in this one.
     */
    public String[] getValuesNotContained(IEnumValueSet otherSet);

    /**
     * Adds all values from the given {@link EnumDatatype} to the value set.
     */
    public void addValuesFromDatatype(EnumDatatype datatype);

    /**
     * Checks if the value at the specified position is a valid. Note that if this value is a
     * duplicate, the method returns a list with a message that has the message code
     * {@link #MSGCODE_DUPLICATE_VALUE}. The messager's invalid object properties specify this value
     * set as the invalid object, the property {@link #PROPERTY_VALUES} as invalid property and the
     * index as the position holding the invalid value. So the other duplicates are NOT returned as
     * invalid!
     */
    public MessageList validateValue(int index, IIpsProject ipsProject) throws CoreException;

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

}
