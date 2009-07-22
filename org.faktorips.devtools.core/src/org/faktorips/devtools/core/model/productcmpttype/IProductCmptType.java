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

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IType;

/**
 * A type of the product definition side of the model. 
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptType extends IType, IIpsMetaClass {

    public String PROPERTY_POLICY_CMPT_TYPE = "policyCmptType"; //$NON-NLS-1$
    
    public String PROPERTY_CONFIGURATION_FOR_POLICY_CMPT_TYPE = "configurationForPolicyCmptType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "ProductCmptType-"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the references policy component type does not exist.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST = MSGCODE_PREFIX + "PolicyCmptTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the referenced policy component type is not marked as confifurable.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_IS_NOT_MARKED_AS_CONFIGURABLE = MSGCODE_PREFIX + "PolicyCmptTypeNotMarkedAsConfigurable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the referenced policy component type does specify this product
     * component type or one if it's supertypes as configuration type. 
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE = MSGCODE_PREFIX + "PolicyCmptTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation code to indicate that a type has a different value for the ConfigurationForPolicyCmptType property
     * than it's supertype. 
     *
     * If a type's supertype configures a policy component type, the type must also configures one, even if it
     * the same. If the supertype doesn't configure a policy component type, also this one doesnt.
     */
    public final static String MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE = MSGCODE_PREFIX + "MustHaveSameValueForPolicyCmptType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists a mismatch between the product component type and the 
     * policy component type hierarchy.
     */
    public final static String MSGCODE_HIERARCHY_MISMATCH = MSGCODE_PREFIX + "HierarchyMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least two formula signatures have the same name.
     */
    public final static String MSGCODE_DUPLICATE_FORMULA_NAME_IN_HIERARCHY = MSGCODE_PREFIX + "DuplicateFormulaNameInHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least two formula signatures have the same name.
     */
    public final static String MSGCODE_PRODUCTCMPTTYPE_ABSTRACT_WHEN_POLICYCMPTTYPE_ABSTRACT = MSGCODE_PREFIX + "ProductCmptTypeAbstractWhenPolicyCmptTypeAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a formula name is only allowed once in a type neglecting the inheritance hierarchy.
     */
    public final static String MSGCODE_DUPLICATE_FORMULAS_NOT_ALLOWED_IN_SAME_TYPE = MSGCODE_PREFIX + "DuplicateFormulasNotAllowedInSameType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the overloaded product component type formula method cannot be overridden. 
     */
    public final static String MSGCODE_OVERLOADED_FORMULA_CANNOT_BE_OVERRIDDEN = MSGCODE_PREFIX + "OverloadedFormulaCannotBeOverridden"; //$NON-NLS-1$

    /**
     * Returns the policy component type this product component type refers to.
     * Returns <code>null</code> if this type does not refer to a policy component type.
     */
    public String getPolicyCmptType();

    /**
     * Returns <code>true</code> if this product component type configures a policy component type.
     * The configured policy component type can be requested via [{@link #getPolicyCmptType()} and {@link #findPolicyCmptType()}.
     * Note that if this method returns <code>true</code> it does not mean that the policy component type
     * actually exists.
     */
    public boolean isConfigurationForPolicyCmptType();
    
    /**
     * Sets if this product component type configures a policy component type or not.
     */
    public void setConfigurationForPolicyCmptType(boolean newValue);
    
    /**
     * Sets the policy component type this type refers to.
     */
    public void setPolicyCmptType(String newType);

    /**
     * Returns the policy component type this product component type refers to. 
     * Returns <code>null</code> if either this product component type does not refer to a policy
     * component type or the policy component type can't be found.
     * 
     * @param project The project which ips object path is used for the search.
     * This is not necessarily the project this type is part of. 
     * 
     * @throws CoreException if an error occurs while searching for the type.
     */
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException;
    
    /**
     * Returns the type's supertype if the type is based on a supertype and the supertype can be found
     * on the project's ips object path. Returns <code>null</code> if either this type is not based on
     * a supertype or the supertype can't be found on the project's ips object path. 
     * 
     * @param project The project which ips object path is used for the search.
     * This is not necessarily the project this type is part of. 
     *
     * @throws CoreException if an error occurs while searching for the supertype.
     */
    public IProductCmptType findSuperProductCmptType(IIpsProject ipsProject) throws CoreException;
    
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
     * @param ipsProject    The project which ips object path is used for the search.
     *                      This is not necessarily the project this type is part of. 
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
     * @param project The project which ips object path is used for the search.
     * This is not necessarily the project this type is part of. 
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
     * The formula name is set to the given formula name and the method name is set to the default
     * method name.
     * 
     * @param The name of the formula signature.
     * 
     * @see IProductCmptTypeMethod#getDefaultMethodName()
     */
    public IProductCmptTypeMethod newFormulaSignature(String formulaName);
    
    /**
     * Returns the type's methods. Typesafe version of <code>getMethods()</code>. 
     */
    public IProductCmptTypeMethod[] getProductCmptTypeMethods();

    /**
     * Returns the type's associations. Typesafe version of <code>getAssociations()</code>. 
     */
    public IProductCmptTypeAssociation[] getProductCmptTypeAssociations();
    
    /**
     * Returns the methods of this type which are no formula signatures.
     */
    public IProductCmptTypeMethod[] getNonFormulaProductCmptTypeMethods();
    
    /**
     * Returns the method signature with the indicates formula name. Returns <code>null</code>
     * if no such method is found in <strong>this</strong> type. The type hierarchy is not
     * searched.
     * Returns <code>null</code> if formulaName is <code>null</code>.
     */
    public IProductCmptTypeMethod getFormulaSignature(String formulaName) throws CoreException;

    /**
     * Returns all method signatures of this product component type neglecting the type hierarchy.
     * Returns an empty array if no formula signature is defined for this type.
     */
    public IProductCmptTypeMethod[] getFormulaSignatures();

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
     * Returns the formula signatures of formulas in the supertype hierarchy that are overloaded by formulas of this type.
     *  
     * @throws CoreException if an error occurs while searching.
     */
    public IProductCmptTypeMethod[] findSignaturesOfOverloadedFormulas(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the types product definition properties including properties defined in one of the type's supertypes.
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
