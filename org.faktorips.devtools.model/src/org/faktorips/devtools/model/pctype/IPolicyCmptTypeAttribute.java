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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.validation.GenericRelevanceValidation;

/**
 * A policy component type's attribute.
 */
public interface IPolicyCmptTypeAttribute extends IAttribute, IValueSetOwner, IProductCmptProperty {

    // property names
    public static final String PROPERTY_COMPUTATION_METHOD_SIGNATURE = "computationMethodSignature"; //$NON-NLS-1$
    public static final String PROPERTY_ATTRIBUTE_TYPE = "attributeType"; //$NON-NLS-1$
    public static final String PROPERTY_PRODUCT_RELEVANT = "productRelevant"; //$NON-NLS-1$
    public static final String PROPERTY_VALUESET_CONFIGURED_BY_PRODUCT = "valueSetConfiguredByProduct"; //$NON-NLS-1$
    public static final String PROPERTY_RELEVANCE_CONFIGURED_BY_PRODUCT = "relevanceConfiguredByProduct"; //$NON-NLS-1$
    public static final String PROPERTY_GENERIC_VALIDATION = "genericValidation"; //$NON-NLS-1$

    public static final String PROPERTY_FORMULAPARAM_NAME = "param.name"; //$NON-NLS-1$
    public static final String PROPERTY_FORMULAPARAM_DATATYPE = "param.datatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute can't be product relevant if the type
     * is configurable by product.
     */
    public static final String MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT = IAttribute.MSGCODE_PREFIX
            + "AttributeCantBeProductRelevantIfTypeIsNot"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the computation method has a different data type.
     */
    public static final String MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE = IAttribute.MSGCODE_PREFIX
            + "CompuationMethodHasWrongDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the computation method must be specified but it is
     * not.
     */
    public static final String MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED = IAttribute.MSGCODE_PREFIX
            + "ComputationMethodNotSpecified"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the computation method is specified but does not
     * exist.
     */
    public static final String MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST = IAttribute.MSGCODE_PREFIX
            + "ComputationMethodDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but has a different
     * attribute type (which is not allowed).
     */
    public static final String MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE = IAttribute.MSGCODE_PREFIX
            + "OverwrittenAttributeDifferentType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the currently configured value set is not allowed
     * for this attribute.
     */
    public static final String MSGCODE_ILLEGAL_VALUESET_TYPE = IAttribute.MSGCODE_PREFIX + "IllegalValueSetType"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a derived value set is not allowed for a product
     * relevant attribute.
     */
    public static final String MSGCODE_PRODUCT_RELEVANT_ATTRIBUTE_CAN_NOT_HAVE_DERIVED_VALUE_SET = IAttribute.MSGCODE_PREFIX
            + "ProductRelevantAttributeCanNotHaveDerivedValueSet"; //$NON-NLS-1$ ;

    /**
     * Validation message code to indicate that a constant attribute can't use an abstract datatype.
     */
    public static final String MSGCODE_CONSTANT_CANT_BE_ABSTRACT = IAttribute.MSGCODE_PREFIX + "ConstantCantBeAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an abstract datatype attribute can't be product
     * relevant.
     */
    public static final String MSGCODE_ABSTRACT_CANT_BE_PRODUCT_RELEVANT = IAttribute.MSGCODE_PREFIX
            + "AbstractCantBeProductRelevant"; //$NON-NLS-1$

    /**
     * Returns the policy component type this attribute belongs to.
     */
    public IPolicyCmptType getPolicyCmptType();

    /**
     * If this Attribute has a rule that checks against its value set, it is returned by this method
     * otherwise <code>null</code> will be returned.
     */
    public IValidationRule findValueSetRule(IIpsProject ipsProject);

    /**
     * Returns a proposal for the name of the rule checking the value against the value set.
     */
    public String getProposalValueSetRuleName();

    /**
     * Returns a proposal for the message code if the rule checking the value against the value set.
     */
    public String getProposalMsgCodeForValueSetRule();

    /**
     * Creates a IValidationRule for this IAttribute. In the generated code the corresponding rule
     * is supposed to validate the value of the corresponding Attribute. There can only exists one
     * rule of this kind for this attribute. If a rule already exists the call to this method
     * returns it and doesn't create a new one.
     */
    public IValidationRule createValueSetRule();

    /**
     * Deletes the IValidationRule that is responsible for checking the value against the value set
     * of this attribute. If no such rule was created for this attribute this method does nothing.
     */
    public void deleteValueSetRule();

