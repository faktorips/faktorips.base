package org.faktorips.devtools.core.model.tablestructure;

import org.eclipse.core.runtime.CoreException;

/**
 *
 */
public interface IForeignKey extends IKey {
    
    public final static String PROPERTY_REF_TABLE_STRUCTURE = "referencedTableStructure";
    
    public final static String PROPERTY_REF_UNIQUE_KEY = "referencedUniqueKey";
    

    /**
     * The name of the foreign key is the name of the table it references 
     * followed by an opening bracket, followed by the name of the referenced
     * unique key, followed by a closing bracket.
     * <p> 
     * Example: <code>referencedTableName(uniqueKeyName)</code>    
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName();
    
    /**
     * Returns the name of the table structure this foreign key references.
     * This method never returns null. 
     */
    public String getReferencedTableStructure();
    
    /**
     * Sets the table structure this key references.  
     * 
     * @throws IllegalArgumentException if tableStructure is <code>null</code>.
     */
    public void setReferencedTableStructure(String tableStructure);
    
    /**
     * Returns the table structure this foreign key references. 
     * Returns <code>null</code> if the table structure can't be found.
     * 
     * @throws CoreException if an error occurs while searching for the table structure.
     */
    public ITableStructure findReferencedTableStructure() throws CoreException;
    
    /**
     * Returns the name of the referenced unique key. 
     * This method never returns null. 
     */
    public String getReferencedUniqueKey();
    
    /**
     * Sets the unique key this foreign key references.  
     * 
     * @throws IllegalArgumentException if tableStructure is <code>null</code>.
     */
    public void setReferencedUniqueKey(String uniqueKey);
    

}
