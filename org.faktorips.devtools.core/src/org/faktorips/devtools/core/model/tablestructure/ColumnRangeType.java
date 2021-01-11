/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;

/**
 * Instances of this class indicate the type of range that is represented by a ColumnRange instance.
 * 
 * @author Peter Erzberger
 */
public class ColumnRangeType extends DefaultEnumValue {

    public static final ColumnRangeType TWO_COLUMN_RANGE;

    public static final ColumnRangeType ONE_COLUMN_RANGE_FROM;

    public static final ColumnRangeType ONE_COLUMN_RANGE_TO;

    private static final DefaultEnumType DEFAULT_ENUM_TYPE;

    static {
        DEFAULT_ENUM_TYPE = new DefaultEnumType("ColumnRangeType", ColumnRangeType.class); //$NON-NLS-1$
        TWO_COLUMN_RANGE = new ColumnRangeType(DEFAULT_ENUM_TYPE, "twoColumn", Messages.ColumnRangeType_twoColumns); //$NON-NLS-1$
        ONE_COLUMN_RANGE_FROM = new ColumnRangeType(DEFAULT_ENUM_TYPE,
                "oneColumnFrom", Messages.ColumnRangeType_fromColumnOnly); //$NON-NLS-1$
        ONE_COLUMN_RANGE_TO = new ColumnRangeType(DEFAULT_ENUM_TYPE,
                "oneColumnTo", Messages.ColumnRangeType_toColumnOnly); //$NON-NLS-1$
    }

    /**
     * Private constructor according to the type save enumeration pattern.
     */
    private ColumnRangeType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

    public static final DefaultEnumType getEnumType() {
        return DEFAULT_ENUM_TYPE;
    }

    public static final ColumnRangeType getValueById(String id) {
        return (ColumnRangeType)DEFAULT_ENUM_TYPE.getEnumValue(id);
    }

    public boolean isOneColumnFrom() {
        return equals(ONE_COLUMN_RANGE_FROM);
    }

    public boolean isOneColumnTo() {
        return equals(ONE_COLUMN_RANGE_TO);
    }

    public boolean isTwoColumn() {
        return equals(TWO_COLUMN_RANGE);
    }
}
