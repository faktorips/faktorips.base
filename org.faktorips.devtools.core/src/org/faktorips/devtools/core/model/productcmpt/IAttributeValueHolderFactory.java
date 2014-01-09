/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.faktorips.devtools.core.model.value.IValue;

/**
 * A factory create an {@link IValueHolder} for {@link IAttributeValue attribute values}. To use
 * this factory with {@link IAttributeValue} you have to register your holder type in the enum
 * {@link AttributeValueType} and provide a zero argument default constructor in your factory.
 * 
 * @author dirmeier
 */
public interface IAttributeValueHolderFactory<T> {

    /**
     * Creating a new value holder with the specified parent. The new value holder is not set as
     * value holder in the specified {@link IAttributeValue}.
     * 
     * @param parent The attribute value used as parent object
     * @return the newly created value holder.
     */
    public IValueHolder<T> createValueHolder(IAttributeValue parent);

    /**
     * Creating a new value holder with the specified parent and set the specified default value.
     * The new value holder is not set as value holder in the specified {@link IAttributeValue}. The
     * implementation must not perform a change event when setting the default value!
     * 
     * @param parent The attribute value used as parent object
     * @param defaultValue The {@link IValue} set as default value. If the new {@link IValueHolder}
     *            contains of multiple values, the default value is the only one value in the list
     *            of multiple values.
     * 
     * @return the newly created value holder.
     */
    public IValueHolder<T> createValueHolder(IAttributeValue parent, IValue<?> defaultValue);

}
