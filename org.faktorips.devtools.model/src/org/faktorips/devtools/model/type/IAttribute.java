/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * A type's attribute.
 */
public interface IAttribute extends IChangingOverTimeProperty {

    public static final String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    public static final String PROPERTY_DEFAULT_VALUE = "defaultValue"; //$NON-NLS-1$

    public static final String PROPERTY_OVERWRITES = "overwrite"; //$NON-NLS-1$

    public static final String PROPERTY_CHANGING_OVER_TIME = "changingOverTime"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "ATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the name of the attribute is not a valid java field
     * identifier.
     */
    public static final String MSGCODE_INVALID_ATTRIBUTE_NAME = MSGCODE_PREFIX + "InvalidAttributeName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this attribute is not set.
     */
    public static final String MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE = MSGCODE_PREFIX
            + "DefaultNotParsableUnknownDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of this attribute is not a valid data
     * type.
     */
    public static final String MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE = MSGCODE_PREFIX
            + "ValueNotParsableInvalidDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the default value of this attribute can not be
     * parsed by the data type of this attribute.
     */
    public static final String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX + "ValueTypeMissmatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type provided for a parameter is not valid.
     */
    public static final String MSGCODE_DATATYPE_NOT_FOUND = MSGCODE_PREFIX + "DatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the default value of this attribute is not contained
     * in the value set of this attribute.
     */
    public static final String MSGCODE_DEFAULT_NOT_IN_VALUESET = MSGCODE_PREFIX + "DefaultNotInValueSet"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute is marked overwriting an attribute in
     * the super type hierarchy, but there is no such attribute.
     */
    public static final String MSGCODE_NOTHING_TO_OVERWRITE = MSGCODE_PREFIX + "NothingToOverwrite"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but does has an
     * incompatible value set.
     */
    public static final String MSGCODE_OVERWRITTEN_ATTRIBUTE_INCOMPAIBLE_VALUESET = MSGCODE_PREFIX
            + "OverwrittenAttributeModifier"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but has a different
     * datatype.
     */
    public static final String MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_DATATYPE = MSGCODE_PREFIX
            + "OverwrittenAttributeDifferentDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but has a different
     * modifier.
     */
    public static final String MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_MODIFIER = IAttribute.MSGCODE_PREFIX
            + "OverwrittenAttributeDifferentModifier"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but has a different
     * data type.
     */
    public static final String MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE = IAttribute.MSGCODE_PREFIX
            + "OverwrittenAttributeIncompatibleDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that an attribute overwrites another but change over time
     * configuration differs
     */
    public static final String MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME = IAttribute.MSGCODE_PREFIX
            + "OverwrittenAttributeDifferentChangeOverTime"; //$NON-NLS-1$

    /**
     * Sets the attribute's name.
     */
    public void setName(String newName);

    /**
     * Returns the attribute's data type. Note that only value data types are allowed as attribute
     * data type.
     */
    public String getDatatype();

    /**
     * Sets the attribute's data type. Note that only value data types are allowed as attribute data
     * type.
     */
    public void setDatatype(String newDatatype);

    /**
     * Returns the attribute's value data type. If this attribute is linked to a policy component
     * type attribute, the policy component type's value data type is returned. If the attribute is
     * not linked, the attribute's *own* value data type is returned.
     * 
     * @param project The project which IPS object path is used for the search. This is not
     *            necessarily the project this type is part of.
     * 
     * @see #getDatatype()
     */
    public ValueDatatype findDatatype(IIpsProject project);

    /**
     * Returns the attribute's default value.
     */
    public String getDefaultValue();

    /**
     * Sets the attribute's default value.
     */
    public void setDefaultValue(String newValue);

    /**
     * Returns <code>true</code> if this attribute is a derived one, otherwise <code>false</code>.
     */
    public boolean isDerived();

    /**
     * Returns <code>true</code> if this attribute is marked to overwrite an attribute with the same
     * name somewhere up the supertype hierarchy, <code>false</code> otherwise.
     */
    public boolean isOverwrite();

    /**
     * <code>true</code> to indicate that this attribute overwrites an attribute with the same name
     * somewhere up the super type hierarchy or <code>false</code> to let this attribute be a new
     * one.
     */
    public void setOverwrite(boolean overwrites);

    /**
     * Returns the first attribute found with the same name in the super types hierarchy or
     * <code>null</code> if no such attribute exists.
     * 
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws CoreRuntimeException if an error occurs while searching.
     */
    public IAttribute findOverwrittenAttribute(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the {@link IValueSet} of this {@link IAttribute}.
     */
    public IValueSet getValueSet();

    /**
     * Configures this attribute to change or be constant over time. If <code>true</code> every
     * {@link IProductCmptGeneration} may specify a different value for this attribute. If
     * <code>false</code> the value is the same for all generations.
     * 
     * @param changesOverTime whether or not this attribute should change over time
     */
    public void setChangingOverTime(boolean changesOverTime);

}
