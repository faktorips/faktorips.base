/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype2;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IType;

/**
 * A type of the product definition side of the model. 
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptType extends IIpsObject, IType {

    public String PROPERTY_POLICY_CMPT_TYPE = "policyCmptType";
    
    public String PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE = "configurationForPolicyCmptType";

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "ProductCmptType-";
    
    /**
     * Validation message code to indicate that the references policy component type does not exist.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST = MSGCODE_PREFIX + "PolicyCmptTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Returns the policy component type this product component type refers to.
     * Returns <code>null</code> if this type does not refer to a policy component type.
     */
    public String getPolicyCmptType();
    
    /**
     * Sets the policy component type this type refers to.
     */
    public void setPolicyCmptType(String newType);

    /**
     * Returns <code>true</code> if this policy component type configures a policy component type
     * that can be requested via [{@link #getPolicyCmptType()} and {@link #findPolicyCmptType()}.
     * Note that if this method returns <code>true</code> it does not mean that the policy component type
     * actually exists.
     */
    public boolean isConfigurationForPolicyCmptType();
    
    /**
     * Returns the policy component type this product component type refers to. 
     * Returns <code>null</code> if either this product component type does not refer to a policy
     * component type or the policy component type can't be found.
     * 
     * @param <code>true</code> if the supertype hierarchy is searched if no policy component type is specified
     * in the type.
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     * 
     * @throws CoreException if an error occurs while searching for the type.
     */
    public IPolicyCmptType findPolicyCmptType(boolean searchSupertypeHierarchy, IIpsProject project) throws CoreException;
    
    /**
     * Returns the type's supertype if the type is based on a supertype and the supertype can be found
     * on the project's ips object path. Returns <code>null</code> if either this type is not based on
     * a supertype or the supertype can't be found on the project's ips object path. 
     * 
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     *
     * @throws CoreException if an error occurs while searching for the supertype.
     */
    public IProductCmptType findSuperProductCmptType(IIpsProject project) throws CoreException;
    
    /**
     * Returns the type's attributes.
     */
    public IAttribute[] getAttributes();
    
    /**
     * Returns the attribute with the given name defined in <strong>this</strong> type
     * (This method does not search the supertype hierarchy.)
     * If more than one attribute with the name exist, the first attribute with the name is returned.
     * Returns <code>null</code> if no attribute with the given name exists.
     */
    public IAttribute getAttribute(String name);

    /**
     * Creates a new attribute and returns it.
     */
    public IAttribute newAttribute();

    /**
     * Returns the number of attributes.
     */
    public int getNumOfAttributes();
    
    /**
     * Moves the attributes identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first attribute), no attribute is moved up. 
     * If one of the indexes is the number of attributes - 1 (the last attribute)
     * no attribute is moved down. 
     * 
     * @param indexes   The indexes identifying the attributes.
     * @param up        <code>true</code>, to move the attributes up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved attributes.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * an attribute.
     */
    public int[] moveAttributes(int[] indexes, boolean up);

    /**
     * Returns the type's relations.
     */
    public IRelation[] getRelations();
    
    /**
     * Returns the relation with the given name defined in <strong>this</strong> type
     * (This method does not search the supertype hierarchy.)
     * If more than one relation with the name exist, the first relation with the name is returned.
     * Returns <code>null</code> if no relation with the given name exists or name is <code>null</code>.
     */
    public IRelation getRelation(String name);

    /**
     * Searchs a relation with the given name in the supertype hierarchy and returns it. Returns <code>null</code> if
     * no such relation exists.
     * 
     * @param name          The relation' name.
     * @param includeSelf   <code>true</code> if this type itseld should be searched, <code>false</code> if only supertypes should be searched.
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     * 
     * @throws CoreException
     */
    public IRelation findRelationInSupertypeHierarchy(String name, boolean includeSelf, IIpsProject project) throws CoreException;

    /**
     * Creates a new relation and returns it.
     */
    public IRelation newRelation();

    /**
     * Returns the number of relations.
     */
    public int getNumOfRelations();
    
    /**
     * Moves the relations identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first relation), no relation is moved up. 
     * If one of the indexes is the number of reations - 1 (the last relation)
     * no relation is moved down. 
     * 
     * @param indexes   The indexes identifying the relations.
     * @param up        <code>true</code>, to move the relations up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved relations.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * an relation.
     */
    public int[] moveRelations(int[] indexes, boolean up);
    
    /**
     * Returns the number of table structure usages.
     */
    public int getNumOfTableStructureUsages();

    /**
     * Returns the table structure usages defined for this type or an empty array is no usage
     * is defined. 
     */
    public ITableStructureUsage[] getTableStructureUsages();
    
    /**
     * Returns the table structure usage with the given role name. If more than one table structure usage
     * with the role name exist, the first table structure usage with the role name is
     * returned. Returns <code>null</code> if no table structure usage with the given role name
     * exists.
     */
    public ITableStructureUsage getTableStructureUsage(String roleName);
    
    /**
     * Looks for the table structure usage with the specified roleName starting from this policy component type and visiting up the
     * supertype hierarchy. If no table structure usage is found, <code>null</code> is returned.
     * 
     * @param roleName the role name of the ITableStructureUsage in question 
     * @param includeSelf   <code>true</code> if this type itseld should be searched, <code>false</code> if only supertypes should be searched.
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     * 
     * @return the ITableStructureUsage for the provided name or <code>null</code> if non is found
     */
    public ITableStructureUsage findTableStructureUsageInSupertypeHierarchy(String roleName, boolean includeSelf, IIpsProject project) throws CoreException;

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
    
    /**
     * Returns a new product component type method.
     * Typesafe version of <code>newMethod</code>. 
     */
    public IProductCmptTypeMethod newProductCmptTypeMethod();
    
    /**
     * Returns the type's methods. Typesafe version of <code>getMethods()</code>. 
     */
    public IProductCmptTypeMethod[] getProductCmptTypeMethods();
    
    
    
}
