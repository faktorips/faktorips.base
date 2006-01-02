package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.devtools.core.model.IIpsObjectPart;


/**
 * A key is list of key items. Key items are either columns or ranges that 
 * belong to the same table.    
 */
public interface IKey extends IIpsObjectPart {
    
    /**
     * Returns the table structure the key belongs to.
     */
    public ITableStructure getTableStructure();
    
    /**
     * Returns the names of the key items that make up the key.
     */
    public String[] getKeyItemNames();
    
    /**
     * Returns the key items that make up the key. Only key items that can be resolved
     * by name are returned. 
     */
    public IKeyItem[] getKeyItems();
    
    /**
     * Sets the items this key is made up of.
     * 
     * @throws IllegalArgumentException if itemNames is <code>null</code> or
     * any of the names in the array is <code>null</code>. 
     */
    public void setKeyItems(String[] itemNames);
    
    /**
     * Adds the key item with the given name to the key.
     * 
     * @throws IllegalArgumentException if name is <code>null</code> 
     */
    public void addKeyItem(String name);
    
    /**
     * Removes the key item with the given name from the key. If the key does
     * not contain an item with the given name, the key remains unchanged.
     */
    public void removeKeyItem(String name);

    /**
     * Returns the number of key items.
     */
    public int getNumOfKeyItems();
    
    /**
     * Returns the key items that can be added to the key. These are the
     * table's key items that aren't part of the key yet. If no candidate
     * exists, an empty array is returned.
     */
    public IKeyItem[] getItemCandidates();
        
}
