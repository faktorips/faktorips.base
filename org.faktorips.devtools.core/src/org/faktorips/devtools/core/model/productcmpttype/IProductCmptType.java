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

package org.faktorips.devtools.core.model.productcmpttype;

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
    public IProductCmptTypeAttribute[] getProductCmptTypeAttributes();
    
    /**
     * Returns the attribute with the given name defined in <strong>this</strong> type
     * (This method does not search the supertype hierarchy.)
     * If more than one attribute with the name exist, the first attribute with the name is returned.
     * Returns <code>null</code> if no attribute with the given name exists.
     */
    public IProductCmptTypeAttribute getProductCmptTypeAttribute(String name);


    /**
     * Searches an attribute with the given name in the type and it's supertype hierarchy and returns it. 
     * Returns <code>null</code> if no such attribute exists.
     * 
     * @param name          The attribute's name.
     * @param ipsProject       The project which ips object path is used for the searched.
     *                      This is not neccessarily the project this type is part of. 
     * 
     * @throws NullPointerException if project is <code>null</code>.
     * @throws CoreException if an error occurs while searching.
     */
    public IProductCmptTypeAttribute findProductCmptTypeAttribute(String name, IIpsProject ipsProject) throws CoreException;
    
    /**
     * Creates a new attribute and returns it.
     */
    public IProductCmptTypeAttribute newProductCmptTypeAttribute();

    /**
     * Creates a new attribute with the given name and returns it.
     */
    public IProductCmptTypeAttribute newProductCmptTypeAttribute(String name);

    /**
     * Creates a new association and returns it.
     */
    public IProductCmptTypeAssociation newProductCmptTypeAssociation();

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
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     * 
     * @return the ITableStructureUsage for the provided name or <code>null</code> if non is found
     */
    public ITableStructureUsage findTableStructureUsage(String roleName, IIpsProject project) throws CoreException;

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
     * Returns a new product component type method that has the role of a formula signature.
     * 
     * @param The name of the formula signature.
     */
    public IProductCmptTypeMethod newFormulaSignature(String formulaName);
    
    /**
     * Returns the type's methods. Typesafe version of <code>getMethods()</code>. 
     */
    public IProductCmptTypeMethod[] getProductCmptTypeMethods();
    
    /**
     * Returns the method signature with the indicates formula name. Returns <code>null</code>
     * if no such method is found in <strong>this</strong> type. The type hierarchy is not
     * searched.
     * Returns <code>null</code> if formulaName is <code>null</code>.
     */
    public IProductCmptTypeMethod getFormulaSignature(String formulaName) throws CoreException;

    /**
     * Searches the method signature with the indicated formula name in the type's supertype hierarchy. 
     * Returns <code>null</code> if no such method is found.
     * 
     * @param formulaName The formula name to search
     * @param The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching.
     * @throws NullPointerException if ips project is <code>null</code>.
     */
    public IProductCmptTypeMethod findFormulaSignature(String formulaName, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the types product definition properties inlcuding properties defined in one of the type's supertypes.
     * 
     * @throws CoreException
     */
    public IProdDefProperty[] findProdDefProperties(IIpsProject ipsProject) throws CoreException;
    
    /**
     * Returns the product definition property with the given name and type. If no such property is found in the type itself, 
     * the supertype hierarchy is searched. 
     * 
     * @throws CoreException
     */
    public IProdDefProperty findProdDefProperty(ProdDefPropertyType type, String propName, IIpsProject ipsProject) throws CoreException;
    
    /**
     * Returns the product definition property with the given name. If no such property is found in the type itself, 
     * the supertype hierarchy is searched. 
     * 
     * @throws CoreException
     */
    public IProdDefProperty findProdDefProperty(String propName, IIpsProject ipsProject) throws CoreException;
}
