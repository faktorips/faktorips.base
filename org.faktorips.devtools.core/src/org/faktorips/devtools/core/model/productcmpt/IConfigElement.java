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

package org.faktorips.devtools.core.model.productcmpt;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
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
public interface IConfigElement extends IIpsObjectPart, IPropertyValue, IValueSetOwner {

    public final static String PROPERTY_TYPE = "type"; //$NON-NLS-1$
    public final static String PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE = "policyCmptTypeAttribute"; //$NON-NLS-1$
    public final static String PROPERTY_VALUE = "value"; //$NON-NLS-1$
    public static final String PROPERTY_VALUE_SET = "valueSet"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "CONFIGELEMENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute the config element is based can't be
     * found.
     */
    public final static String MSGCODE_UNKNWON_ATTRIBUTE = MSGCODE_PREFIX + "UnknownAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute's datatype can't be found and so the
     * value can't be parsed.
     */
    public final static String MSGCODE_UNKNOWN_DATATYPE_VALUE = MSGCODE_PREFIX + "UnknownDatatypeValue"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the datatype is invalid. (E.g. the definition of a
     * dynamic datatype can be wrong.)
     */
    public final static String MSGCODE_INVALID_DATATYPE = MSGCODE_PREFIX + "InvalidDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value can't be parsed, it is not an instance of
     * the datatype
     */
    public final static String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX + "ValueNotParsable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not contained in the valueset.
     */
    public final static String MSGCODE_VALUE_NOT_IN_VALUESET = MSGCODE_PREFIX + "ValueNotInValueSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value-set of the attribute (in the model) is
     * invalid.
     */
    public final static String MSGCODE_UNKNWON_VALUESET = MSGCODE_PREFIX + "InvalidAttirbuteValueSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the config'elements value set is not of a type that
     * conforms to the specification in the model. E.g. if in the model (=the policy component
     * type's attribute) the value set type
     * <code>UNRESTRICTED<code> is specified, then in the config element all types
     * are allowd. If <code>RANGE</code> is specified in the model, then only <code>RANGE</code> is
     * allowed in the config element.
     */
    public final static String MSGCODE_VALUESET_TYPE_MISMATCH = MSGCODE_PREFIX + "ValueSetTypeMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the config'elements value set is not a subset of the
     * value set defined in the model.
     */
    public final static String MSGCODE_VALUESET_IS_NOT_A_SUBSET = MSGCODE_PREFIX + "ValueSetIsNotASubset"; //$NON-NLS-1$

    /**
     * Returns the product component this config element belongs to.
     */
    public IProductCmpt getProductCmpt();

    /**
     * Returns the product component generation this config element belongs to.
     */
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
    public IValueSet getValueSet();

    /**
     * This method is defined in {@link IValueSetOwner}. It is also added to this interface to
     * provide more detailed documentation.
     * <p>
     * Returns the value set types that are allowed for the element's value set. If the attribute's
     * value set this config element configures is unrestricted, all value set types allowed by the
     * project are returned. Otherwise the attribute's value set type is returned. If the attribute
     * does not exist, this method returns the type of the current config element's value set.
     * 
     * @throw CoreException if an error occurs.
     * 
     * @see IIpsProject#getValueSetTypes(ValueDatatype)
     * @see IPolicyCmptTypeAttribute#getValueSet()
     * @see IValueSet#isUnrestricted()
     */
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the type of the value set defining the values valid for this config element. If the type
     * of the currently existing value set is the same as the one to set, all old informations (e.g.
     * bounds and step for a range value set) are removed.
     * 
     * @throws OperationNotSupportedException if this element ist of type PRODUCT_ATTRIBUTE because
     *             config elements of this type does not support own value sets.
     */
    public void setValueSetType(ValueSetType type);

    /**
     * Finds the corresponding attribute in the product component type this product component is an
     * instance of.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @return the corresponding attribute or <code>null</code> if no such attribute exists.
     * 
     * @throws CoreException if an exception occurs while searching for the attribute.
     */
    public IPolicyCmptTypeAttribute findPcTypeAttribute(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the element's value datatype, or <code>null</code> if it can't be found. The config
     * element's datatype is the attribute's datatype the element is based on.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a copy of the given value set and applies this copy to this config element.
     */
    public void setValueSetCopy(IValueSet source);

}
