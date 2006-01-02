package org.faktorips.devtools.core.model.tablecontents;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 *
 */
public interface IRow extends IIpsObjectPart {
    
    /**
     * Returns the row number as string.
     *  
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName();
    
    /**
     * Returns the number of the row in the table. First row has number 0. 
     */
    public int getRowNumber();
    
    /**
     * Returns the value for the indicated column index.
     *  
     * @param column The column index. 
     * 
     * @throws IllegalArgumentException if the row does no contain a cell
     * for the indicated column index.
     */
    public String getValue(int column);
    
    /**
     * Sets the value for the indicated column index.
     *  
     * @param column	The column index.
     * @param newValue	The new value as string. 
     * 
     * @throws IllegalArgumentException if the row does no contain a cell
     * for the indicated column index.
     */
    public void setValue(int column, String newValue);
    
}
