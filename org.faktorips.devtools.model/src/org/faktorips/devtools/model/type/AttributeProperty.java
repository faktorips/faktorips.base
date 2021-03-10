/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import java.util.EnumSet;

/**
 * This enum defines several properties that may be supported by an {@link IAttribute}.
 * <p>
 * Not every attribute have to support every property. The properties normally stored in an
 * {@link EnumSet}.
 * 
 * @author dirmeier
 */
public enum AttributeProperty {

    /**
     * This property defines whether an attribute may change over time, that means in different
     * generations.
     */
    CHANGING_OVER_TIME,

    /**
     * This property defines whether an attribute supports multiple values. If this property is set
     * the attribute value could have a list of values otherwise it has only one value.
     */
    MULTI_VALUE_ATTRIBUTE,

    /**
     * This property defines if an attribute is visible in its editor or not.
     */
    VISIBLE,

    /**
     * This property defines whether this attribute supports multiLingual. Multilingual support is
     * only allowed for attributes having String as a data type.
     * 
     */
    MULTILINGUAL;

    public static void setValue(EnumSet<AttributeProperty> set, AttributeProperty value, boolean active) {
        if (active) {
            set.add(value);
        } else {
            set.remove(value);
        }
    }

}
