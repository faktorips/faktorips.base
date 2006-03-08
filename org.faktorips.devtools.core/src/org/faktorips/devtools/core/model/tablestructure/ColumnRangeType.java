/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;

/**
 * Instances of this class indicate the type of range that is represented by a ColumnRange instance.
 * 
 * @author Peter Erzberger
 */
public class ColumnRangeType extends DefaultEnumValue{

    
    public final static ColumnRangeType TWO_COLUMN_RANGE;
    
    public final static ColumnRangeType ONE_COLUMN_RANGE_FROM;
    
    public final static ColumnRangeType ONE_COLUMN_RANGE_TO;

    private final static DefaultEnumType enumType;
    
    static{
        enumType = new DefaultEnumType("ColumnRangeType", ColumnRangeType.class); //$NON-NLS-1$
        TWO_COLUMN_RANGE = new ColumnRangeType(enumType, "twoColumn", "Two columns"); //$NON-NLS-1$ //$NON-NLS-2$
        ONE_COLUMN_RANGE_FROM = new ColumnRangeType(enumType, "oneColumnFrom", "From column only"); //$NON-NLS-1$ //$NON-NLS-2$
        ONE_COLUMN_RANGE_TO = new ColumnRangeType(enumType, "oneColumnTo", "To column only"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static final DefaultEnumType getEnumType(){
        return enumType;
    }
    
    public static final ColumnRangeType getValueById(String id){
        return (ColumnRangeType)enumType.getEnumValue(id);
    }
    
    /**
     * Private constructor according to the type save enum pattern.
     */
    private ColumnRangeType(DefaultEnumType type, String id, String name){
        super(type, id, name);
    }
    
    public boolean isOneColumnFrom(){
        return ONE_COLUMN_RANGE_FROM.equals(this);
    }
    
    public boolean isOneColumnTo(){
        return ONE_COLUMN_RANGE_TO.equals(this);
    }
    
    public boolean isTwoColumn(){
        return TWO_COLUMN_RANGE.equals(this);
    }
}
