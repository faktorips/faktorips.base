/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;

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
    public String PROPERTY_ICON_FOR_INSTANCES = "instancesIcon"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "ProductCmptType-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the references policy component type does not exist.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_EXIST = MSGCODE_PREFIX + "PolicyCmptTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the referenced policy component type is not marked
     * as configurable.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_IS_NOT_MARKED_AS_CONFIGURABLE = MSGCODE_PREFIX
            + "PolicyCmptTypeNotMarkedAsConfigurable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the referenced policy component type does specify
     * this product component type or one if it's supertypes as configuration type.
     */
    public final static String MSGCODE_POLICY_CMPT_TYPE_DOES_NOT_SPECIFY_THIS_TYPE = MSGCODE_PREFIX
            + "PolicyCmptTypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation code to indicate that a type has a different value for the
     * ConfigurationForPolicyCmptType property than it's supertype.
     * <p>
     * If a type's supertype configures a policy component type, the type must also configures one,
     * even if it the same. If the supertype doesn't configure a policy component type, also this
     * one doesn't.
     */
    public final static String MSGCODE_MUST_HAVE_SAME_VALUE_FOR_CONFIGURES_POLICY_CMPT_TYPE = MSGCODE_PREFIX
            + "MustHaveSameValueForPolicyCmptType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists a mismatch between the product
     * component type and the policy component type hierarchy.
     */
    public final static String MSGCODE_HIERARCHY_MISMATCH = MSGCODE_PREFIX + "HierarchyMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least two formula signatures have the same name.
     */
    public final static String MSGCODE_DUPLICATE_FORMULA_NAME_IN_HIERARCHY = MSGCODE_PREFIX
            + "DuplicateFormulaNameInHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that at least two formula signatures have the same name.
     */
    public final static String MSGCODE_PRODUCTCMPTTYPE_ABSTRACT_WHEN_POLICYCMPTTYPE_ABSTRACT = MSGCODE_PREFIX
            + "ProductCmptTypeAbstractWhenPolicyCmptTypeAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a formula name is only allowed once in a type
     * neglecting the inheritance hierarchy.
     */
    public final static String MSGCODE_DUPLICATE_FORMULAS_NOT_ALLOWED_IN_SAME_TYPE = MSGCODE_PREFIX
            + "DuplicateFormulasNotAllowedInSameType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the overloaded product component type formula method
     * cannot be overridden.
     */
    public final static String MSGCODE_OVERLOADED_FORMULA_CANNOT_BE_OVERRIDDEN = MSGCODE_PREFIX
            + "OverloadedFormulaCannotBeOverridden"; //$NON-NLS-1$
    /**
     * Validation message code to indicate that the custom icon cannot be resolved.
     */
    public final static String MSGCODE_ICON_PATH_INVALID = MSGCODE_PREFIX + "IconPathInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding policy component type is inherited
     * from a super type, but the product component type is not.
     */
    public final static String MSGCODE_MUST_HAVE_SUPERTYPE = MSGCODE_PREFIX + "MustHaveSupertype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no default {@link IProductCmptCategory} for
     * {@link IProductCmptTypeMethod}s marked as formula signature definition exists.
     */
    public final static String MSGCODE_NO_DEFAULT_CATEGORY_FOR_FORMULA_SIGNATURE_DEFINITIONS = MSGCODE_PREFIX
            + "NoDefaultCategoryForFormulaSignatureDefinitions"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no default {@link IProductCmptCategory} for product
     * relevant {@link IValidationRule}s exists.
     */
    public final static String MSGCODE_NO_DEFAULT_CATEGORY_FOR_VALIDATION_RULES = MSGCODE_PREFIX
            + "NoDefaultCategoryForValidationRules"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no default {@link IProductCmptCategory} for
     * {@link ITableStructureUsage}s exists.
     */
    public final static String MSGCODE_NO_DEFAULT_CATEGORY_FOR_TABLE_STRUCTURE_USAGES = MSGCODE_PREFIX
            + "NoDefaultCategoryForTableStructureUsages"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no default {@link IProductCmptCategory} for product
     * relevant {@link IPolicyCmptTypeAttribute}s exists.
     */
    public final static String MSGCODE_NO_DEFAULT_CATEGORY_FOR_POLICY_CMPT_TYPE_ATTRIBUTES = MSGCODE_PREFIX
            + "NoDefaultCategoryForPolicyCmptTypeAttributes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that no default {@link IProductCmptCategory} for
     * {@link IProductCmptTypeAttribute}s exists.
     */
    public final static String MSGCODE_NO_DEFAULT_CATEGORY_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES = MSGCODE_PREFIX
            + "NoDefaultCategoryForProductCmptTypeAttributes"; //$NON-NLS-1$

    /**
     * Returns the policy component type this product component type refers to. Returns
     * <code>null</code> if this type does not refer to a policy component type.
     */
    public String getPolicyCmptType();

    /**
     * Returns <code>true</code> if this product component type configures a policy component type.
     * The configured policy component type can be requested via [{@link #getPolicyCmptType()} and
     * {@link #findPolicyCmptType(IIpsProject)}. Note that if this method returns <code>true</code>
     * it does not mean that the policy component type actually exists.
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
     * Returns the policy component type this product component type refers to. Returns
     * <code>null</code> if either this product component type does not refer to a policy component
     * type or the policy component type can't be found.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the type.
     */
    public IPolicyCmptType findPolicyCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the type's supertype if the type is based on a supertype and the supertype can be
     * found on the project's IPS object path. Returns <code>null</code> if either this type is not
     * based on a supertype or the supertype can't be found on the project's IPS object path.
     * 
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws CoreException if an error occurs while searching for the supertype.
     */
    public IProductCmptType findSuperProductCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns all not derived associations from this type and its super types.
     */
    // TODO AW 18-10-2011: Finder missing IPS project
    public List<IAssociation> findAllNotDerivedAssociations() throws CoreException;

    /**
     * Returns the type's attributes.
     */
    public List<IProductCmptTypeAttribute> getProductCmptTypeAttributes();

    /**
     * Returns the attribute with the given name defined in <strong>this</strong> type (This method
     * does not search the supertype hierarchy.) If more than one attribute with the name exist, the
     * first attribute with the name is returned. Returns <code>null</code> if no attribute with the
     * given name exists.
     */
    public IProductCmptTypeAttribute getProductCmptTypeAttribute(String name);

    /**
     * Searches an attribute with the given name in the type and it's supertype hierarchy and
     * returns it. Returns <code>null</code> if no such attribute exists.
     * 
     * @param name The attribute's name.
     * @param ipsProject The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @throws NullPointerException if project is <code>null</code>.
     * @throws CoreException if an error occurs while searching.
     */
    public IProductCmptTypeAttribute findProductCmptTypeAttribute(String name, IIpsProject ipsProject)
            throws CoreException;

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
     * Returns the table structure usages defined for this type or an empty array is no usage is
     * defined.
     */
    public List<ITableStructureUsage> getTableStructureUsages();

    /**
     * Returns the table structure usage with the given role name. If more than one table structure
     * usage with the role name exist, the first table structure usage with the role name is
     * returned. Returns <code>null</code> if no table structure usage with the given role name
     * exists.
     */
    public ITableStructureUsage getTableStructureUsage(String roleName);

    /**
     * Looks for the table structure usage with the specified roleName starting from this policy
     * component type and visiting up the supertype hierarchy. If no table structure usage is found,
     * <code>null</code> is returned.
     * 
     * @param roleName the role name of the ITableStructureUsage in question
     * @param project The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @return the ITableStructureUsage for the provided name or <code>null</code> if non is found
     */
    public ITableStructureUsage findTableStructureUsage(String roleName, IIpsProject project) throws CoreException;

    /**
     * Creates a new table usage and returns it.
     */
    public ITableStructureUsage newTableStructureUsage();

    /**
     * Moves the table structure usages identified by the indexes up or down by one position. If one
     * of the indexes is 0 (the first object), no object is moved up. If one of the indexes is the
     * number of objects - 1 (the last object) no object is moved down.
     * 
     * @param indexes The indexes identifying the table structure usages.
     * @param up <code>true</code>, to move the table structure usages up, <false> to move them
     *            down.
     * 
     * @return The new indexes of the moved table structure usages.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a table structure
     *             usage.
     */
    public int[] moveTableStructureUsage(int[] indexes, boolean up);

    /**
     * Returns a new product component type method. Typesafe version of <code>newMethod</code>.
     */
    public IProductCmptTypeMethod newProductCmptTypeMethod();

    /**
     * Returns a new product component type method that has the role of a formula signature. The
     * formula name is set to the given formula name and the method name is set to the default
     * method name.
     * 
     * @param formulaName The name of the formula signature.
     * 
     * @see IProductCmptTypeMethod#getDefaultMethodName()
     */
    public IProductCmptTypeMethod newFormulaSignature(String formulaName);

    /**
     * Returns the type's methods. Typesafe version of <code>getMethods()</code>.
     */
    public List<IProductCmptTypeMethod> getProductCmptTypeMethods();

    /**
     * Returns the type's associations. Typesafe version of <code>getAssociations()</code>.
     */
    public List<IProductCmptTypeAssociation> getProductCmptTypeAssociations();

    /**
     * Returns the methods of this type which are no formula signatures.
     */
    public List<IProductCmptTypeMethod> getNonFormulaProductCmptTypeMethods();

    /**
     * Returns the method signature with the indicates formula name. Returns <code>null</code> if no
     * such method is found in <strong>this</strong> type. The type hierarchy is not searched.
     * Returns <code>null</code> if formulaName is <code>null</code>.
     */
    public IProductCmptTypeMethod getFormulaSignature(String formulaName);

    /**
     * Returns all method signatures of this product component type neglecting the type hierarchy.
     * Returns an empty array if no formula signature is defined for this type.
     */
    public List<IProductCmptTypeMethod> getFormulaSignatures();

    /**
     * Searches the method signature with the indicated formula name in the type's supertype
     * hierarchy. Returns <code>null</code> if no such method is found.
     * 
     * @param formulaName The formula name to search
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching.
     * @throws NullPointerException if IPS project is <code>null</code>.
     */
    public IProductCmptTypeMethod findFormulaSignature(String formulaName, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the formula signatures of formulas in the supertype hierarchy that are overloaded by
     * formulas of this type.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public List<IProductCmptTypeMethod> findSignaturesOfOverloadedFormulas(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the type's product definition properties.
     * 
     * @param searchSupertypeHierarchy Flag indicating whether the supertype hierarchy shall be
     *            searched as well
     */
    public List<IProductCmptProperty> findProductCmptProperties(boolean searchSupertypeHierarchy, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns the product definition property with the given name and type. If no such property is
     * found in the type itself, the supertype hierarchy is searched.
     */
    public IProductCmptProperty findProductCmptProperty(ProductCmptPropertyType type,
            String propName,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the product definition property with the given name. If no such property is found in
     * the type itself, the supertype hierarchy is searched.
     */
    public IProductCmptProperty findProductCmptProperty(String propName, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns a list containing the {@link IProductCmptProperty}s of the indicated
     * {@link IProductCmptCategory} in the referenced order.
     * 
     * @param category The {@link IProductCmptCategory} to search the {@link IProductCmptProperty}s
     *            for
     * @param searchSupertypeHierarchy Flag indicating whether the supertype hierarchy shall be
     *            included
     * @param ipsProject The {@link IIpsProject} whose {@link IIpsObjectPath} is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public List<IProductCmptProperty> findProductCmptPropertiesForCategory(IProductCmptCategory category,
            boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) throws CoreException;

    /**
     * Returns <code>true</code> if the user has configured a custom icon for enabled instances of
     * this type, <code>false</code> otherwise.
     */
    public boolean isUseCustomInstanceIcon();

    /**
     * Returns the string-presentation of the path of an Icon file used for enabled instances of
     * this type. This method may return a valid path even though no custom Icon is configured.
     */
    public String getInstancesIcon();

    /**
     * Configures this {@link ProductCmptType} to use the icon at the given path as icon for enabled
     * instances.
     */
    public void setInstancesIcon(String path);

    /**
     * @see #searchMetaObjectSrcFiles(boolean)
     */
    public Collection<IIpsSrcFile> searchProductComponents(boolean includeSubtypes) throws CoreException;

    /**
     * Creates and returns a new {@link IProductCmptCategory} that belongs to this type.
     */
    public IProductCmptCategory newCategory();

    /**
     * Creates and returns a new {@link IProductCmptCategory} with the provided name, belonging to
     * this type.
     * 
     * @param name The name of the category to create
     */
    public IProductCmptCategory newCategory(String name);

    /**
     * Returns a list (defensive copy) containing the {@link IProductCmptCategory}s belonging to
     * this type.
     * <p>
     * This method does <strong>not</strong> consider categories defined in the supertype hierarchy.
     */
    public List<IProductCmptCategory> getCategories();

    /**
     * Returns a list containing the {@link IProductCmptCategory}s belonging to this type.
     * <p>
     * This method <strong>does</strong> consider categories defined in the supertype hierarchy.
     * Categories from supertypes are located at the top of the list.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy
     */
    public List<IProductCmptCategory> findCategories(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the {@link IProductCmptCategory} identified by the given name or null if no such
     * category is found.
     * <p>
     * This method does <strong>not</strong> consider categories defined in the supertype hierarchy.
     * 
     * @param name The name identifying the {@link IProductCmptCategory} to be retrieved
     */
    public IProductCmptCategory getCategory(String name);

    /**
     * Returns the first {@link IProductCmptCategory} of the provided {@link Position}.
     * <p>
     * Returns null if no {@link IProductCmptCategory} with the provided {@link Position} is defined
     * in this {@link IProductCmptType}.
     * <p>
     * This operation does <strong>not</strong> consider {@link IProductCmptCategory}s defined in
     * the supertype hierarchy.
     * 
     * @param position the {@link Position} to retrieve the first {@link IProductCmptCategory} for
     */
    public IProductCmptCategory getFirstCategory(Position position);

    /**
     * Returns the last {@link IProductCmptCategory} of the provided {@link Position}.
     * <p>
     * Returns null if no {@link IProductCmptCategory}s with the provided {@link Position} is
     * defined in this {@link IProductCmptType}.
     * <p>
     * This operation does <strong>not</strong> consider {@link IProductCmptCategory}s defined in
     * the supertype hierarchy.
     * 
     * @param position the {@link Position} to retrieve the last {@link IProductCmptCategory} for
     */
    public IProductCmptCategory getLastCategory(Position position);

    /**
     * Returns whether the indicated {@link IProductCmptCategory} is the first
     * {@link IProductCmptCategory} at it's {@link Position}.
     * 
     * @param category the {@link IProductCmptCategory} to check whether it is the first
     *            {@link IProductCmptCategory} of it's {@link Position}
     */
    public boolean isFirstCategory(IProductCmptCategory category);

    /**
     * Returns whether the indicated {@link IProductCmptCategory} is the last
     * {@link IProductCmptCategory} at it's {@link Position}.
     * 
     * @param category the {@link IProductCmptCategory} to check whether it is the last
     *            {@link IProductCmptCategory} of it's {@link Position}
     */
    public boolean isLastCategory(IProductCmptCategory category);

    /**
     * Returns whether the indicated {@link IProductCmptCategory} can be moved up.
     * <p>
     * This is the case if the {@link IProductCmptCategory} is defined in this
     * {@link IProductCmptType} and it is <strong>not</strong> the first
     * {@link IProductCmptCategory} of it's {@link Position}.
     * 
     * @param category The {@link IProductCmptCategory} to check whether it can be moved up
     */
    public boolean canMoveCategoryUp(IProductCmptCategory category);

    /**
     * Returns whether the indicated {@link IProductCmptCategory} can be moved down.
     * <p>
     * This is the case if the {@link IProductCmptCategory} is defined in this
     * {@link IProductCmptType} and it is <strong>not</strong> the last {@link IProductCmptCategory}
     * of it's {@link Position}.
     * 
     * @param category The {@link IProductCmptCategory} to check whether it can be moved down
     */
    public boolean canMoveCategoryDown(IProductCmptCategory category);

    /**
     * Returns the {@link IProductCmptCategory} that precedes the indicated
     * {@link IProductCmptCategory} or null if no predecessor {@link IProductCmptCategory} exists.
     * <p>
     * The preceding {@link IProductCmptCategory} is the {@link IProductCmptCategory} with the next
     * smaller index than that of the given {@link IProductCmptCategory} while featuring the same
     * {@link Position}.
     * <p>
     * This operation does <strong>not</strong> consider {@link IProductCmptCategory}s defined in
     * the supertype hierarchy.
     * 
     * @param category the {@link IProductCmptCategory} to find the predecessor
     *            {@link IProductCmptCategory} for
     */
    public IProductCmptCategory getPredecessorCategory(IProductCmptCategory category);

    /**
     * Returns the {@link IProductCmptCategory} that succeeds the indicated
     * {@link IProductCmptCategory} or null if no successor {@link IProductCmptCategory} exists.
     * <p>
     * The succeeding {@link IProductCmptCategory} is the {@link IProductCmptCategory} with the next
     * greater index than that of the given {@link IProductCmptCategory} while featuring the same
     * {@link Position}.
     * <p>
     * This operation does <strong>not</strong> consider {@link IProductCmptCategory}s defined in
     * the supertype hierarchy.
     * 
     * @param category the {@link IProductCmptCategory} to find the successor
     *            {@link IProductCmptCategory} for
     */
    public IProductCmptCategory getSuccessorCategory(IProductCmptCategory category);

    /**
     * Returns whether an {@link IProductCmptCategory} with the given name exists in this
     * {@link IProductCmptType}.
     * 
     * @param name The name of the {@link IProductCmptCategory} to check for existence in this type
     * 
     * @see #findHasCategory(String, IIpsProject)
     */
    public boolean hasCategory(String name);

    /**
     * Returns whether an {@link IProductCmptCategory} with the given name exists in this
     * {@link IProductCmptType} or it's supertype hierarchy.
     * 
     * @param name The name of the {@link IProductCmptCategory} to check for existence in this type
     *            and it's supertypes
     * @param ipsProject The {@link IIpsProject} whose {@link IIpsObjectPath} is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     * 
     * @see #hasCategory(String)
     */
    public boolean findHasCategory(String name, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the {@link IProductCmptCategory} identified by the given name or null if no such
     * category is found.
     * <p>
     * This method <strong>does</strong> consider categories defined in the supertype hierarchy.
     * 
     * @param name The name identifying the {@link IProductCmptCategory} to be retrieved
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public IProductCmptCategory findProductCmptCategory(String name, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the first {@link IProductCmptCategory} marked as default for
     * {@link IProductCmptTypeMethod}s marked as formula signature definition or null if no such
     * category is found.
     * <p>
     * This method <strong>does</strong> consider categories defined in the supertype hierarchy.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public IProductCmptCategory findDefaultCategoryForFormulaSignatureDefinitions(IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns the first {@link IProductCmptCategory} marked as default for product relevant
     * {@link IValidationRule}s or null if no such category is found.
     * <p>
     * This method <strong>does</strong> consider categories defined in the supertype hierarchy.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public IProductCmptCategory findDefaultCategoryForValidationRules(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the first {@link IProductCmptCategory} marked as default for
     * {@link ITableStructureUsage}s or null if no such category is found.
     * <p>
     * This method <strong>does</strong> consider categories defined in the supertype hierarchy.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public IProductCmptCategory findDefaultCategoryForTableStructureUsages(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the first {@link IProductCmptCategory} marked as default for product relevant
     * {@link IPolicyCmptTypeAttribute}s or null if no such category is found.
     * <p>
     * This method <strong>does</strong> consider categories defined in the supertype hierarchy.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public IProductCmptCategory findDefaultCategoryForPolicyCmptTypeAttributes(IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns the first {@link IProductCmptCategory} marked as default for
     * {@link IProductCmptTypeAttribute}s or null if no such category is found.
     * <p>
     * This method <strong>does</strong> consider categories defined in the supertype hierarchy.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public IProductCmptCategory findDefaultCategoryForProductCmptTypeAttributes(IIpsProject ipsProject)
            throws CoreException;

    /**
     * Moves the indicated {@link IProductCmptCategory}s up or down by one position and returns
     * whether a move has been performed.
     * <p>
     * Returns false if
     * <ul>
     * <li>the first {@link IProductCmptCategory} of it's {@link Position} is moved up
     * <li>the last {@link IProductCmptCategory} of it's {@link Position} is moved down
     * </ul>
     * 
     * @param categories the {@link IProductCmptCategory}s to be moved
     * @param up flag indicating whether to move up or down
     * 
     * @throws IllegalArgumentException if one of the {@link IProductCmptCategory}s to be moved is
     *             not defined in this {@link IProductCmptType}
     */
    public boolean moveCategories(List<IProductCmptCategory> categories, boolean up);

}
