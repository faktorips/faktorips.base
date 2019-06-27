/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;

/**
 * The aggregation kind as specified in the UML super structure document.
 * 
 * @author Jan Ortmann
 */
public class AggregationKind extends DefaultEnumValue {

    public static final AggregationKind NONE;

    public static final AggregationKind SHARED;

    public static final AggregationKind COMPOSITE;

    private static final DefaultEnumType enumType;

    public static final DefaultEnumType getEnumType() {
        return enumType;
    }

    public static final AggregationKind getKind(String id) {
        return (AggregationKind)enumType.getEnumValue(id);
    }

    static {
        enumType = new DefaultEnumType("AggregationKind", AggregationKind.class); //$NON-NLS-1$
        NONE = new AggregationKind(enumType, "none", "None"); //$NON-NLS-1$ //$NON-NLS-2$
        SHARED = new AggregationKind(enumType, "shared", "Shared"); //$NON-NLS-1$ //$NON-NLS-2$
        COMPOSITE = new AggregationKind(enumType, "composite", "Composite"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private AggregationKind(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

}
