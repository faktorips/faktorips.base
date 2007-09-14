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
     * Returns the first relation with the indicated name in this type or one of it's
     * supertypes. Returns <code>null</code> if no such relation exists.
     * <p>
     * Note that a relation's name is equal to it's target role singular, so you
     * can also use the target role singular as parameter.
     * <p>
     * Returns <code>null</code> if relationName is <code>null</code>.
     * 
     * @throws CoreException if an error occurs while searching the supertype hierarchy.
     */
    public IProductCmptTypeRelation findRelationInHierarchy(String relationName) throws CoreException;
    
}
