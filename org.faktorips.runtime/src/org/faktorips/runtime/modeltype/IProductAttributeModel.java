/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype;

import java.util.Calendar;
import java.util.List;

import org.faktorips.runtime.IProductComponent;

/**
 * Represents an attribute in a IpsProductCmptType.
 */
public interface IProductAttributeModel extends IModelTypeAttribute {

    /**
     * Returns the value of this attribute in the given product component (or its generation
     * identified by the effectiveDate, if the attribute is changeable over time).
     * <p>
     * It is safe to cast the returned object to the class returned by {@link #getDatatype()},
     * except when the attribute is a {@linkplain #isMultiValue() multi-value} attribute - then a
     * {@link List} is returned (and it's contents can be cast to the class returned by
     * {@link #getDatatype()}).
     * 
     * @param productComponent a product component based on the product component type this
     *            attribute belongs to.
     * @param effectiveDate (optional) the date to use for selecting the product component's
     *            generation, if this attribute {@link #isChangingOverTime()}
     */
    public Object getValue(IProductComponent productComponent, Calendar effectiveDate);

    /**
     * Whether this attribute has just one value or multiple values. If the attribute has multiple
     * values, {@link #getDatatype()} will still return the class of a single value, but
     * {@link #getValue(IProductComponent, Calendar)} will return a {@link List}.
     */
    public Boolean isMultiValue();

}
