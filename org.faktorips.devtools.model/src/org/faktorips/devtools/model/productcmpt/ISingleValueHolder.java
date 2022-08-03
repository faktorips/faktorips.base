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
import java.util.List;

import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueType;

/**
 * The default value holder for attribute values holding a single value. The StringValue does not
 * mean that the element in the model needs to be of type String but that it is persisted as a
 * String value. It can also be an InternationalStringValue.
 * 
 * @since 3.7
 * @author dirmeier
 */
public interface ISingleValueHolder extends IValueHolder<IValue<?>> {

    String DEFAULT_XML_TYPE_NAME = "SingleValue"; //$NON-NLS-1$

    @Override
    default List<IValue<?>> getValueList() {
        ArrayList<IValue<?>> result = new ArrayList<>(1);
        result.add(getValue());
        return result;
    }

    @Override
    default ValueType getValueType() {
        return ValueType.getValueType(getValue());
    }

    @Override
    default boolean isMultiValue() {
        return false;
    }
}
