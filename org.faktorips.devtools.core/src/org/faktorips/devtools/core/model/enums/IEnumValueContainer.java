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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * <code>EnumValueContainer</code> is the supertype for <code>EnumType</code> and
 * <code>EnumContent</code>.
 * <p>
 * In Faktor-IPS the values of an enum can be defined directly in the enum type itself or separate
 * from it in an enum content.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValueContainer extends IIpsObject {

    /**
     * Returns a list containing all enum values that belong to this enum value container.
     */
    public List<IEnumValue> getEnumValues();

    /**
     * Returns the {@link IEnumValue} for the provided value of the identifier attribute.
     * <p>
     * This method can only be applied to {@link IEnumValueContainer} that contain there own values.
     * Especially this doesn't hold true for {@link IEnumType}s which delegate the content to a
     * {@link IEnumContent}. For those cases <code>null</code> will be returned by this method.
     * <p>
     * Returns <tt>null</tt> if no <tt>IEnumValue</tt> could be found for the given identifier
     * attribute value or if the referenced <tt>IEnumType</tt> could not be found.
     * 
     * @throws CoreException If an exception occurs will processing.
     */
    public IEnumValue findEnumValue(String identifierAttributeValue, IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a new {@link List} and collects the values of the enumeration attribute that is
     * marked as the identifier attribute of all enumeration values of this container and returns
     * it.
     * 
     * @param ipsProject used as the starting point to search for enumeration type if necessary
     * @return the list of enumeration attribute values of the identifier attribute. An empty list
     *         will be returned if now values are found.
     */
    public List<String> findAllIdentifierAttributeValues(IIpsProject ipsProject);

    /**
     * Creates and returns a new enum value that has as many enum attribute values as the
     * corresponding enum type has attributes.
     * <p>
     * If the enum type referenced by this enum value container cannot be found then no enum value
     * will be created and <code>null</code> will be returned.
     * 
     * @throws CoreException If an error occurs while searching for the enum type.
     */
    public IEnumValue newEnumValue() throws CoreException;

    /**
     * Returns a reference to the enum type or <code>null</code> if no enum type can be found.
     * 
     * @param ipsProject The ips project which ips object path is used for the search. This is not
     *            necessarily the project this enum attribute is part of.
     * 
     * @throws CoreException If an error occurs while searching the given ips project for the enum
     *             type.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public IEnumType findEnumType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns how many enum values this enum value container currently contains.
     */
    public int getEnumValuesCount();

    /**
     * Moves the given <tt>IEnumValue</tt>s up or down by 1 and returns the their new positions.
     * This operation assures that there aren't any <tt>ContentChangeEvent</tt>s fired while moving
     * the individual <tt>IEnumValue</tt>s. Instead, a <tt>WHOLE_CONTENT_CHANGED</tt> event will be
     * fired after every <tt>IEnumValue</tt>s has been moved.
     * 
     * @param enumValuesToMove A list containing the <tt>IEnumValue</tt>s that shall be moved.
     * @param up Flag indicating whether to move up (<code>true</code>) or down (<code>false</code>
     *            ).
     * 
     * @throws CoreException If an error occurs while moving the <tt>IEnumValue</tt>s.
     * @throws NullPointerException If <code>enumValuesToMove</code> is <code>null</code>.
     * @throws NoSuchElementException If any of the given <tt>IEnumValue</tt>s is not part of this
     *             <tt>IEnumValueContainer</tt>.
     */
    public int[] moveEnumValues(List<IEnumValue> enumValuesToMove, boolean up) throws CoreException;

    /**
     * Returns the index of the given enum value in the containing list.
     * 
     * @param enumValue The enum value to obtain its index for.
     * 
     * @throws NoSuchElementException If there is no such enum value in this enum value container.
     * @throws NullPointerException If <code>enumValue</code> is <code>null</code>.
     */
    public int getIndexOfEnumValue(IEnumValue enumValue);

    /** Deletes all enum values from this enum value container. */
    public void clear();

    /** Clears the unique identifier validation cache. */
    public void clearUniqueIdentifierValidationCache();

    /**
     * Deletes the given <tt>IEnumValue</tt>s from this <tt>IEnumValueContainer</tt>. This operation
     * assures that no <tt>ContentChangeEvent</tt>s are fired during the deletion of the individual
     * <tt>IEnumValue</tt>s. Instead a <tt>WHOLE_CONTENT_CHANGED</tt> event will be fired after
     * every <tt>IEnumValue</tt> has been deleted.
     * <p>
     * Returns <tt>true</tt> if any <tt>IEnumValue</tt>s were deleted, <tt>false</tt> if not
     * (following the behavior of the Java collections here).
     * 
     * @param enumValuesToDelete A list containing all <tt>IEnumValue</tt>s that should be deleted
     *            from this <tt>IEnumValueContainer</tt>.
     * 
     * @throws NoSuchElementException If the given list contains an <tt>IEnumValue</tt> that is
     *             not part of this <tt>IEnumValueContainer</tt>.
     */
    public boolean deleteEnumValues(List<IEnumValue> enumValuesToDelete);

}
