/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public interface IForeignKey extends IKey {

    public final static String PROPERTY_REF_TABLE_STRUCTURE = "referencedTableStructure"; //$NON-NLS-1$

    public final static String PROPERTY_REF_UNIQUE_KEY = "referencedUniqueKey"; //$NON-NLS-1$

    /**
     * The name of the foreign key is the name of the table it references followed by an opening
     * bracket, followed by the name of the referenced unique key, followed by a closing bracket.
     * <p>
     * Example: <code>referencedTableName(uniqueKeyName)</code>
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getName()
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
     * @throws CoreException if an error occurs while searching for the table structure.
     */
    public ITableStructure findReferencedTableStructure(IIpsProject ipsProject) throws CoreException;

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
