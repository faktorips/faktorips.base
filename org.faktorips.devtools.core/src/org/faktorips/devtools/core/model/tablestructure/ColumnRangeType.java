/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

    public final static ColumnRangeType TWO_COLUMN_RANGE;

    public final static ColumnRangeType ONE_COLUMN_RANGE_FROM;

    public final static ColumnRangeType ONE_COLUMN_RANGE_TO;

    private final static DefaultEnumType enumType;

    static {
        enumType = new DefaultEnumType("ColumnRangeType", ColumnRangeType.class); //$NON-NLS-1$
        TWO_COLUMN_RANGE = new ColumnRangeType(enumType, "twoColumn", Messages.ColumnRangeType_twoColumns); //$NON-NLS-1$
        ONE_COLUMN_RANGE_FROM = new ColumnRangeType(enumType, "oneColumnFrom", Messages.ColumnRangeType_fromColumnOnly); //$NON-NLS-1$
        ONE_COLUMN_RANGE_TO = new ColumnRangeType(enumType, "oneColumnTo", Messages.ColumnRangeType_toColumnOnly); //$NON-NLS-1$
    }

    public static final DefaultEnumType getEnumType() {
        return enumType;
    }

    public static final ColumnRangeType getValueById(String id) {
        return (ColumnRangeType)enumType.getEnumValue(id);
    }

    /**
     * Private constructor according to the type save enumeration pattern.
     */
    private ColumnRangeType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

    public boolean isOneColumnFrom() {
        return ONE_COLUMN_RANGE_FROM.equals(this);
    }

    public boolean isOneColumnTo() {
        return ONE_COLUMN_RANGE_TO.equals(this);
    }

    public boolean isTwoColumn() {
        return TWO_COLUMN_RANGE.equals(this);
    }
}
