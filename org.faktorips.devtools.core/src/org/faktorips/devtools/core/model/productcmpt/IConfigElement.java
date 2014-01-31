/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.model.valueset.ValueSetType;

/**
 * A configuration element is based on an product component type's attribute.
 * <p>
 * For example a policy component could have a constant attribute interestRate. All product
 * components based on that policy component have a matching product attribute that stores the
 * concrete interest rate value.
 */
public interface IConfigElement extends IPropertyValue, IValueSetOwner, IDescribedElement {

    public static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$
    public static final String PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE = "policyCmptTypeAttribute"; //$NON-NLS-1$
    public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$
    public static final String PROPERTY_VALUE_SET = "valueSet"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "CONFIGELEMENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute the configuration element is based
     * can't be found.
     */
    public static final String MSGCODE_UNKNWON_ATTRIBUTE = MSGCODE_PREFIX + "UnknownAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute's data type can't be found and so the
     * value can't be parsed.
     */
    public static final String MSGCODE_UNKNOWN_DATATYPE_VALUE = MSGCODE_PREFIX + "UnknownDatatypeValue"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type is invalid. (E.g. the definition of a
     * dynamic data type can be wrong.)
     */
    public static final String MSGCODE_INVALID_DATATYPE = MSGCODE_PREFIX + "InvalidDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value can't be parsed, it is not an instance of
     * the data type
     */
    public static final String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX + "ValueNotParsable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not contained in the value set.
     */
    public static final String MSGCODE_VALUE_NOT_IN_VALUESET = MSGCODE_PREFIX + "ValueNotInValueSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value-set of the attribute (in the model) is
     * invalid.
     */
    public static final String MSGCODE_UNKNWON_VALUESET = MSGCODE_PREFIX + "InvalidAttirbuteValueSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the config'elements value set is not of a type that
     * conforms to the specification in the model. E.g. if in the model (=the policy component
     * type's attribute) the value set type
     * <code>UNRESTRICTED<code> is specified, then in the configuration element all types
     * are allowed. If <code>RANGE</code> is specified in the model, then only <code>RANGE</code> is
     * allowed in the configuration element.
     */
    public static final String MSGCODE_VALUESET_TYPE_MISMATCH = MSGCODE_PREFIX + "ValueSetTypeMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the config'elements value set is not a subset of the
     * value set defined in the model.
     */
    public static final String MSGCODE_VALUESET_IS_NOT_A_SUBSET = MSGCODE_PREFIX + "ValueSetIsNotASubset"; //$NON-NLS-1$

    /**
     * Returns the product component generation this configuration element belongs to.
     * 
     * @deprecated Config-elements can be used in a context other than product component
     *             generations. This method will then yield unexpected and or erroneous results. Use
     *             {@link #getParent()} instead.
     */
    @Deprecated
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Returns the name of the product component type's attribute this element is based on.
     */
    public String getPolicyCmptTypeAttribute();

    /**
     * Sets the name of the policy component type's attribute this element is based on.
     * 
     * @throws NullPointerException if name is <code>null</code>.
     */
    public void setPolicyCmptTypeAttribute(String name);

    /**
     * Returns the attribute's value.
     */
    public String getValue();

    /**
     * Sets the attribute's value.
     */
    public void setValue(String newValue);

    /**
     * Returns the set of allowed values.
     */
    @Override
    public IValueSet getValueSet();

    /**
     * This method is defined in {@link IValueSetOwner}. It is also added to this interface to
     * provide more detailed documentation.
     * <p>
     * Returns the value set types that are allowed for the element's value set. If the attribute's
     * value set this configuration element configures is unrestricted, all value set types allowed
     * by the project are returned. Otherwise the attribute's value set type is returned. If the
     * attribute does not exist, this method returns the type of the current configuration element's
     * value set.
     * 
     * @throw CoreException if an error occurs.
     * 
     * @see IIpsProject#getValueSetTypes(ValueDatatype)
     * @see IPolicyCmptTypeAttribute#getValueSet()
     * @see IValueSet#isUnrestricted()
     */
    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the type of the value set defining the values valid for this configuration element. If
     * the type of the currently existing value set is the same as the one to set, all old
     * informations (e.g. bounds and step for a range value set) are removed.
     */
    @Override
    public void setValueSetType(ValueSetType type);

    /**
     * Converts the current value set to an enumeration type value set. If the current value set
     * type already is an enumeration type, nothing is changed. If the data type of the value set is
     * a {@link BooleanDatatype} or a {@link PrimitiveBooleanDatatype}, the values true, false are
     * added to the value set enumeration; in case of {@link BooleanDatatype}, null is also added to
     * the set.
     * 
     * @return the new enumeration value set or the unchanged set if it has already been of type
     *         enumeration.
     * 
     * @since 3.9
     */
    public IEnumValueSet convertValueSetToEnumType();

    /**
     * Finds the corresponding attribute in the product component type this product component is an
     * instance of.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @return the corresponding attribute or <code>null</code> if no such attribute exists.
     * 
     * @throws CoreException if an exception occurs while searching for the attribute.
     */
    public IPolicyCmptTypeAttribute findPcTypeAttribute(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the element's value data type, or <code>null</code> if it can't be found. The
     * configuration element's data type is the attribute's data type the element is based on.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    @Override
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a copy of the given value set and applies this copy to this configuration element.
     */
    public void setValueSetCopy(IValueSet source);

    void setValueSet(IValueSet source);

}
