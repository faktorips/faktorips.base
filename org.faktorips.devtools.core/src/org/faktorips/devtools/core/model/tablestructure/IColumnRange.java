package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 *
 */
public interface IColumnRange extends IIpsObjectPart, IKeyItem {

    public final static String PROPERTY_RANGE_TYPE = "columnRangeType"; //$NON-NLS-1$
    public final static String PROPERTY_FROM_COLUMN = "fromColumn"; //$NON-NLS-1$
    public final static String PROPERTY_TO_COLUMN = "toColumn"; //$NON-NLS-1$
    public final static String PROPERTY_PARAMETER_NAME = "parameterName"; //$NON-NLS-1$
    
    /**
     * Returns the table structure this range belongs to.
     */
    public ITableStructure getTableStructure();
    
    /**
     * Returns the range's name. The name is made up of the first column name,
     * followed by a dash ('-'), followed by the second column's name.
     * <p>
     * Example: <code>ageFrom-ageTo</code>
     *  
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName();
    
    /**
     * Returns the name of the column that defines the beginning of the range 
     * or <code>null</code> if no such column is defined yet.
     */
    public String getFromColumn();
    
    /**
     * Sets the name of the column that defines the beginning of the range.
     * 
     * @throws IllegalArgumentException if columnName is <code>null</code>
     */
    public void setFromColumn(String columnName);
    
    /**
     * Returns the name of the column that defines the end of the range or <code>null</code> if no
     * such column is defined yet.
     */
    public String getToColumn();
    
    /**
     * Sets the name of the column that defines the end of the range.
     * 
     * @throws IllegalArgumentException if columnName is <code>null</code>
     */
    public void setToColumn(String columnName);

    /**
     * Sets the type of this column range. Dependent on the range type both or only one column is to specify as
     * a the range defining column. If the range type is ONE_COLUMN_RANGE_FROM only the "from" column field needs
     * to be specified and if the range type is ONE_COLUMN_RANGE_TO only the "to" field needs to be specified.
     * 
     */
    public void setColumnRangeType(ColumnRangeType rangeType);
    
    /**
     * Returns the range type of this column range.
     */
    public ColumnRangeType getColumnRangeType();
    
    /**
     * Returns the datatype identifier of the columns of this range.
     */
    public String getDatatype();
    
    /**
     * The name is supposed to be used in generated code for a variable or parameter that 
     * specifies a value assigned to this range. 
     */
    public String getParameterName();
}
