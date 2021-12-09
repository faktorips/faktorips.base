/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.tablestructure;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public interface IForeignKey extends IKey {

    public static final String PROPERTY_REF_TABLE_STRUCTURE = "referencedTableStructure"; //$NON-NLS-1$

    public static final String PROPERTY_REF_UNIQUE_KEY = "referencedUniqueKey"; //$NON-NLS-1$

    /**
     * The name of the foreign key is the name of the table it references followed by an opening
     * bracket, followed by the name of the referenced unique key, followed by a closing bracket.
     * <p>
     * Example: <code>referencedTableName(uniqueKeyName)</code>
     * 
     * @see org.faktorips.devtools.model.IIpsElement#getName()
     */
    @Override
    public String getName();

    /**
     * Returns the name of the table structure this foreign key references. This method never
     * returns null.
     */
    public String getReferencedTableStructure();

    /**
     * Sets the table structure this key references.
     * 
     * @throws IllegalArgumentException if tableStructure is <code>null</code>.
     */
    public void setReferencedTableStructure(String tableStructure);

    /**
     * Returns the table structure this foreign key references. Returns <code>null</code> if the
     * table structure can't be found.
     * 
     * @throws CoreRuntimeException if an error occurs while searching for the table structure.
     */
    public ITableStructure findReferencedTableStructure(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the name of the referenced unique key. This method never returns null.
     */
    public String getReferencedUniqueKey();

    /**
     * Sets the unique key this foreign key references.
     * 
     * @throws IllegalArgumentException if tableStructure is <code>null</code>.
     */
    public void setReferencedUniqueKey(String uniqueKey);

}
