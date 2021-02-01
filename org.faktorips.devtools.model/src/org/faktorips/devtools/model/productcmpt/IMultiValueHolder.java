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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.ComparatorUtils;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.util.collections.ListComparator;

/**
 * A multi value holder used for multi value attributes.
 * <p>
 * This holder just contains a list of {@link ISingleValueHolder}. The list validation and XML
 * handling is delegated to the internal string value holders.
 * 
 * @since 3.7
 * @author dirmeier
 */
public interface IMultiValueHolder extends IValueHolder<List<ISingleValueHolder>> {

    public static final String SEPARATOR = "|"; //$NON-NLS-1$

    public static final String XML_TYPE_NAME = "MultiValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "MULTIVALUEHOLDER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there an error in any value of this multi value
     * holder
     */
    public static final String MSGCODE_CONTAINS_INVALID_VALUE = MSGCODE_PREFIX + "ContainsInvalidValue"; //$NON-NLS-1$
    /**
     * Validation message code to indicate that this {@link IMultiValueHolder value holder's} values
     * are not unique. At least one value has a duplicate.
     */
    public static final String MSGCODE_CONTAINS_DUPLICATE_VALUE = MSGCODE_PREFIX + "ContainsDuplicateValue"; //$NON-NLS-1$

    @Override
    public default List<IValue<?>> getValueList() {
        return getValue().stream().map(ISingleValueHolder::getValue).collect(Collectors.toList());
    }

    @Override
    public default int compareTo(IValueHolder<List<ISingleValueHolder>> o) {
        if (o == null) {
            return 1;
        } else {
            @SuppressWarnings("unchecked")
            Comparator<ISingleValueHolder> naturalComparator = ComparatorUtils.naturalComparator();
            return ListComparator.listComparator(naturalComparator).compare(getValue(), o.getValue());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a string representation of the list of values. For example
     * <code>[value1, value2, value3]
     */
    @Override
    public default String getStringValue() {
        List<String> stringList = new ArrayList<String>();
        for (ISingleValueHolder holder : getValue()) {
            stringList.add(holder.getStringValue());
        }
        return stringList.toString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * IMultiValueHolder Is never null, because the list can be empty but never null.
     */
    @Override
    public default boolean isNullValue() {
        return false;
    }

    @Override
    public default boolean isMultiValue() {
        return true;
    }

}
