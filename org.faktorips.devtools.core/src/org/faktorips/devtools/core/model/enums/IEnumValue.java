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
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An enum value represents a complete set of enum attribute values for an enum type. Enum values
 * are always contained in an enum value container.
 * <p>
 * For more information about how enum values relate to the entire Faktor-IPS enumeration concept
 * please read the documentation of IEnumType.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumType
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
    public final static String MSGCODE_ENUM_VALUE_NUMBER_ATTRIBUTE_VALUES_DOES_NOT_CORRESPOND_TO_NUMBER_ATTRIBUTES = MSGCODE_PREFIX
            + "EnumValueNumberAttributeValuesDoesNotCorrespondToNumberAttributes"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the enum type is abstract and therefore this enum
     * value is obsolete (only if this enum value belongs to an enum type).
     */
    public final static String MSGCODE_ENUM_VALUE_ENUM_TYPE_ABSTRACT = MSGCODE_PREFIX + "EnumValueEnumTypeAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the enum type does not contain values and therefore
     * this enum value is obsolete (only if this enum value belongs to an enum type).
     */
    public final static String MSGCODE_ENUM_VALUE_ENUM_TYPE_DOES_NOT_CONTAIN_VALUES = MSGCODE_PREFIX
            + "EnumValueEnumTypeDoesNotContainValues"; //$NON-NLS-1$

    /**
     * Returns a list containing all enum attribute values.
     */
    public List<IEnumAttributeValue> getEnumAttributeValues();

    /**
     * Creates a new enum attribute value and returns a reference to it.
     * 
     * @throws CoreException If the enum type this enum value is based upon cannot be found.
     */
    public IEnumAttributeValue newEnumAttributeValue() throws CoreException;

    /**
     * Returns the enum value container this enum value is being stored in.
     * <p>
     * This is a shortcut for: <code>(IEnumValueContainer)this.getParent();</code>
     */
    public IEnumValueContainer getEnumValueContainer();

    /**
     * Searches and returns the enum attribute value that refers to the given enum attribute of the
     * enum type this enum value refers to.
     * <p>
     * Returns <code>null</code> if none can be found, if the referenced enum type can't be found or
     * if the provided enum attribute is <code>null</code>.
     * 
     * @param ipsProject The ips project which ips object path is used for the search of the
     *            referenced enum type. This is not necessarily the project this enum attribute is
     *            part of.
     * @param enumAttribute The enum attribute to obtain the enum attribute value for.
     * 
     * @throws CoreException If an error occurs while searching the given ips project for the
     *             referenced enum type.
     * @throws IllegalArgumentException If the given enum attribute is not part of the enum type
     *             referenced by this enum value.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public IEnumAttributeValue findEnumAttributeValue(IIpsProject ipsProject, IEnumAttribute enumAttribute)
            throws CoreException;

    /**
     * Returns how many enum attribute values this enum value is currently containing.
     */
    public int getEnumAttributeValuesCount();

    /**
     * Moves the given enum attribute value up or down by 1 in the containing list and returns its
     * new index.
     * <p>
     * If the enum attribute value is already the first / last one then nothing will be done.
     * 
     * @param enumAttributeValue The enum attribute value that is to be moved.
     * @param up Flag indicating whether to move upwards (<code>true</code>) or downwards (
     *            <code>false</code>).
     * 
     * @throws NullPointerException If <code>enumAttributeValue</code> is <code>null</code>.
     * @throws NoSuchElementException If the given enum attribute value is not a part of this enum
     *             value.
     */
    public int moveEnumAttributeValue(IEnumAttributeValue enumAttributeValue, boolean up);

}
