/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype;

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypeHierarchy;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;

/**
 * The policy component type represents a Java class that is part of a policy class model.
 */
public interface IPolicyCmptType extends IType {

    /**
     * The name of the "configurableByProductComponentType" property.
     */
    public static final String PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE = "configurableByProductCmptType"; //$NON-NLS-1$

    /**
     * The name of the "generateValidatorClass" property.
     */
    public static final String PROPERTY_GENERATE_VALIDATOR_CLASS = "generateValidatorClass"; //$NON-NLS-1$
    /**
     * The name of the product component type property.
     */
    public static final String PROPERTY_PRODUCT_CMPT_TYPE = "productCmptType"; //$NON-NLS-1$

    /**
     * The name of the abstract property.
     */
    public static final String PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU = "forceExtensionCompilationUnitGeneration"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "POLICYCMPTTYPE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined as
     * configurable by product, but the product component type name is not set.
     */
    public static final String MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING = MSGCODE_PREFIX + "ProductCmptTypeNameMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined as
     * configurable by product, but the product component type name is not set.
     */
    public static final String MSGCODE_PRODUCT_CMPT_TYPE_NOT_FOUND = MSGCODE_PREFIX + "ProductCmptTypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined configurable
     * by product and the product type name is the same as this type's name.
     */
    public static final String MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSMATCH = MSGCODE_PREFIX
            + "ProductCmptTypeNameMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a policy component type defined a product component
     * type as configuring it, but the referenced product component type configures another policy
     * component type or none at all.
     */
    public static final String MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE = MSGCODE_PREFIX
            + "ProductCmptTypeDoesNotConfigureThisType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product relevant flag is set but the super type
     * is not product relevant.
     */
    public static final String MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT = MSGCODE_PREFIX
            + "SupertypeNotProductRelevantIfTheTypeIsProductRelevant"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this class needs to be configurable if the super
     * type is configurable.
     */
    public static final String MSGCODE_SUPERTYPE_CONFIGURABLE_FORCES_THIS_TYPE_IS_CONFIGURABLE = MSGCODE_PREFIX
            + "SupertypeProductRelevantForcesThisTypeIsProductRelevant"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that not all classes in the hierarchy have the same
     * setting for creating a separate validator class.
     */
    public static final String MSGCODE_DIFFERENT_GENERATE_VALIDATOR_CLASS_SETTING_IN_HIERARCHY = MSGCODE_PREFIX
            + "DifferentGenerateValidatorClassSettingInHierarchy"; //$NON-NLS-1$

    /**
     * Returns <code>true</code> if this class has a corresponding product component type, otherwise
     * <code>false</code>.
     */
    public boolean isConfigurableByProductCmptType();

    /**
     * Sets if this policy component type has a corresponding product component type or not.
     */
    public void setConfigurableByProductCmptType(boolean newValue);

    /**
     * Returns whether this policy component type is configured to delegate validation to a separate
     * validation class.
     */
    public boolean isGenerateValidatorClass();

    /**
     * Configure this policy component type to generate a separate validation class and delegate
     * validation to that class.
     */
    public void setGenerateValidatorClass(boolean newValue);

    /**
     * Returns the qualified name of the product component type. Returns an empty string if this
     * policy component type has no corresponding product component type.
     */
    public String getProductCmptType();

    /**
     * Returns the product component type this type refers to. Returns <code>null</code> if either
     * this type does not refer to a product component type or the product component type can't be
     * found on the project's IPS object path.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject);

    /**
     * Sets the qualified name of the product component type that configures this type.
     */
    public void setProductCmptType(String qualifiedName);

    /**
     * Returns the qualified name of the type's super type. Returns an empty string if this type has
     * no super type.
     */
    @Override
    public String getSupertype();

    /**
     * Returns <code>true</code> if this type has a super type, otherwise <code>false</code>. This
     * method also returns <code>true</code> if the type refers to a super type but the super type
     * does not exist.
     */
    @Override
    public boolean hasSupertype();

