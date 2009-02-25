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
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;

/**
 * This is the published interface for <code>EnumValueContainer</code>.
 * <p>
 * <code>EnumValueContainer</code> is the supertype for <code>EnumType</code> and
 * <code>EnumContent</code>. This is because in Faktor-IPS the values of an enumeration can be
 * defined directly in the enum type or separate from it by the product side.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValueContainer extends IIpsObject {

    /**
     * Returns a list containing all enum values that belong to this enum value container.
     * 
     * @return A list containing all enum values that belong to this enum value container.
     */
    public List<IEnumValue> getEnumValues();

    /**
     * Returns the enum value with the specified id.
     * 
     * @param id The id of the enum value to return.
     * 
     * @return A reference to the enum value specified by the given id.
     */
    public IEnumValue getEnumValue(int id);

    /**
     * Creates a new enum value and returns a reference to it.
     * 
     * @return A reference to the newly created enum value.
     */
    public IEnumValue newEnumValue();

    /**
     * Returns a reference to the enum type or <code>null</code> if no enum type can be found.
     * 
     * @return A reference to the enum type or <code>null</code> if the enum type can't be found.
     * 
     * @throws CoreException If an error occures while searching the ips model for the enum type.
     */
    public IEnumType findEnumType() throws CoreException;

    /**
     * Returns how many enum values this enum value container currently contains.
     * 
     * @return The number of enum values that this enum value container is currently containing.
     */
    public int getEnumValuesCount();

    /**
     * Moves the given enum value one position upwards in the enum values collection.
     * <p>
     * If the given enum value is already the first enum value then absolutely nothing will be done.
     * 
     * @param enumValue The enum value to move further up in the order.
     * 
     * @return The new index of the enum value.
     * 
     * @throws CoreException If an error occurs while moving the enum values.
     * @throws NullPointerException If enumValue is <code>null</code>.
     */
    public int moveEnumValueUp(IEnumValue enumValue) throws CoreException;

    /**
     * Moves the given enum value one position downwards in the enum values collection.
     * <p>
     * If the given enum value is already the last enum value then absolutely nothing will be done.
     * 
     * @param enumValue The enum value to move further down in the order.
     * 
     * @return The new index of the enum value.
     * 
     * @throws CoreException If an error occurs while moving the enum values.
     * @throws NullPointerException If enumValue is <code>null</code>.
     */
    public int moveEnumValueDown(IEnumValue enumValue) throws CoreException;

    /**
     * Returns the index of the given enum value in the enum values collection.
     * 
     * @param enumValue The enum value to obtain its index for.
     * 
     * @return The index identifying the given enum value in the enum values collection.
     * 
     * @throws NoSuchElementException If there is no such enum value in this enum value container.
     * @throws NullPointerException If enumValue is <code>null</code>.
     */
    public int getIndexOfEnumValue(IEnumValue enumValue);

}
