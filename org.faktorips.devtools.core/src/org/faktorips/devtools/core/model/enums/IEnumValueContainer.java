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
 * <tt>EnumValueContainer</tt> is the supertype for <tt>EnumType</tt> and <tt>EnumContent</tt>.
 * <p>
 * In Faktor-IPS the values of an enumeration can be defined directly in the <tt>IEnumType</tt>
 * itself or separate from it in an <tt>IEnumContent</tt>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValueContainer extends IIpsObject {

    /**
     * Returns a list containing all <tt>IEnumValue</tt>s that belong to this
     * <tt>IEnumValueContainer</tt>.
     */
    public List<IEnumValue> getEnumValues();

    /**
     * Returns the <tt>IEnumValue</tt> for the provided value of the identifier attribute.
     * <p>
     * This method can only be applied to <tt>IEnumValueContainer</tt>s that contain there own
     * values. Especially this doesn't hold true for <tt>IEnumType</tt>s which delegate their
     * content to a separate <tt>IEnumContent</tt>. For those cases <tt>null</tt> will be returned
     * by this method.
     * <p>
     * Returns <tt>null</tt> if no <tt>IEnumValue</tt> could be found for the given identifier
     * attribute value or if the referenced <tt>IEnumType</tt> could not be found.
     * 
     * @throws CoreException If an exception occurs will processing.
     */
    public IEnumValue findEnumValue(String identifierAttributeValue, IIpsProject ipsProject) throws CoreException;

    /**
     * Creates a new list and collects the values of the enumeration attribute that is marked as the
     * identifier attribute of all enumeration values of this container and returns it.
     * 
     * @param ipsProject The IPS project used as the starting point to search for the enumeration
     *            type if necessary.
     */
    public List<String> findAllIdentifierAttributeValues(IIpsProject ipsProject);

    /**
     * Creates and returns a new <tt>IEnumValue</tt> that has as many <tt>IEnumAttributeValue</tt>s
     * as the corresponding <tt>IEnumType</tt> has <tt>IEnumAttribute</tt>s.
     * <p>
     * If the <tt>IEnumType</tt> referenced by this <tt>IEnumValueContainer</tt> cannot be found
     * then no <tt>IEnumValue</tt> will be created and <tt>null</tt> will be returned.
     * 
     * @throws CoreException If an error occurs while searching for the referenced
     *             <tt>IEnumType</tt> (in case this <tt>IEnumValueContainer</tt> is an
     *             <tt>IEnumContent</tt>.
     */
    public IEnumValue newEnumValue() throws CoreException;

    /**
     * Returns a reference to the <tt>IEnumType</tt> or <tt>null</tt> if no <tt>IEnumType</tt> can
     * be found.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this <tt>IEnumAttribute</tt> is part of.
     * 
     * @throws CoreException If an error occurs while searching the given IPS project for the
     *             referenced <tt>IEnumType</tt>.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public IEnumType findEnumType(IIpsProject ipsProject) throws CoreException;

    /** Returns how many <tt>IEnumValue</tt>s this <tt>IEnumValueContainer</tt> currently contains. */
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
     * Returns the index of the given <tt>IEnumValue</tt> or -1 if the given <tt>IEnumValue</tt>
     * does not exist in this <tt>IEnumValueContainer</tt>.
     * 
     * @param enumValue The <tt>IEnumValue</tt> to obtain its index for.
     * 
     * @throws NullPointerException If <tt>enumValue</tt> is <tt>null</tt>.
     */
    public int getIndexOfEnumValue(IEnumValue enumValue);

    /** Deletes all <tt>IEnumValue</tt>s from this <tt>IEnumValueContainer</tt>. */
    public void clear();

    /** Clears the unique identifier validation cache. */
    public void clearUniqueIdentifierValidationCache();

    /**
     * Deletes the given <tt>IEnumValue</tt>s from this <tt>IEnumValueContainer</tt>. This operation
     * assures that no <tt>ContentChangeEvent</tt>s are fired during the deletion of the individual
     * <tt>IEnumValue</tt>s. Instead a <tt>WHOLE_CONTENT_CHANGED</tt> event will be fired after
     * every <tt>IEnumValue</tt> has been deleted.
     * <p>
     * If <tt>null</tt> is given nothing will happen. If an <tt>IEnumValue</tt> is not part of this
     * <tt>IEnumValueContainer</tt> the <tt>IEnumValue</tt> will be skipped.
     * <p>
     * Returns <tt>true</tt> if any <tt>IEnumValue</tt>s were deleted, <tt>false</tt> if not
     * (following the behavior of the Java collections here).
     * 
     * @param enumValuesToDelete A list containing all <tt>IEnumValue</tt>s that should be deleted
     *            from this <tt>IEnumValueContainer</tt>.
     */
    public boolean deleteEnumValues(List<IEnumValue> enumValuesToDelete);

    /**
     * Returns whether this <tt>IEnumValueContainer</tt> is currently capable of holding
     * <tt>IEnumValue</tt>s (<tt>true</tt>) or not (<tt>false</tt>).
     * 
     * @throws CoreException May throw this exception at any time.
     */
    public boolean isCapableOfContainingValues() throws CoreException;

}