    /**
     * Sets the type's super type.
     * 
     * @throws IllegalArgumentException if newSupertype is null.
     */
    @Override
    public void setSupertype(String newSupertype);

    /**
     * Sets the type's abstract property.
     */
    @Override
    public void setAbstract(boolean newValue);

    /**
     * Returns <code>true</code> if an extension compilation unit is generated whether it is
     * necessary because of the presence of none abstract methods or validation rules or not.
     */
    public boolean isForceExtensionCompilationUnitGeneration();

    /**
     * Sets if an extension compilation unit should be generated in any case.
     * <p>
     * The developer using FaktorIps can set this property, if he wants to override methods for
     * relation and attribute handling that are normally not overridden.
     */
    public void setForceExtensionCompilationUnitGeneration(boolean flag);

    /**
     * Returns <code>true</code> if an extension Java compilation unit should exists for policy
     * component type, where the developer using FaktorIps can add or override code. An extension
     * compilation unit exists in the following cases:
     * <ol>
     * <li>The policy component type has a none abstract method.
     * <li>The policy component type has a validation rule.
     * <li>The policy component type has a computed or derived attribute that is not product
     * relevant.
     * <li>The flag forceGenerationOfExtensionCompilationUnit is set.
     * </ol>
     * 
     * Returns <code>false</code> otherwise.
     */
    public boolean isExtensionCompilationUnitGenerated();

    /**
     * Returns the type's attributes.
     */
    public List<IPolicyCmptTypeAttribute> getPolicyCmptTypeAttributes();

    /**
     * Returns a list containing the {@link IProductCmptProperty IProductCmptPropertys} belonging to
     * this type.
     * 
     * @param propertyType The type of the properties to be retrieved or null to retrieve all
     *            properties regardless of their type
     * 
     * @throws IllegalArgumentException If the indicated property type identified properties that do
     *             not belong to policy component types
     */
    public List<IProductCmptProperty> getProductCmptProperties(ProductCmptPropertyType propertyType);

    /**
     * Returns the attribute with the given name defined in <strong>this</strong> type (This method
     * does not search the super type hierarchy.) If more than one attribute with the name exist,
     * the first attribute with the name is returned. Returns <code>null</code> if no attribute with
     * the given name exists.
     */
    public IPolicyCmptTypeAttribute getPolicyCmptTypeAttribute(String name);

    /**
     * Searches this type and it's super types for an attribute with the given name. If more than
     * one attribute with the name exist, the first attribute with the name is returned. Returns
     * <code>null</code> if no attribute with the given name is found.
     * 
     */
    public IPolicyCmptTypeAttribute findPolicyCmptTypeAttribute(String name, IIpsProject ipsProject);

    /**
     * Creates a new attribute and returns it.
     */
    public IPolicyCmptTypeAttribute newPolicyCmptTypeAttribute();

    /**
     * Creates and returns a new {@link IPolicyCmptTypeAttribute} with the given name.
     * 
     * @param name The name of the attribute to create
     */
    public IPolicyCmptTypeAttribute newPolicyCmptTypeAttribute(String name);

    /**
     * Returns the number of attributes.
     */
    @Override
    public int getNumOfAttributes();

    /**
     * Moves the attributes identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first attribute), no attribute is moved up. If one of the indexes is the
     * number of attributes - 1 (the last attribute) no attribute is moved down.
     * 
     * @param indexes The indexes identifying the attributes.
     * @param up <code>true</code>, to move the attributes up, <code>false</code> to move them down.
     * 
     * @return The new indexes of the moved attributes.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify an attribute.
     */
    @Override
    public int[] moveAttributes(int[] indexes, boolean up);

    /**
     * Returns the type's validation rules.
     */
    public List<IValidationRule> getValidationRules();

    /**
     * Returns this type's validation rule with the given name or <code>null</code> if no rule
     * exists with this name.
     * <p>
     * Use {@link #findAllValidationRules(IIpsProject)} to search the super-type hierarchy.
     */
    public IValidationRule getValidationRule(String ruleName);

