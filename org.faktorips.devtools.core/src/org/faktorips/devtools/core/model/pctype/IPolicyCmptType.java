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

package org.faktorips.devtools.core.model.pctype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PersistentTypeInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypeHierarchy;

/**
 * The policy component type represents a Java class that is part of a policy class model.
 */
public interface IPolicyCmptType extends IType {

    /**
     * The name of the "configurableByProductComponentType" property.
     */
    public final static String PROPERTY_CONFIGURABLE_BY_PRODUCTCMPTTYPE = "configurableByProductCmptType"; //$NON-NLS-1$

    /**
     * The name of the product component type property.
     */
    public final static String PROPERTY_PRODUCT_CMPT_TYPE = "productCmptType"; //$NON-NLS-1$

    /**
     * The name of the super type property.
     */
    public final static String PROPERTY_SUPERTYPE = "supertype"; //$NON-NLS-1$

    /**
     * The name of the abstract property.
     */
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    /**
     * The name of the abstract property.
     */
    public final static String PROPERTY_FORCE_GENERATION_OF_EXTENSION_CU = "forceExtensionCompilationUnitGeneration"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "POLICYCMPTTYPE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined as
     * configurable by product, but the product component type name is not set.
     */
    public final static String MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSING = MSGCODE_PREFIX + "ProductCmptTypeNameMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined as
     * configurable by product, but the product component type name is not set.
     */
    public final static String MSGCODE_PRODUCT_CMPT_TYPE_NOT_FOUND = MSGCODE_PREFIX + "ProductCmptTypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this policy component type is defined configurable
     * by product and the product type name is the same as this type's name.
     */
    public final static String MSGCODE_PRODUCT_CMPT_TYPE_NAME_MISSMATCH = MSGCODE_PREFIX
            + "ProductCmptTypeNameMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a policy component type defined a product component
     * type as configuring it, but the referenced product component type configures another policy
     * component type or none at all.
     */
    public final static String MSGCODE_PRODUCT_CMPT_TYPE_DOES_NOT_CONFIGURE_THIS_TYPE = MSGCODE_PREFIX
            + "ProductCmptTypeDoesNotConfigureThisType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the product relevant flag is set but the super type
     * is not product relevant.
     */
    public final static String MSGCODE_SUPERTYPE_NOT_PRODUCT_RELEVANT_IF_THE_TYPE_IS_PRODUCT_RELEVANT = MSGCODE_PREFIX
            + "SupertypeNotProductRelevantIfTheTypeIsProductRelevant"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this class needs to be configurable if the super
     * type is configurable.
     */
    public final static String MSGCODE_SUPERTYPE_CONFIGURABLE_FORCES_THIS_TYPE_IS_CONFIGURABLE = MSGCODE_PREFIX
            + "SupertypeProductRelevantForcesThisTypeIsProductRelevant"; //$NON-NLS-1$

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
     * @throws CoreException if an error occurs while searching for the type.
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

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
     * Returns a list (defensive copy) containing the {@link IPolicyCmptTypeAttribute}s of this type
     * marked as product relevant.
     */
    public List<IPolicyCmptTypeAttribute> getProductRelevantPolicyCmptTypeAttributes();

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
     * @throws CoreException if an error occurs while searching.
     */
    public IPolicyCmptTypeAttribute findPolicyCmptTypeAttribute(String name, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Creates a new attribute and returns it.
     */
    public IPolicyCmptTypeAttribute newPolicyCmptTypeAttribute();

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
     * @param up <code>true</code>, to move the attributes up, <false> to move them down.
     * 
     * @return The new indexes of the moved attributes.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify an attribute.
     */
    @Override
    public int[] moveAttributes(int[] indexes, boolean up);

    /**
     * Returns an array of all attributes of all super types not yet overwritten by this policy
     * component type.
     * 
     * @param ipsProject TODO
     */
    public List<IPolicyCmptTypeAttribute> findOverrideAttributeCandidates(IIpsProject ipsProject) throws CoreException;

    /**
     * Creates new attributes in this type overriding the given attributes. Note that it is not
     * checked, if the attributes really belong to one of the type's super types.
     * 
     * @return The created attributes.
     */
    public List<IPolicyCmptTypeAttribute> overrideAttributes(List<IPolicyCmptTypeAttribute> attributes);

    /**
     * Returns the type's validation rules.
     */
    public List<IValidationRule> getValidationRules();

    /**
     * Returns a list (defensive copy) containing the {@link IValidationRule}s of this type marked
     * as configurable by product component.
     */
    public List<IValidationRule> getConfigurableValidationRules();

    /**
     * Returns this type's validation rule with the given name or <code>null</code> if no rule
     * exists with this name.
     * <p>
     * Use {@link #findAllValidationRules(IIpsProject)} to search the super-type hierarchy.
     */
    public IValidationRule getValidationRule(String ruleName);

    /**
     * Returns all {@link IValidationRule}s defined by this type and its super-types.
     * 
     * @throws CoreException if an error occurs while searching
     */
    public List<IValidationRule> findAllValidationRules(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the {@link IValidationRule} with the given name, or <code>null</code> if no such rule
     * is defined by this type and its super-types.
     * 
     * @throws CoreException if an error occurs while searching
     */
    public IValidationRule findValidationRule(String ruleName, IIpsProject ipsProject) throws CoreException;

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
     * @param up <code>true</code>, to move the rules up, <false> to move them down.
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
     * @throws CoreException if an error occurs while searching the super type hierarchy.
     */
    public boolean isAggregateRoot() throws CoreException;

    /**
     * Returns <code>true</code> if this not an aggregate root, otherwise <code>false</code>.
     */
    public boolean isDependantType() throws CoreException;

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
     * @param up <code>true</code>, to move the relations up, <false> to move them down.
     * 
     * @return The new indexes of the moved relations.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a relation.
     */
    @Override
    public int[] moveAssociations(int[] indexes, boolean up);

    /**
     * Creates a new super type hierarchy for the type and returns it.
     */
    public ITypeHierarchy getSupertypeHierarchy() throws CoreException;

    /**
     * Creates a new sub type hierarchy for the type and returns it.
     */
    public ITypeHierarchy getSubtypeHierarchy() throws CoreException;

    /**
     * Initializes the persistence metadata (like table, column and discriminator information) with
     * suitable defaults.
     * 
     * @throws CoreException if the corresponding IPS Project does not have persistence support
     *             enabled.
     */
    public void initPersistentTypeInfo() throws CoreException;

    /**
     * Returns the information about how to persist this policy component type into a relational
     * database table.
     * 
     * @return <code>null</code> if the persistence information is not available, e.g. when the
     *         corresponding IPS project this type belongs to does not support persistence.
     * 
     * @see org.faktorips.devtools.core.model.ipsproject.IIpsProject#isPersistenceSupportEnabled
     */
    public IPersistentTypeInfo getPersistenceTypeInfo();

    /**
     * Returns true if the policy component type should persistent. Returns <code>false</code> if
     * the type shouldn't persists, and therefore no persistent type info is necessary.
     * 
     * @see #getPersistenceTypeInfo()
     * @see PersistentTypeInfo#isEnabled()
     */
    public boolean isPersistentEnabled();

}
