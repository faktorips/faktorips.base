package org.faktorips.devtools.core.model.tablestructure;

/**
 * A key item is a part of a unique key. There are two kind of key items: columns and ranges.
 */
public interface IKeyItem {

    /**
     * Returns the item's name.
     */
    public String getName();
    
    /**
     * Returns the name for a parameter in a table access function. For columns this is
     * the name of the column, for ranges this paramter can be specified.
     */
    public String getAccessParameterName();
    
    /**
     * Returns the item's datatype. For columns this is the column's datatype and for
     * ranges this is the datatype of the column if it's a one column range and the first
     * column's datatype if it is a two column range. 
     */
    public String getDatatype();
    
    /**
     * Returns the columns this item comprises.
     */
    public IColumn[] getColumns();
}
