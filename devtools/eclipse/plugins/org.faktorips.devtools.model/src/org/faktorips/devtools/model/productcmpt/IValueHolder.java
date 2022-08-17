/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.util.List;

import org.faktorips.devtools.model.Validatable;
import org.faktorips.devtools.model.XmlSupport;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueType;

/**
 * This is the public interface for all value holders as they are used by {@link IAttributeValue}.
 * It is recommended to use the {@code AbstractValueHolder} class for implementing this interface.
 * <p>
 * A value holder is a child of an {@link IIpsObjectPart} for example an {@link IAttributeValue}.
 * Any changes within the value holder have to be propagated to this part.
 * 
 * @author dirmeier
 * @since 3.7
 */
public interface IValueHolder<T> extends XmlSupport, Validatable, Comparable<IValueHolder<T>> {

    String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Name of the XML attribute for the value holder type.
     */
    String XML_ATTRIBUTE_VALUE_TYPE = "valueType"; //$NON-NLS-1$

    /**
     * Returning the {@link IAttributeValue} of which this value holder is a child of. Every value
     * holder need to have a parent {@link IAttributeValue}. If anything within the value holder
     * changes, the change event is propagated to this part.
     * 
     * @return The parent {@link IIpsObjectPart}
     */
    IAttributeValue getParent();

    /**
     * Returning a string representation of the value.
     * 
     * @return a string representation of this part.
     */
    String getStringValue();

    /**
     * Returns the value of this value holder. The type of the value depends on the generic type T.
     * 
     * @return The current value stored in this value holder.
     */
    T getValue();

    /**
     * Setting a new value for this value holder. The type of the value must match the generic type
     * T.
     * <p>
     * Setting a new value have to perform a change event on the parent.
     * 
     * @param value The value that should be set as current value in this holder.
     */
    void setValue(T value);

    /**
     * Get the list of single values. If this {@link IValueHolder} is a {@link ISingleValueHolder}
     * this {@link List} always returns a list with the single {@link IValue}. If it is a
     * {@link IMultiValueHolder} the {@link List} contains all values;
     * 
     * @return The list of {@link IValue} of this {@link IValueHolder}
     */
    List<IValue<?>> getValueList();

    /**
     * Set the list of values to this {@link IValueHolder}. If this is a {@link ISingleValueHolder}
     * the list must contain at most one element, an empty list is treated as <code>null</code>
     * value. If this is a {@link IMultiValueHolder} all the values will be placed.
     * 
     * @param values the list of values, must have at most one value in case of
     *            {@link ISingleValueHolder}
     * 
     * @throws IllegalArgumentException if the list has more than one values but this is only
     *             a{@link ISingleValueHolder}
     */
    void setValueList(List<IValue<?>> values);

    /**
     * Returns <code>true</code>, if the value is <code>null</code> otherwise <code>false</code>. It
     * depends on the specific ValueHolder.
     * 
     * @return boolean <code>true</code> if the value is <code>null</code>
     */
    boolean isNullValue();

    /**
     * The ValueType describe the kind of value used in this value holder. The different kinds are
     * described in the {@link ValueType}. The reason for {@link ValueType} is to distinguish the
     * kind of {@link IValue}.
     */
    ValueType getValueType();

    /**
     * Basically there are two different kinds of value holder: multi value holder and single value
     * holder. This method returns <code>true</code> if this value holder is a multi value holder.
     * It does not say anything about the concrete implementation so do not use for instance-of
     * check!
     * 
     * @return Returns <code>true</code> if the value holder has multiple values
     */
    boolean isMultiValue();

    /**
     * Creates a new {@link IValueHolder} by copying this value holder. The new value holder gets
     * the specified parent.
     * 
     * @return A new value holder with the same content as this value holder
     */
    IValueHolder<?> copy(IAttributeValue parent);

    /**
     * Compares this {@link IValueHolder} with the given one, unwrapping delegating value holders.
     */
    boolean equalsValueHolder(IValueHolder<?> valueHolder);

}
