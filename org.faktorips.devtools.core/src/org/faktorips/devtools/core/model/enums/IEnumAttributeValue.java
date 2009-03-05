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

/**
 * An enum attribute value belongs to an enum value. It represents the value for a specific enum
 * attribute of the enum type referenced by the enum value container the enum value is contained in.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumAttributeValue extends IIpsObjectPart {

    /** The xml tag for this ips object part. */
    public final static String XML_TAG = "EnumAttributeValue"; //$NON-NLS-1$

    /** Name of the value property. */
    public final static String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value of this enum attribute value does not
     * correspond to the datatype defined in the enum attribute being referenced.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX
            + "EnumAttributeValueNotParsable"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this enum attribute value is refering the identifier
     * enum attribute but is empty.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_VALUE_IDENTIFIER_VALUE_EMPTY = MSGCODE_PREFIX
            + "EnumAttributeValueIdentifierValueEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that that this enum attribute value is refering the
     * identifier enum attribute but is not unique.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_VALUE_IDENTIFIER_NOT_UNIQUE = MSGCODE_PREFIX
            + "EnumAttributeValueIdentifierNotUnique"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that that this enum attribute value is refering the
     * identifier enum attribute but is not java conform.
     */
    public final static String MSGCODE_ENUM_ATTRIBUTE_VALUE_IDENTIFIER_NOT_JAVA_CONFORM = MSGCODE_PREFIX
            + "EnumAttributeValueIdentifierNotJavaConform"; //$NON-NLS-1$

    /**
     * Searches and returns the enum attribute this enum attribute value refers to. Returns
     * <code>null</code> if none could be found.
     * 
     * @throws CoreException If an error occurs while searching for the enum attribute.
     */
    public IEnumAttribute findEnumAttribute() throws CoreException;

    /**
     * Returns the value as <code>String</code>.
     */
    public String getValue();

    /**
     * Sets the actual value.
     * 
     * @param value The new value. May also be <code>null</code>.
     */
    public void setValue(String value);

    /**
     * Returns the enum value this enum attribute value belongs to.
     * <p>
     * This is a shortcut for: <code>(IEnumValue)this.getParent();</code>
     */
    public IEnumValue getEnumValue();

}
