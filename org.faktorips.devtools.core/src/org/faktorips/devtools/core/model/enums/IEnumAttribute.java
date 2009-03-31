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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * An enum attribute is a part of an enum type that describes a property of the enum type.
 * <p>
 * Enum attributes are always of a specific <em>datatype</em> and can be <em>inherited</em> from an
 * enum type in the supertype hierarchy.
 * <p>
 * An enum attribute can be marked as <em>identifier</em> which implies that each value for this
 * enum attribute must be unique.
 * <p>
 * For more information about how enum attributes relate to the entire Faktor-IPS enums concept
 * please read the documentation of IEnumType.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttribute extends IIpsObjectPart {

    /** The xml tag for this ips object part. */
    public final static String XML_TAG = "EnumAttribute"; //$NON-NLS-1$

    /** Name of the <code>datatype</code> property. */
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /** Name of the <code>identifier</code> property. */
    public final static String PROPERTY_IDENTIFIER = "identifier"; //$NON-NLS-1$

    /** Name of the <code>inherited</code> property. */
    public final static String PROPERTY_INHERITED = "inherited"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMATTRIBUTE-"; //$NON-NLS-1$

    /** Validation message code to indicate that the name of this enum attribute is missing. */
    public final static String MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING = MSGCODE_PREFIX + "EnumAttributeNameMissing"; //$NON-NLS-1$

    /** Validation message code to indicate that the name of this enum attribute is already used. */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME = MSGCODE_PREFIX + "EnumAttributeNameMissing"; //$NON-NLS-1$

    /** Validation message code to indicate that the datatype of this enum attribute is missing. */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING = MSGCODE_PREFIX
            + "EnumAttributeDatatypeMissing"; //$NON-NLS-1$

    /** Validation message code to indicate that the datatype of this enum attribute does not exist. */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST = MSGCODE_PREFIX
            + "EnumAttributeDatatypeDoesNotExist"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this enum attribute is marked as identifier but the
     * datatype is not String.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_IDENTIFIER_NOT_OF_DATATYPE_STRING = MSGCODE_PREFIX
            + "EnumAttributeIdentifierNotOfDatatypeString"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this enum attribute inherits from the supertype
     * hierarchy but there is no such attribute in the supertype hierarchy.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "EnumAttributeNoSuchAttributeInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there are other enum attributes marked as
     * identifier.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_IDENTIFIER = MSGCODE_PREFIX
            + "EnumAttributeDuplicateIdentifier"; //$NON-NLS-1$

    /**
     * Sets the name of this enum attribute.
     * 
     * @param name The new name for this enum attribute.
     * 
     * @throws NullPointerException If <code>name</code> is <code>null</code>.
     */
    public void setName(String name);

    /**
     * Returns the unqualified name of the datatype of this enum attribute.
     */
    public String getDatatype();

    /**
     * Sets the datatype of this enum attribute.
     * 
     * @param datatype The unqualified name of the datatype.
     * 
     * @throws NullPointerException If <code>datatype</code> is <code>null</code>.
     */
    public void setDatatype(String datatype);

    /**
     * Returns <code>true</code> if this enum attribute is the literal name attribute of this enum type, <code>false</code> if not.
     */
    public boolean isLiteralNameAttribute();

    /**
     * Sets whether this enum attribute is the literal name attribute of this enum type.
     * 
     * @param isIdentifier Flag indicating whether this enum attribute will be an identifier.
     */
    public void setLiteralNameAttribute(boolean isIdentifier);

    /**
     * Returns <code>true</code> if this enum attribute is inherited from the supertype hierarchy,
     * <code>false</code> if not.
     */
    public boolean isInherited();

    /**
     * Sets whether this enum attribute is inherited from the supertype hierarchy.
     * 
     * @param isInherited Flag indicating whether this enum attribute is inherited from the
     *            supertype hierarchy.
     */
    public void setInherited(boolean isInherited);

    /**
     * Returns the enum type this enum attribute belongs to.
     * <p>
     * This is a shortcut for: <code>(IEnumType)this.getParent();</code>
     */
    public IEnumType getEnumType();

}
