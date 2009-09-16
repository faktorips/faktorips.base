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

package org.faktorips.devtools.core.model.pctype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * A policy component type's attribute.
 */
public interface IPolicyCmptTypeAttribute extends IAttribute, IValueSetOwner, IProdDefProperty {

    // property names
    public final static String PROPERTY_COMPUTATION_METHOD_SIGNATURE = "computationMethodSignature"; //$NON-NLS-1$
    public final static String PROPERTY_ATTRIBUTE_TYPE = "attributeType"; //$NON-NLS-1$
    public final static String PROPERTY_PRODUCT_RELEVANT = "productRelevant"; //$NON-NLS-1$

    public final static String PROPERTY_FORMULAPARAM_NAME = "param.name"; //$NON-NLS-1$
    public final static String PROPERTY_FORMULAPARAM_DATATYPE = "param.datatype"; //$NON-NLS-1$
    public final static String PROPERTY_OVERWRITES = "overwrite"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute can't be product relevant if the type
     * is configurable by product.
     */
    public final static String MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT = MSGCODE_PREFIX
            + "AttributeCantBeProductRelevantIfTypeIsNot"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute is marked overwriting an attribute in
     * the supertype hierarchy, but there is no such attribute.
     */
    public final static String MSGCODE_NOTHING_TO_OVERWRITE = MSGCODE_PREFIX + "NothingToOverwrite"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the compuation method has a different datatype.
     */
    public final static String MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE = MSGCODE_PREFIX
            + "CompuationMethodHasWrongDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the computation method must be specified but it is
     * not.
     */
    public final static String MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED = MSGCODE_PREFIX
            + "ComputationMethodNotSpecified"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the computation method is specified but does not
     * exist.
     */
    public final static String MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "ComputationMethodDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrittes another but has a different
     * attribute type (which is not allowed).
     */
    public final static String MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE = MSGCODE_PREFIX
            + "ComputationMethodDoesNotExist"; //$NON-NLS-1$

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
     * Deletes the IValidationRule that is reponsible for checking the value angainst the value set
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
    public boolean isDerived();

    /**
     * Returns true if this attribute is product relevant, that means the product developer can
     * configure some aspect of the attribute.
     */
    public boolean isProductRelevant();

    /**
     * Sets if this attribute is product relevant or not.
     */
    public void setProductRelevant(boolean newValue);

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
     * @param ipsProject The project which ips object path is used to search.
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
     * attribute's datatype. However if the datatype is defined by an {@link IEnumType} with values
     * stored in a separate content, then only {@link ValueSetType#UNRESTRICTED} is allowed.
     * 
     * @throws CoreException if an error occurs.
     */
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a copy of the given value set and aplies this copy to this attribute.
     */
    public void setValueSetCopy(IValueSet source);

    /**
     * <code>true</code> to indicate that this attribute overwrites an attribute with the same name
     * somewerhe up the supertype hierarchy or <code>false</code> to let this attribute be a new
     * one.
     */
    public void setOverwrite(boolean overwrites);

    /**
     * Returns the first attribute found with the same name in the supertypes hierarchy or
     * <code>null</code> if no such attribute exists.
     * 
     * @param ipsProject The project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IPolicyCmptTypeAttribute findOverwrittenAttribute(IIpsProject ipsProject) throws CoreException;

}
