/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import java.util.List;

import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.internal.ValueToXmlHelper;

public interface IConfiguredValueSet extends IConfigElement, IValueSetOwner {

    String LEGACY_TAG_NAME = ValueToXmlHelper.XML_TAG_VALUE_SET;

    String TAG_NAME = ValueToXmlHelper.XML_TAG_CONFIGURED_VALUE_SET;

    String PROPERTY_VALUE_SET = "valueSet"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "CONFIGUREDVALUESET-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the config'elements value set is not of a type that
     * conforms to the specification in the model. E.g. if in the model (=the policy component
     * type's attribute) the value set type <code>UNRESTRICTED</code> is specified, then in the
     * configuration element all types are allowed. If <code>RANGE</code> is specified in the model,
     * then only <code>RANGE</code> is allowed in the configuration element.
     */
    String MSGCODE_VALUESET_TYPE_MISMATCH = MSGCODE_PREFIX + "ValueSetTypeMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the config'elements value set is not a subset of the
     * value set defined in the model.
     */
    String MSGCODE_VALUESET_IS_NOT_A_SUBSET = MSGCODE_PREFIX + "ValueSetIsNotASubset"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value-set of the attribute (in the model) is
     * invalid.
     */
    String MSGCODE_UNKNWON_VALUESET = MSGCODE_PREFIX + "InvalidAttirbuteValueSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a string is too long to fit into the value set.
     */
    String MSGCODE_STRING_TOO_LONG = MSGCODE_PREFIX + "StringTooLong"; //$NON-NLS-1$

    /**
     * Returns the set of allowed values.
     */
    @Override
    IValueSet getValueSet();

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
     * @throws IpsException if an error occurs.
     *
     * @see IIpsProject#getValueSetTypes(ValueDatatype)
     * @see IPolicyCmptTypeAttribute#getValueSet()
     * @see IValueSet#isUnrestricted()
     */
    @Override
    List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws IpsException;

    /**
     * Sets the type of the value set defining the values valid for this configuration element. If
     * the type of the currently existing value set is the same as the one to set, all old
     * informations (e.g. bounds and step for a range value set) are removed.
     */
    @Override
    void setValueSetType(ValueSetType type);

    /**
     * Converts the current value set to an enumeration type value set. If the current value set
     * type already is an enumeration type, nothing is changed. If the data type of the value set is
     * a {@link BooleanDatatype} or a {@link PrimitiveBooleanDatatype}, the values true, false are
     * added to the value set enumeration; in case of {@link BooleanDatatype}, null is also added to
     * the set.
     *
     * @return the new enumeration value set or the unchanged set if it has already been of type
     *             enumeration.
     *
     * @since 3.9
     */
    IEnumValueSet convertValueSetToEnumType();

    /**
     * Returns the element's value data type, or <code>null</code> if it can't be found. The
     * configuration element's data type is the attribute's data type the element is based on.
     *
     * @param ipsProject The IPS project which IPS object path is used to search.
     *
     */
    @Override
    ValueDatatype findValueDatatype(IIpsProject ipsProject);

    /**
     * Creates a copy of the given value set and applies this copy to this configuration element.
     */
    void setValueSetCopy(IValueSet source);

    void setValueSet(IValueSet source);

    /**
     * Overrides {@link IPropertyValue#findTemplateProperty(IIpsProject)} to return co-variant
     * {@code IConfiguredValueSet}.
     *
     * @see IPropertyValue#findTemplateProperty(IIpsProject)
     */
    @Override
    IConfiguredValueSet findTemplateProperty(IIpsProject ipsProject);

    IValueSet getNonTemplateValueSet();
}
