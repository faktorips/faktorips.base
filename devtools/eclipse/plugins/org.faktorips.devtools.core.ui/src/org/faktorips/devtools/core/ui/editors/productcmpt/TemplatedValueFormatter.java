/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.ITemplatedValue;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Formats the value of {@link ITemplatedValue templated values}. It uses the
 * {@link PropertyValueFormatter} to format {@link IPropertyValue} and defines an additional
 * formatter that is used to format {@link IProductCmptLink}.
 */
public class TemplatedValueFormatter {

    public static final Function<IProductCmptLink, String> PRODUCT_CMPT_LINK = link -> link != null
            ? link.getCardinality().format()
            : IpsStringUtils.EMPTY;

    // Utility class that should not be instantiated
    private TemplatedValueFormatter() {
        super();
    }

    /**
     * @return the formatted value of the provided {@link IPropertyValue}
     * @throws NullPointerException if property value is <code>null</code>.
     * @throws IllegalStateException if the {@link PropertyValueType} is unknown.
     */
    public static String format(ITemplatedValue value) {
        if (value == null) {
            throw new NullPointerException("Cannot format null"); //$NON-NLS-1$
        }
        if (value instanceof IPropertyValue) {
            return PropertyValueFormatter.format((IPropertyValue)value);
        }
        if (value instanceof IProductCmptLink) {
            return PRODUCT_CMPT_LINK.apply((IProductCmptLink)value);
        }
        throw new IllegalArgumentException("Cannot format value of type " + value.getClass()); //$NON-NLS-1$

    }

    public static String shortedFormat(ITemplatedValue propertyValue) {
        return StringUtils.abbreviateMiddle(format(propertyValue), "[...]", 45); //$NON-NLS-1$
    }

}
