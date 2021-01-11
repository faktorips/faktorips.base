/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

import org.faktorips.devtools.core.internal.model.enums.EnumAttribute;
import org.faktorips.devtools.core.internal.model.enums.EnumValueContainer;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.internal.model.value.StringValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.devtools.core.model.value.ValueTypeMismatch;

/**
 * An <code>IEnumAttributeValue</code> belongs to an <code>IEnumValue</code>. It represents the value for a
 * specific <code>IEnumAttribute</code> of the <code>IEnumType</code> referenced by the
 * <code>IEnumValueContainer</code> the <code>IEnumValue</code> is contained in.
 * <p>
 * When searching for the referenced <code>IEnumAttribute</code> the assumption is made, that the
 * attributes in the <code>IEnumType</code> are ordered so that normal attributes come first and after
 * them the <code>IEnumLiteralNameAttribute</code>s.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttributeValue extends IIpsObjectPart {

    /** The XML tag for this IPS object part. */
    public static final String XML_TAG = "EnumAttributeValue"; //$NON-NLS-1$

    /** Name of the <code>value</code> property. */
    public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public static final String MSGCODE_PREFIX = "ENUMATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value of this <code>IEnumAttributeValue</code> does
     * not correspond to the data type defined in the <code>IEnumAttribute</code> being referenced.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX
            + "EnumAttributeValueNotParsable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <code>IEnumAttributeValue</code> is referring a
     * unique identifier <code>IEnumAttribute</code> but its value is empty.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY = MSGCODE_PREFIX
            + "EnumAttributeValueUniqueIdentifierValueEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that that this <code>IEnumAttributeValue</code> is referring
     * a unique identifier <code>IEnumAttribute</code> but its value is not unique.
     */
    public static final String MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE = MSGCODE_PREFIX
            + "EnumAttributeValueIdentifierNotUnique"; //$NON-NLS-1$

    public static final String MSGCODE_ENUM_ATTRIBUTE_ID_DISALLOWED_BY_IDENTIFIER_BOUNDARY = MSGCODE_PREFIX
            + "IdDisallowedByEnumTypesIdentifierBoundary"; //$NON-NLS-1$
    /**
     * Validation message code to indicate that that this <code>IEnumAttributeValue</code> is referring
     * a <code>IEnumLiteralNameAttribute</code> but its value is not java conform.
     * 
     * @deprecated Since version 3.0 a new type called <code>IEnumLiteralNameAttributeValue</code>
     *             exists.
     */
    @Deprecated
    public static final String MSGCODE_ENUM_ATTRIBUTE_VALUE_LITERAL_NAME_NOT_JAVA_CONFORM = MSGCODE_PREFIX
            + "EnumAttributeValueLiteralNameNotJavaConform"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute defines the wrong Value type
     */
    public static final String MSGCODE_INVALID_VALUE_TYPE = MSGCODE_PREFIX + "InvalidValueType"; //$NON-NLS-1$

    /**
     * Searches and returns the <code>IEnumAttribute</code> this <code>IEnumAttributeValue</code> refers to.
     * Returns <code>null</code> if none could be found.
     * <p>
     * Also returns <code>null</code> if the number of <code>IEnumAttribute</code>s in the referenced
     * <code>IEnumType</code> does not correspond to the number of <code>IEnumAttributeValue</code>s in the
     * <code>IEnumValue</code> containing this <code>IEnumAttributeValue</code>.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the
     *            referenced <code>IEnumAttribute</code>. This is not necessarily the project this
     *            <code>IEnumAttribute</code> is part of.
     * 
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public IEnumAttribute findEnumAttribute(IIpsProject ipsProject);

    /**
     * Returns whether this <code>IEnumAttributeValue</code> is the value for the
     * <code>IEnumLiteralNameAttribute</code>.
     */
    public boolean isEnumLiteralNameAttributeValue();

    /** Returns the value as <code>String</code>. Can also be <code>null</code>. */
    public IValue<?> getValue();

    /**
     * Sets the actual value.
     * 
     * @param value The new value. May also be <code>null</code>.
     */
    public void setValue(IValue<?> value);

    /**
     * Returns the <code>IEnumValue</code> this <code>IEnumAttributeValue</code> belongs to.
     * <p>
     * This is a shortcut for: <code>(IEnumValue)this.getParent();</code>
     */
    public IEnumValue getEnumValue();

    /**
     * Returning a string representation of the value.
     * 
     * @return a string representation of this part.
     */
    String getStringValue();

    /**
     * Returns <code>true</code>, if the value is <code>null</code> otherwise <code>false</code>. It
     * depends on the specific ValueHolder.
     * 
     * @return boolean <code>true</code> if the value is <code>null</code>
     */
    boolean isNullValue();

    /**
     * The ValueType describe the kind of value used in this value holder. The different kinds are
     * described in the {@link ValueType}. The reason for {@link ValueType} is to distinguish the
     * kind of {@link IValue}.
     */
    ValueType getValueType();

    /**
     * Converts from {@link StringValue} to {@link InternationalStringValue} or from
     * {@link InternationalStringValue} to {@link StringValue} depending on the property
     * multilingual.
     * <p>
     * IMPORTANT: This method does not trigger a change event! This is useful because this method is
     * normally not called for only one value but for multiple values in one enum container.
     * Consider to call {@link EnumValueContainer#fixEnumAttributeValues(IEnumAttribute)} instead of
     * this method.
     * 
     * @param multilingual the setting of the {@link IEnumAttribute}
     */
    void fixValueType(boolean multilingual);

    /**
     * Checks the {@link ValueType} in {@link IEnumAttributeValue} of the {@link IEnumAttribute}.
     * 
     * @param enumAttribute the {@link EnumAttribute} to check
     * @return {@link ValueTypeMismatch}
     */
    ValueTypeMismatch checkValueTypeMismatch(IEnumAttribute enumAttribute);

}
