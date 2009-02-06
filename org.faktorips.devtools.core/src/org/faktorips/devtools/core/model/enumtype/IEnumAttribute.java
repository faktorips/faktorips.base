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

package org.faktorips.devtools.core.model.enumtype;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * <p>
 * This is the published interface for enum attributes.
 * </p>
 * <p>
 * For more information about how enum attributes relate to the entire Faktor-IPS enumeration
 * concept please read the documentation of IEnumType.
 * </p>
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttribute extends IIpsObjectPart {

    /** The xml tag for this ips object part. */
    public final static String XML_TAG = "EnumAttribute"; //$NON-NLS-1$

    /** Name of the the datatype property. */
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /** Name of the the isIdentifier property. */
    public final static String PROPERTY_IDENTIFIER = "identifier"; //$NON-NLS-1$

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
     * Sets the name of this enum attribute.
     * 
     * @param name The new name for this enum attribute.
     * 
     * @throws NullPointerException If name is <code>null</code>.
     */
    public void setName(String name);

    /**
     * Returns the unqualified name of the datatype of this enum attribute.
     * 
     * @return A <code>String</code> representing the unqualified name of the datatype of this enum
     *         attribute.
     */
    public String getDatatype();

    /**
     * Sets the datatype of this enum attribute.
     * 
     * @param datatype The unqualified name of the datatype.
     * 
     * @throws NullPointerException If datatype is <code>null</code>.
     */
    public void setDatatype(String datatype);

    /**
     * Returns <code>true</code> if this enum attribute is an identifier, <code>false</code> if not.
     * 
     * @return Flag indicating whether this enum attribute is an identifier or not.
     */
    public boolean isIdentifier();

    /**
     * Sets whether this enum attribute is an identifier.
     * 
     * @param identifier Flag indicating whether this enum attribute will be an identifier.
     */
    public void setIdentifier(boolean identifier);

}
