package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 *
 */
public interface IColumn extends IIpsObjectPart, IKeyItem {
    
    public final static String PROPERTY_DATATYPE = "datatype";
    
    /**
     * Sets the column name.
     * 
     * @throws IllegalArgumentException if newName is <code>null</code>.
     */
    public void setName(String newName);
    
    /**
     * Sets the column's datatype.
     * 
     * @throws IllegalArgumentException if newDatatype is <code>null</code>.
     */
    public void setDatatype(String newDatatype);

}
