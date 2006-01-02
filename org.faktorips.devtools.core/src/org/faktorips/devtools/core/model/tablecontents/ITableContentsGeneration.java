package org.faktorips.devtools.core.model.tablecontents;

import org.faktorips.devtools.core.model.IIpsObjectGeneration;

/**
 *
 */
public interface ITableContentsGeneration extends IIpsObjectGeneration {
    
    /**
     * Returns the rows that make up the table.
     */
    public IRow[] getRows();

    /**
     * Returns the number of rows in the table.
     */
    public int getNumOfRows();
    
    /**
     * Creates a new row.
     */
    public IRow newRow();

}
