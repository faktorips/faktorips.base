/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import java.util.EnumSet;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;

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
     * <p>
     * This property is supported by {@link IProductCmptTypeAttribute} only.
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
    VISIBLE;

    public static void setValue(EnumSet<AttributeProperty> set, AttributeProperty value, boolean active) {
        if (active) {
            set.add(value);
        } else {
            set.remove(value);
        }
    }

}
