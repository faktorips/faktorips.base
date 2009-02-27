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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * This is the published interface for enum values.
 * <p>
 * For more information about how enum values relate to the entire Faktor-IPS enumeration concept
 * please read the documentation of IEnumType.
 * 
 * @see org.faktorips.devtools.core.model.enumtype.IEnumType
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValue extends IIpsObjectPart {

    /** The xml tag for this ips object part. */
    public final static String XML_TAG = "EnumValue"; //$NON-NLS-1$

    /** Prefix for all message codes of this class. */
    public final static String MSGCODE_PREFIX = "ENUMVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there are not as many enum attribute values as enum
     * attributes in the enum type.
     */
    public final static String MSGCODE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES = MSGCODE_PREFIX
            + "NumberAttributeValuesDoesNotCorrespondToNumberAttributes"; //$NON-NLS-1$

    /** Validation message code to indicate that the identifier enum attribute value is empty. */
    public final static String MSGCODE_IDENTIFIER_ATTRIBUTE_VALUE_EMPTY = MSGCODE_PREFIX
            + "IdentifierAttributeValueEmpty"; //$NON-NLS-1$

    /**
     * Returns a list containing all enum attribute values.
     */
    public List<IEnumAttributeValue> getEnumAttributeValues();

    /**
     * Creates a new enum attribute value and returns a reference to it.
     * <p>
     * This works only if there are currently less enum attribute values than the referenced enum
     * type has enum attributes.
     * 
     * @throws CoreException If the enum type this enum value is based upon cannot be found.
     * @throws IllegalStateException If there are already the same number of enum attribute values
     *             as enum attributes in the enum type.
     */
    public IEnumAttributeValue newEnumAttributeValue() throws CoreException;

    /**
     * Returns the enum value container this enum value is being stored in.
     * <p>
     * This is a shortcut for: <code>(IEnumValueContainer)enumValue.getParent();</code>
     */
    public IEnumValueContainer getEnumValueContainer();

    /**
     * Searches and returns the enum attribute value that refers to the identifier attribute of the
     * enum type this enum value refers to.
     * <p>
     * Returns <code>null</code> if none can be found.
     * 
     * @throws CoreException If an error occurs while searching for the identifier enum attribute.
     */
    public IEnumAttributeValue findIdentifierEnumAttributeValue() throws CoreException;

    /**
     * Returns how many enum attribute values this enum value is currently containing.
     */
    public int getEnumAttributeValuesCount();

}
