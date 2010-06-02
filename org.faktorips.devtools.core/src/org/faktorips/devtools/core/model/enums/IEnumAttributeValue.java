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

package org.faktorips.devtools.core.model.enums;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An <tt>IEnumAttributeValue</tt> belongs to an <tt>IEnumValue</tt>. It represents the value for a
 * specific <tt>IEnumAttribute</tt> of the <tt>IEnumType</tt> referenced by the
 * <tt>IEnumValueContainer</tt> the <tt>IEnumValue</tt> is contained in.
 * <p>
 * When searching for the referenced <tt>IEnumAttribute</tt> the assumption is made, that the
 * attributes in the <tt>IEnumType</tt> are ordered so that normal attributes come first and after
 * them the <tt>IEnumLiteralNameAttribute</tt>s.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttributeValue extends IIpsObjectPart {

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "EnumAttributeValue"; //$NON-NLS-1$

    /** Name of the <tt>value</tt> property. */
    public final static String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value of this <tt>IEnumAttributeValue</tt> does
     * not correspond to the data type defined in the <tt>IEnumAttribute</tt> being referenced.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX
            + "EnumAttributeValueNotParsable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this <tt>IEnumAttributeValue</tt> is referring a
     * unique identifier <tt>IEnumAttribute</tt> but its value is empty.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_VALUE_EMPTY = MSGCODE_PREFIX
            + "EnumAttributeValueUniqueIdentifierValueEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that that this <tt>IEnumAttributeValue</tt> is referring
     * a unique identifier <tt>IEnumAttribute</tt> but its value is not unique.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_VALUE_UNIQUE_IDENTIFIER_NOT_UNIQUE = MSGCODE_PREFIX
            + "EnumAttributeValueIdentifierNotUnique"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that that this <tt>IEnumAttributeValue</tt> is referring
     * a <tt>IEnumLiteralNameAttribute</tt> but its value is not java conform.
     * 
     * @deprecated Since version 3.0 a new type called <tt>IEnumLiteralNameAttributeValue</tt>
     *             exists.
     */
    @Deprecated
    public final static String MSGCODE_ENUM_ATTRIBUTE_VALUE_LITERAL_NAME_NOT_JAVA_CONFORM = MSGCODE_PREFIX
            + "EnumAttributeValueLiteralNameNotJavaConform"; //$NON-NLS-1$

    /**
     * Searches and returns the <tt>IEnumAttribute</tt> this <tt>IEnumAttributeValue</tt> refers to.
     * Returns <tt>null</tt> if none could be found.
     * <p>
     * Also returns <tt>null</tt> if the number of <tt>IEnumAttribute</tt>s in the referenced
     * <tt>IEnumType</tt> does not correspond to the number of <tt>IEnumAttributeValue</tt>s in the
     * <tt>IEnumValue</tt> containing this <tt>IEnumAttributeValue</tt>.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search of the
     *            referenced <tt>IEnumAttribute</tt>. This is not necessarily the project this
     *            <tt>IEnumAttribute</tt> is part of.
     * 
     * @throws CoreException If an error occurs while searching the given IPS project for the
     *             referenced <tt>IEnumAttribute</tt>.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public IEnumAttribute findEnumAttribute(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns whether this <tt>IEnumAttributeValue</tt> is the value for the
     * <tt>IEnumLiteralNameAttribute</tt>.
     */
    public boolean isEnumLiteralNameAttributeValue();

    /** Returns the value as <tt>String</tt>. Can also be <tt>null</tt>. */
    public String getValue();

    /**
     * Sets the actual value.
     * 
     * @param value The new value. May also be <tt>null</tt>.
     */
    public void setValue(String value);

    /**
     * Sets the actual value, transformed to a valid literal name.
     * <p>
     * This could be a programming language dependent implementation in the future but for now all
     * letters will be transformed to upper case letters and all spaces will be transformed to
     * underscores.
     * 
     * @param value The new value. May also be <tt>null</tt>. Will be transformed to a valid literal
     *            name.
     * 
     * @deprecated This method is useless since version 3.0 as literal names are now set by casting
     *             the <tt>IEnumAttributeValue</tt> to <tt>IEnumLiteralNameAttributeValue</tt> and
     *             calling {@link IEnumLiteralNameAttributeValue#setValue(String)}. The cast should
     *             only be done after calling {@link #isEnumLiteralNameAttributeValue()}.
     * 
     * @see #isEnumLiteralNameAttributeValue()
     * @see IEnumLiteralNameAttributeValue#setValue(String)
     */
    @Deprecated
    public void setValueAsLiteralName(String value);

    /**
     * Returns the <tt>IEnumValue</tt> this <tt>IEnumAttributeValue</tt> belongs to.
     * <p>
     * This is a shortcut for: <tt>(IEnumValue)this.getParent();</tt>
     */
    public IEnumValue getEnumValue();

}