    /**
     * Returns all {@link IValidationRule IValidationRules} defined by this type and its
     * super-types.
     * 
     */
    public List<IValidationRule> findAllValidationRules(IIpsProject ipsProject);

    /**
     * Returns the {@link IValidationRule} with the given name, or <code>null</code> if no such rule
     * is defined by this type and its super-types.
     * 
     */
    public IValidationRule findValidationRule(String ruleName, IIpsProject ipsProject);

    /**
     * Creates a new validation rule and returns it.
     */
    public IValidationRule newRule();

    /**
     * Returns the number of rules.
     */
    public int getNumOfRules();

    /**
     * Moves the rules identified by the indexes up or down by one position. If one of the indexes
     * is 0 (the first rule), no rule is moved up. If one of the indexes is the number of rules - 1
     * (the last rule) no rule is moved down.
     * 
     * @param indexes The indexes identifying the rules.
     * @param up <code>true</code>, to move the rules up, <code>false</code> to move them down.
     * 
     * @return The new indexes of the moved rules.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a rule.
     */
    public int[] moveRules(int[] indexes, boolean up);

    /**
     * Returns <code>true</code> if this class represents the root of a complex aggregate, otherwise
     * <code>false</code>. For example an insurance policy is complex aggregate that consist of a
     * policy class itself but also of coverages, insured persons, etc. In this case the policy
     * class is the root of the complex policy aggregate.
     * <p>
     * A policy component type is considered an aggregate root if it and it's super types havn't got
     * a reverse composite relation.
     * 
     * @throws IpsException if an error occurs while searching the super type hierarchy.
     */
    public boolean isAggregateRoot() throws IpsException;

    /**
     * Returns <code>true</code> if this not an aggregate root, otherwise <code>false</code>.
     */
    public boolean isDependantType() throws IpsException;

    /**
     * Returns the type's relations.
     */
    public List<IPolicyCmptTypeAssociation> getPolicyCmptTypeAssociations();

    /**
     * Creates a new association and returns it.
     */
    public IPolicyCmptTypeAssociation newPolicyCmptTypeAssociation();

    /**
     * Returns the number of relations.
     */
    @Override
    public int getNumOfAssociations();

    /**
     * Moves the relations identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first relation), no relation is moved up. If one of the indexes is the
     * number of relations - 1 (the last relation) no relation is moved down.
     * 
     * @param indexes The indexes identifying the relations.
     * @param up <code>true</code>, to move the relations up, <code>false</code> to move them down.
     * 
     * @return The new indexes of the moved relations.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a relation.
     */
    @Override
    public int[] moveAssociations(int[] indexes, boolean up);

    /**
     * Creates a new sub type hierarchy for the type and returns it.
     */
    @Override
    public ITypeHierarchy getSubtypeHierarchy() throws IpsException;

    /**
     * Initializes the persistence metadata (like table, column and discriminator information) with
     * suitable defaults.
     * 
     * @throws IpsException if the corresponding IPS Project does not have persistence support
     *             enabled.
     */
    public void initPersistentTypeInfo() throws IpsException;

    /**
     * Returns the information about how to persist this policy component type into a relational
     * database table.
     * 
     * @return <code>null</code> if the persistence information is not available, e.g. when the
     *         corresponding IPS project this type belongs to does not support persistence.
     * 
     * @see org.faktorips.devtools.model.ipsproject.IIpsProject#isPersistenceSupportEnabled
     */
    public IPersistentTypeInfo getPersistenceTypeInfo();

    /**
     * Returns true if the policy component type should be persistent. Returns <code>false</code> if
     * the type shouldn't persist, and therefore no persistent type info is necessary.
     * 
     * @see #getPersistenceTypeInfo()
     * @see IPersistentTypeInfo#isEnabled()
     */
    public boolean isPersistentEnabled();

}
