package org.faktorips.devtools.core.model.tablestructure;

/**
 * A unique key is a list of key items that, given a value for each item, 
 * you can find exactly one row in the table is belongs to or no none.
 */
public interface IUniqueKey extends IKey {

    /**
     * The name of the unique key is the concatenation of it's items separated
     * by a comma and a space character (<code>", "</code>).
     *  
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
     */
    public String getName();
    
    /**
     * Returns true if the key contains any ranges.
     */
    public boolean containsRanges();

    /**
     * Returns true if the key contains any columns.
     */
    public boolean containsColumns();
    
    /**
     * Returns ture if the key contains only ranges.
     */
    public boolean containsRangesOnly();
    
}
