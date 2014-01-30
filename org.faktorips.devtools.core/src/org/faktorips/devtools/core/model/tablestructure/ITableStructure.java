/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

import java.util.List;

import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;

public interface ITableStructure extends IIpsMetaClass, ILabeledElement {

    public static final String PROPERTY_TYPE = "tableStructureType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "TABLESTRUCTURE-"; //$NON-NLS-1$

    /**
     * Returns <code>true</code> if multiple table contents are allowed for this table structure,
     * otherwise <code>false</code>.
     */
    public boolean isMultipleContentsAllowed();

    /**
     * Set the type for this table structure represented by the given id. If no type is found for
     * the given id, the type is not modified.
     */
    public void setTableStructureType(TableStructureType type);

    /**
     * @return The type of this table structure.
     */
    public TableStructureType getTableStructureType();

    /**
     * Returns the functions to access the table in the formula language.
     * 
     */
    public ITableAccessFunction[] getAccessFunctions();

    /**
     * Returns the table's columns.
     */
    public IColumn[] getColumns();

    /**
     * Returns the first column with the indicated name or <code>null</code> if the table structure
     * does not contain a column with the name.
     */
    public IColumn getColumn(String name);

    /**
     * Returns the column at the given index. Returns null if the index is out of bounds (less than
     * zero or greater or equal than the number of columns).
     */
    public IColumn getColumn(int index);

    /**
     * Returns the index of the given column in this tablestructure's list of columns. Throws a
     * RuntimeException is the column is not part of this {@link ITableStructure}.
     * 
     * @throws RuntimeException if the column was not found
     */
    public int getColumnIndex(IColumn column);

    /**
     * Returns the index for the given name of a column in this tablestructure's list of columns.
     * Throws a RuntimeException is the column is not part of this {@link ITableStructure}.
     * 
     * @param columnName The name of the column
     * @throws RuntimeException if the column was not found
     */
    public int getColumnIndex(String columnName);

    /**
     * Returns the number of columns in the table.
     */
    public int getNumOfColumns();

    /**
     * Creates a new column.
     */
    public IColumn newColumn();

    /**
     * Moves the columns identified by the indexes up or down by one position. If one of the indexes
     * is 0 (the first column), no column is moved up. If one of the indexes is the number of
     * columns - 1 (the last column) no column is moved down.
     * 
     * @param indexes The indexes identifying the columns.
     * @param up <code>true</code>, to move the columns up, <false> to move them down.
     * 
     * @return The new indexes of the moved columns.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a column.
     */
    public int[] moveColumns(int[] indexes, boolean up);

    /**
     * Returns the table's column ranges.
     */
    public IColumnRange[] getRanges();

    /**
     * Returns the first range with the indicated name or <code>null</code> if the table structure
     * does not contain a range with the name.
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
     * Moves the ranges identified by the indexes up or down by one position. If one of the indexes
     * is 0 (the first range), no range is moved up. If one of the indexes is the number of ranges -
     * 1 (the last range) no range is moved down.
     * 
     * @param indexes The indexes identifying the ranges.
     * @param up <code>true</code>, to move the ranges up, <false> to move them down.
     * 
     * @return The new indexes of the moved ranges.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a range.
     */
    public int[] moveRanges(int[] indexes, boolean up);

    /**
     * Returns the table's unique keys.
     */
    public IIndex[] getUniqueKeys();

    /**
     * Returns the first unique key with the indicated name or <code>null</code> if the table
     * structure does not contain a unique key with the name.
     */
    public IIndex getUniqueKey(String name);

    /**
     * Returns the number of unique keys in the table, these are all indices that are marked as
     * unique.
     */
    public int getNumOfUniqueKeys();

    /**
     * Returns true, if there are several indices based on the same datatypes
     * 
     * @deprecated Since 3.11 use {@link #hasIndexWithSameDatatype()} instead
     */
    @Deprecated
    public boolean hasUniqueKeysWithSameDatatype();

    /**
     * Returns true, if there are several indices based on the same datatypes
     */
    public boolean hasIndexWithSameDatatype();

    /**
     * Creates a new unique key.
     * 
     * @deprecated Since 3.11 use {@link #newIndex()} instead
     */
    @Deprecated
    public IIndex newUniqueKey();

    /**
     * Creates a new index.
     */
    public IIndex newIndex();

    /**
     * Moves the {@link IIndex} identified by the indices up or down by one position. If one of the
     * indices is 0 (the first key), no {@link IIndex} is moved up. If one of the indexes is the
     * number of keys - 1 (the last key) no {@link IIndex} is moved down.
     * 
     * @param indices The indices of the {@link IIndex} instances that should be moved up or down.
     * @param up <code>true</code>, to move the objects up, <code>false</code> to move them down.
     * 
     * @return The new indices of the moved {@link IIndex} objects.
     * 
     * @throws NullPointerException if indices is null.
     * @throws IndexOutOfBoundsException if one of the indices does not identify a {@link IIndex}
     *             object.
     * @deprecated Since 3.11 use {@link #moveIndex(int[],boolean)} instead
     */
    @Deprecated
    public int[] moveUniqueKeys(int[] indices, boolean up);

    /**
     * Moves the {@link IIndex} identified by the indices up or down by one position. If one of the
     * indices is 0 (the first key), no {@link IIndex} is moved up. If one of the indexes is the
     * number of keys - 1 (the last key) no {@link IIndex} is moved down.
     * 
     * @param indices The indices of the {@link IIndex} instances that should be moved up or down.
     * @param up <code>true</code>, to move the objects up, <code>false</code> to move them down.
     * 
     * @return The new indices of the moved {@link IIndex} objects.
     * 
     * @throws NullPointerException if indices is null.
     * @throws IndexOutOfBoundsException if one of the indices does not identify a {@link IIndex}
     *             object.
     */
    public int[] moveIndex(int[] indices, boolean up);

    /**
     * Returns the table's indices.
     */
    public List<IIndex> getIndices();

    /**
     * Returns the first index with the indicated name or <code>null</code> if the table structure
     * does not contain an index with the name.
     */
    public IIndex getIndex(String name);

    /**
     * Returns the number of indices in the table.
     */
    public int getNumOfIndices();

    /**
     * Returns the table's foreign keys.
     */
    public IForeignKey[] getForeignKeys();

    /**
     * Returns the first foreign key with the indicated name or <code>null</code> if the table
     * structure does not contain a foreign key with the name.
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
     * Moves the foreign keys identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first key), no range is moved up. If one of the indexes is the number of
     * keys - 1 (the last key) no range is moved down.
     * 
     * @param indexes The indexes identifying the keys.
     * @param up <code>true</code>, to move the keys up, <false> to move them down.
     * 
     * @return The new indexes of the moved keys.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a key.
     */
    public int[] moveForeignKeys(int[] indexes, boolean up);

    /**
     * Returns the columns not contained in the given key.
     * 
     * @throws NullPointerException if key is <code>null</code>.
     */
    public IColumn[] getColumnsNotInKey(IKey key);

}
