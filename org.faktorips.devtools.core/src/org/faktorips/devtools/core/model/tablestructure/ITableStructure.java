package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.devtools.core.model.IIpsObject;

/**
 *
 */
public interface ITableStructure extends IIpsObject {
    
    /**
     * Returns the functions to access the table in the formula language.
     */
    public ITableAccessFunction[] getAccessFunctions();
    
    /**
     * Returns the table's columns.
     */
    public IColumn[] getColumns();
    
    /**
     * Returns the first column with the indicated name or <code>null</code>
     * if the table structure does not contain a column with the name.
     */
    public IColumn getColumn(String name);
    
    /**
     * Returns the number of columns in the table.
     */
    public int getNumOfColumns();

    /**
     * Creates a new column.
     */
    public IColumn newColumn();
    
    /**
     * Moves the columns identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first column), no column is moved up. 
     * If one of the indexes is the number of columns - 1 (the last column)
     * no column is moved down. 
     * 
     * @param indexes	The indexes identifying the columns.
     * @param up 		<code>true</code>, to move the columns up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved columns.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a column.
     */
    public int[] moveColumns(int[] indexes, boolean up);
    
    /**
     * Returns the table's column ranges.
     */
    public IColumnRange[] getRanges();
    
    /**
     * Returns the first range with the indicated name or <code>null</code>
     * if the table structure does not contain a range with the name.
     */
    public IColumnRange getRange(String name);
    
    /**
     * Returns true if the table has a range with the specified name.
     */
    public boolean hasRange(String name);
    
    /**
     * Returns true if the table has a column with the specified name.
     */
    public boolean hasColumn(String name);
    
    /**
     * Returns the number of ranges in the table.
     */
    public int getNumOfRanges();

    /**
     * Creates a new range.
     */
    public IColumnRange newRange();
    
    /**
     * Moves the ranges identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first range), no range is moved up. 
     * If one of the indexes is the number of ranges - 1 (the last range)
     * no range is moved down. 
     * 
     * @param indexes	The indexes identifying the ranges.
     * @param up 		<code>true</code>, to move the ranges up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved ranges.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a range.
     */
    public int[] moveRanges(int[] indexes, boolean up);
    
    /**
     * Returns the table's unique keys.
     */
    public IUniqueKey[] getUniqueKeys();
    
    /**
     * Returns the first unique key with the indicated name or <code>null</code>
     * if the table structure does not contain a range with the name.
     */
    public IUniqueKey getUniqueKey(String name);
    
    /**
     * Returns the number of unique keys in the table.
     */
    public int getNumOfUniqueKeys();

    /**
     * Creates a new unique key.
     */
    public IUniqueKey newUniqueKey();
    
    /**
     * Moves the unique keys identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first key), no range is moved up. 
     * If one of the indexes is the number of keys - 1 (the last key)
     * no range is moved down. 
     * 
     * @param indexes	The indexes identifying the keys.
     * @param up 		<code>true</code>, to move the keys up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved keys.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a key.
     */
    public int[] moveUniqueKeys(int[] indexes, boolean up);
    
    /**
     * Returns the table's foreign keys.
     */
    public IForeignKey[] getForeignKeys();
    
    /**
     * Returns the first foreign key with the indicated name or <code>null</code>
     * if the table structure does not contain a foreign key with the name.
     */
    public IForeignKey getForeignKey(String name);
    
    /**
     * Returns the number of foreign keys in the table.
     */
    public int getNumOfForeignKeys();

    /**
     * Creates a new foreign key.
     */
    public IForeignKey newForeignKey();
    
    /**
     * Moves the foreign keys identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first key), no range is moved up. 
     * If one of the indexes is the number of keys - 1 (the last key)
     * no range is moved down. 
     * 
     * @param indexes	The indexes identifying the keys.
     * @param up 		<code>true</code>, to move the keys up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved keys.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a key.
     */
    public int[] moveForeignKeys(int[] indexes, boolean up);
    
    /**
     * Returns the columns not contained in the given key.
     * 
     *  @throws NullPointerException if key is <code>null</code>.
     */
    public IColumn[] getColumnsNotInKey(IKey key);
    
}
