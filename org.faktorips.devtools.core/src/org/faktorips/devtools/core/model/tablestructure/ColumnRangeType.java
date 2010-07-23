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

/**
 * Instances of this class indicate the type of range that is represented by a ColumnRange instance.
 * 
 * @author Peter Erzberger
 */
public enum ColumnRangeType {

    TWO_COLUMN_RANGE(Messages.ColumnRangeType_twoColumns),

    ONE_COLUMN_RANGE_FROM(Messages.ColumnRangeType_fromColumnOnly),

    ONE_COLUMN_RANGE_TO(Messages.ColumnRangeType_toColumnOnly);

    private final String name;

    /**
     * Private constructor according to the type save enumeration pattern.
     */
    private ColumnRangeType(String name) {
        this.name = name;
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

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
}
