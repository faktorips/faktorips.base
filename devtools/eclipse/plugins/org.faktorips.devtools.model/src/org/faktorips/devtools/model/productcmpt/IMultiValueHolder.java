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

    String SEPARATOR = "|"; //$NON-NLS-1$

    String XML_TYPE_NAME = "MultiValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    String MSGCODE_PREFIX = "MULTIVALUEHOLDER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there an error in any value of this multi value
     * holder
     */
    String MSGCODE_CONTAINS_INVALID_VALUE = MSGCODE_PREFIX + "ContainsInvalidValue"; //$NON-NLS-1$
    /**
     * Validation message code to indicate that this {@link IMultiValueHolder value holder's} values
     * are not unique. At least one value has a duplicate.
     */
    String MSGCODE_CONTAINS_DUPLICATE_VALUE = MSGCODE_PREFIX + "ContainsDuplicateValue"; //$NON-NLS-1$

    @Override
    default List<IValue<?>> getValueList() {
        return getValue().stream().map(ISingleValueHolder::getValue).collect(Collectors.toList());
    }

    @Override
    default int compareTo(IValueHolder<List<ISingleValueHolder>> o) {
        if (o == null) {
            return 1;
        } else {
            Comparator<ISingleValueHolder> naturalComparator = Comparator.naturalOrder();
            return ListComparator.listComparator(naturalComparator).compare(getValue(), o.getValue());
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a string representation of the list of values. For example
     * <code>[value1, value2, value3]</code>.
     */
    @Override
    default String getStringValue() {
        List<String> stringList = new ArrayList<>();
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
    default boolean isNullValue() {
        return false;
    }

    @Override
    default boolean isMultiValue() {
        return true;
    }

}
