/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.tablestructure;

import java.util.Arrays;

import org.faktorips.devtools.model.INamedValue;
import org.faktorips.util.ArgumentCheck;

/**
 * Instances of this class indicate the type of range that is represented by a ColumnRange instance.
 * 
 * @author Peter Erzberger
 */
public enum ColumnRangeType implements INamedValue {
    TWO_COLUMN_RANGE("twoColumn", Messages.ColumnRangeType_twoColumns), //$NON-NLS-1$
    ONE_COLUMN_RANGE_FROM("oneColumnFrom", Messages.ColumnRangeType_fromColumnOnly), //$NON-NLS-1$
    ONE_COLUMN_RANGE_TO("oneColumnTo", Messages.ColumnRangeType_toColumnOnly); //$NON-NLS-1$

    private final String id;
    private final String name;

    /**
     * Private constructor according to the type save enumeration pattern.
     */
    ColumnRangeType(String id, String name) {
        ArgumentCheck.notNull(id);
        ArgumentCheck.notNull(name);
        this.id = id;
        this.name = name;
    }

    public static final ColumnRangeType getValueById(String id) {
        return Arrays.stream(ColumnRangeType.values()).filter(s -> s.id.equals(id)).findAny().orElse(null);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
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
