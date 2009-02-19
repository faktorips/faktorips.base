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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * <p>
 * This is the published interface for enum values.
 * </p>
 * <p>
 * For more information about how enum values relate to the entire Faktor-IPS enumeration concept
 * please read the documentation of IEnumType.
 * </p>
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

    /**
     * Returns a list containing all enum attribute values.
     * 
     * @return A list containing all enum attribute values.
     */
    public List<IEnumAttributeValue> getEnumAttributeValues();

    /**
     * Returns the enum attribute value with the specified id.
     * 
     * @param id The id of the enum attribute value to return.
     * 
     * @return A reference to the enum attribute value specified by the given id.
     */
    public IEnumAttributeValue getEnumAttributeValue(int id);

    /**
     * Creates a new enum attribute value and returns a reference to it. This works only if there
     * are currently less enum attribute values than the enum type has attributes.
     * 
     * @return A reference to the newly created enum attribute value.
     * 
     * @throws CoreException If the enum type this enum value is based upon cannot be found.
     * @throws IllegalStateException If there are already the same number of enum attribute values
     *             as enum attributes in the enum type.
     */
    public IEnumAttributeValue newEnumAttributeValue() throws CoreException;

    /**
     * <p>
     * Returns the enum value container this enum value is being stored in.
     * </p>
     * <p>
     * This is a shortcut for:<br />
     * <code>IEnumValueContainer enumValueContainer = (IEnumValueContainer)enumValue.getParent();</code>
     * </p>
     * 
     * @return The enum value container this enum value belongs to.
     */
    public IEnumValueContainer getEnumValueContainer();

}
