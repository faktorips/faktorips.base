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
     * Moves the enum attribute value that has been assigned to the given enum attribute one
     * position up in the collection order.
     * </p>
     * <p>
     * If no corresponding enum attribute value can be found or the enum attribute value is already
     * the first one then nothing will be done.
     * </p>
     * 
     * @param enumAttribute The enum attribute, that the enum attribute value that is to be moved
     *            upwards, refers to.
     * 
     * @throws NullPointerException If enumAttribute is <code>null</code>.
     */
    public void moveEnumAttributeValueUp(IEnumAttribute enumAttribute);

    /**
     * <p>
     * Moves the enum attribute value that has been assigned to the given enum attribute one
     * position down in the collection order.
     * </p>
     * <p>
     * If no corresponding enum attribute value can be found or the enum attribute value is already
     * the last one then nothing will be done.
     * </p>
     * 
     * @param enumAttribute The enum attribute, that the enum attribute value that is to be moved
     *            downwards, refers to.
     * 
     * @throws NullPointerException If enumAttribute is <code>null</code>.
     */
    public void moveEnumAttributeValueDown(IEnumAttribute enumAttribute);

}
