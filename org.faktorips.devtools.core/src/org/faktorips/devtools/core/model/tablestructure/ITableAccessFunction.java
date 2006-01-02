package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.devtools.core.model.IIpsObjectPart;


/**
 * The definition of a function to access a table. 
 * 
 * @author firstName lastName
 */
public interface ITableAccessFunction extends IIpsObjectPart {

    /**
     * Returns the table structure this function gives access to.
     */
    public ITableStructure getTableStructure();
    
    /**
     * Returns the name of the column accessed by this function.
     */
    public String getAccessedColumn();
    
    /**
     * Sets the name of the column accessed by this function.
     */
    public void setAccessedColumn(String columnName);

    /**
     * Returns the column accessed by this function or <code>null</code> if
     * the column can't be found.
     */
    public IColumn findAccessedColumn();
    
    /**
     * Returns the function's return type.
     */
    public String getType();
    
    /**
     * Sets the function's return type.
     */
    public void setType(String newType);
    
    /**
     * Sets the function's name.
     */
    public void setName(String newName);

    /**
     * Returns the function's arguments' types.
     */
    public String[] getArgTypes();
    
    /**
     * Sets the function's arguments' types.
     */
    public void setArgTypes(String[] argTypes);
    
}