    /**
     * Returns the attribute's type.
     */
    public AttributeType getAttributeType();

    /**
     * Sets the attribute's type.
     */
    public void setAttributeType(AttributeType newType);

    /**
     * Returns true if the attribute type is changeable.
     */
    public boolean isChangeable();

    /**
     * Returns <code>true</code> if this attribute is derived, otherwise <code>false</code>.
     */
    @Override
    public boolean isDerived();

    /**
     * Returns {@code true} if this attribute is product relevant. If the attribute is product
     * relevant, the set of allowed values and the default value can be defined in product
     * components that are based on the associated product component type.
     * <p>
     * How this configuration happens depends on {@link #isValueSetConfiguredByProduct()} and
     * {@link #isRelevanceConfiguredByProduct()}.
     * 
     * @see IPolicyCmptType#getProductCmptType()
     */
    public boolean isProductRelevant();

    /**
     * Returns whether the {@linkplain IValueSet value set} can be configured explicitly in the
     * product configuration.
     */
    public boolean isValueSetConfiguredByProduct();

    /**
     * Returns whether the relevance of this attribute can be configured in the product
     * configuration by implicitly modifying the allowed {@linkplain IValueSet value set} to
     * in-/exclude {@code null} (optional/obligatory) or disallow all values (irrelevant,
     * {@link IValueSet#isEmpty()}).
     */
    public boolean isRelevanceConfiguredByProduct();

    /**
     * Sets whether this attribute is product relevant or not.
     * 
     * @deprecated since 20.6 use {@link #setValueSetConfiguredByProduct(boolean)} and/or
     *             {@link #setRelevanceConfiguredByProduct(boolean)} instead
     */
    @Deprecated
    public void setProductRelevant(boolean newValue);

    /**
     * Sets whether the {@linkplain IValueSet value set} can be configured explicitly in the product
     * configuration.
     */
    public void setValueSetConfiguredByProduct(boolean valueSetConfiguredByProduct);

    /**
     * Sets whether the relevance of this attribute can be configured in the product configuration
     * by implicitly modifying the allowed {@linkplain IValueSet value set} to in-/exclude
     * {@code null} (optional/obligatory) or disallow all values (irrelevant,
     * {@link IValueSet#isEmpty()}).
     */
    public void setRelevanceConfiguredByProduct(boolean relevanceConfiguredByProduct);

    /**
     * Returns the name of the method computing the value for this attribute. The attribute must be
     * a derived one, that is computed on the fly. The method returns an empty String if this is not
     * the case.
     */
    public String getComputationMethodSignature();

    /**
     * Sets the name of the method computing this attribute.
     */
    public void setComputationMethodSignature(String newMethodName);

    /**
     * If this is a derived attribute which value is derived by a method defined in the product
     * component type, this method is search and returned.
     * 
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IProductCmptTypeMethod findComputationMethod(IIpsProject ipsProject) throws CoreException;

    /**
     * This method is defined in {@link IValueSetOwner}. It is also added to this interface to
     * provide more detailed documentation.
     * 
     * For policy component type attributes the allowed values set types are the types returned by
     * {@link IIpsProject#getValueSetTypes(org.faktorips.datatype.ValueDatatype)} using the
     * attribute's data type. However if the data type is defined by an {@link IEnumType} with
     * values stored in a separate content, then only {@link ValueSetType#UNRESTRICTED} is allowed.
     * 
     * @throws CoreException if an error occurs.
     */
    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a copy of the given value set and applies this copy to this attribute.
     */
    public void setValueSetCopy(IValueSet source);

    /**
     * Returns the object containing information about how to persist this policy component type
     * attribute into a relational database table.
     * 
     * @return <code>null</code> if the persistence information is not available, e.g. when the
     *         corresponding IPS project this type belongs to does not support persistence.
     * 
     * @see org.faktorips.devtools.model.ipsproject.IIpsProject#isPersistenceSupportEnabled
     */
    public IPersistentAttributeInfo getPersistenceAttributeInfo();

    /**
     * Returns whether generic validation should be generated for this attribute.
     *
     * @return {@code true} if generic validation is enabled for this attribute, otherwise
     *         {@code false}
     * @see GenericRelevanceValidation
     */
    boolean isGenericValidationEnabled();

    /**
     * Sets whether generic validation should be generated for this attribute.
     *
     * @param genericValidationEnabled whether generic validation should be generated for this
     *            attribute
     * @see GenericRelevanceValidation
     */
    void setGenericValidationEnabled(boolean genericValidationEnabled);
}
