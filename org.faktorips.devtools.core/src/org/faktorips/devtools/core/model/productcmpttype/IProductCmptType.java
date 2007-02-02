/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * A product component type. Currently the product component type represents a filtered view
 * on the policy component type that gives access to product relevant information.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptType extends IIpsObject {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PRODUCTCMPTTYPE-"; //$NON-NLS-1$
    
	/**
	 * Returns <code>true</code> if the type is abstract, <code>false</code> otherwise.
	 */
	public boolean isAbstract();
	
	/**
	 * Returns the qualified name of the policy component type this is a 
	 * product component type for.
	 */
	public String getPolicyCmptyType();
	
	/**
	 * Returns the policy component type this is a product component type for
	 * or <code>null</code> if the policy component type can't be found.
	 * 
	 * @throws CoreException if an erros occurs while searching for the type.
	 */
	public IPolicyCmptType findPolicyCmptyType() throws CoreException;
	
    /**
     * Returns the type's supertype if the type is based on a supertype and the supertype can be found
     * on the project's ips object path. Returns <code>null</code> if either this type is not based on
     * a supertype or the supertype can't be found on the project's ips object path. 
     *
     * @throws CoreException if an error occurs while searching for the supertype.
	 */
	public IProductCmptType findSupertype() throws CoreException;
	
	/**
	 * Returns the attributes defined for this type or an empty array is no relation
	 * is defined. Note that computed or derived attributes are not returned as these 
	 * correspond to methods on the product side.
	 */
	public IAttribute[] getAttributes();

	/**
	 * Returns the relations defined for this type or an empty array is no relation
	 * is defined.
	 */
	public IProductCmptTypeRelation[] getRelations();
	
    /**
     * Returns the first relation with the indicated name or <code>null</code> if
     * no such relation exists.
     * <p>
     * Note that a relation's name is equal to it's target role singular, so you
     * can also use the target role singular as parameter.
     * 
     * @throws NullPointerException if name is <code>null</code>.
     */
	public IProductCmptTypeRelation getRelation(String relationName);
    
    /**
     * Returns the table structure usages defined for this type or an empty array is no usage
     * is defined. 
     */
    public ITableStructureUsage[] getTableStructureUsages();
	
    /**
     * Returns the table structure usage with the given role name. If more than one table structure
     * usages with the role name exist, the first table structure usage with the role name is
     * returned. Returns <code>null</code> if no table structure usage with the given role name
     * exists.
     */
    public ITableStructureUsage getTableStructureUsage(String roleName);
    
    /**
     * Looks for the ITableStructureUsage with the specified roleName starting from this policy component type and visiting up the
     * supertype hierarchy. If no ITableStructureUsage is found <code>null</code> will be returned.
     * 
     * @param roleName the role name of the ITableStructureUsage in question 
     * @return the ITableStructureUsage for the provided name or <code>null</code> if non is found
     */
    public ITableStructureUsage findTableStructureUsageInSupertypeHierarchy(String roleName) throws CoreException;

    /**
     * Creates a new table usage and returns it.
     */
    public ITableStructureUsage newTableStructureUsage();
    
    /**
     * Moves the table structure usages identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first object), no object is moved up. 
     * If one of the indexes is the number of objects - 1 (the last object)
     * no object is moved down. 
     * 
     * @param indexes   The indexes identifying the table structure usages.
     * @param up        <code>true</code>, to move the table structure usages up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved table structure usages.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a table structure usage.
     */
    public int[] moveTableStructureUsage(int[] indexes, boolean up);
}
