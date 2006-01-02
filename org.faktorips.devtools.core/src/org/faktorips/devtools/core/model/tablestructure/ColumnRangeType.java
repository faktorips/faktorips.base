package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.datatype.DefaultEnumType;
import org.faktorips.datatype.DefaultEnumValue;

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
        enumType = new DefaultEnumType("ColumnRangeType", ColumnRangeType.class);
        TWO_COLUMN_RANGE = new ColumnRangeType(enumType, "twoColumn", "Two columns");
        ONE_COLUMN_RANGE_FROM = new ColumnRangeType(enumType, "oneColumnFrom", "From column only");
        ONE_COLUMN_RANGE_TO = new ColumnRangeType(enumType, "oneColumnTo", "To column only");
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
