/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.util.message.MessageList;

/**
 * A ValueSet is the specification of a set of values. It is asumed that all values in a ValueSet are of the same
 * datatype.
 * <p>
 * Values in the set are represented by strings so that we don't have to deal with type conversion
 * when the datatype changes. E.g. If an attributes datatype is changed by the user from Decimal to Money,
 * lower bound and upper bound from a range value set become invalid (if they were valid before) but the 
 * string values remain. The user can switch back the datatype to Decimal and the range is valid again. 
 * This works also when the attribute's datatype is unkown.
 * 
 * @author Thorsten Guenther
 * @author Jan Ortmann
 */
public interface IValueSet extends IIpsObjectPart {
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "VALUESET-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not contained in the value set.
     */
    public final static String MSGCODE_VALUE_NOT_CONTAINED = MSGCODE_PREFIX + "ValueNotContained"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value-subset is not of the correct type.
     */
    public final static String MSGCODE_TYPE_OF_VALUESET_NOT_MATCHING = MSGCODE_PREFIX + "TypeOfValueSetNotMatching"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value could not be parsed.
     */
    public final static String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX + "ValueNotParsable"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set. 
     */
    public final static String MSGCODE_NOT_COMPARABLE = MSGCODE_PREFIX + "NotComparable"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the lower bound of the subset is less than the lower
     * bound of this value set. 
     */
    public final static String MSGCODE_UNKNOWN_DATATYPE = MSGCODE_PREFIX + "UnknownDatatype"; //$NON-NLS-1$
    
    /**
     * Returns the type of the value set.
     */
    public ValueSetType getValueSetType();
    
    /**
     * Returns <code>true</code> if the value set contains the indicated value, otherwise <code>false</code>.
     * 
     * @param value The value to check.
     * @param datatype The datatype to parse the string values to 'real' values.
     * 
     * @throws NullPointerException if datatype is <code>null</code>. 
     */
    public boolean containsValue(String value, ValueDatatype datatype);

    /**
     * Returns <code>true</code> it the value set contains the indicated value, otherwise <code>false</code>.
     * A message is stored in the message list, if the value set doesn't contain the indicated value.
     * 
     * @param value The value to check.
     * @param datatype The datatype to parse the string values to 'real' values.
     * @param list The list to add messages, if any. Can be null if no messages are needed.
     * @param invalidObject The object the message refers to. Ignored if <code>list</code>
     *                      is <code>null</code>. Can be <code>null</code> itself. 
     * @param invalidProperty The property of the object the message refers to. Ignored if 
     *                        <code>list</code> or <code>invalidObject</code> is <code>null</code>.
     *                        Can be <code>null</code> itself.
     * @throws NullPointerException if datatype is <code>null</code>. 
     */
    public boolean containsValue(String value, ValueDatatype datatype,
			MessageList list, Object invalidObject, String invalidProperty);
    
    /**
     * Returns <code>true</code> if this valueset contains the other valueset, otherwise <code>false</code>.
     * 
     * @param subset The valueset to check.
     * @param datatype The datatype to parse the valueset's values to 'real' values.
     * 
     * @throws NullPointerException if subset or datatype is <code>null</code>. 
     */
    public boolean containsValueSet(IValueSet subset, ValueDatatype datatype);
    
    /**
     * Returns <code>true</code> if this valueset contains the other valueset, otherwise <code>false</code>.
     * 
     * @param subset The valueset to check.
     * @param datatype The datatype to parse the valueset's values to 'real' values.
     * @param list The list to which a message is added in case the given valueset is not a subset of this valueset. 
     * @param invalidObject The object the message refers to. Ignored if <code>list</code>
     *                      is <code>null</code>. Can be <code>null</code> itself. 
     * @param invalidProperty The property of the object the message refers to. Ignored if 
     *                        <code>list</code> or <code>invalidObject</code> is <code>null</code>.
     *                        Can be <code>null</code> itself.
     * 
     * @throws NullPointerException if subset or datatype is <code>null</code>. 
     */
    public boolean containsValueSet(IValueSet subset, ValueDatatype datatype, 
    		MessageList list, Object invalidObject, String invalidProperty);

    /**
     * Validates the value set.
     * 
     * @param datatype The value datatype to parse the set's string values to 'real' values.
     * @param list Message collection paramter.
     */
    public void validate(ValueDatatype datatype, MessageList list);
    
    /**
     * Creates a copy of this value set (type and values, parent and id are set
     * to the given values).
     */
    public IValueSet copy(IIpsObjectPart parent, int id);

    /**
     * Copy all values (not the parent or the id) of the given target to this value set.
     * @throws IllegalArgumentException if the given target is not the same type
     * this method is invoked.
     */
    public void setValuesOf(IValueSet source);
    
    /**
     * Returns the unqulified, human readable representation of this value set.
     */
    public String toShortString();
    
}
