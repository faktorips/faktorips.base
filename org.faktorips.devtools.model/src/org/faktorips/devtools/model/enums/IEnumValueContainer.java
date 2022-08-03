/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.enums;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.devtools.model.value.ValueTypeMismatch;
import org.faktorips.runtime.model.enumtype.EnumType;

/**
 * {@link IEnumValueContainer} is the super type for {@link IEnumType} and {@link IEnumContent}.
 * <p>
 * In Faktor-IPS the values of an enumeration can be defined directly in the {@link IEnumType}
 * itself or separate from it in an {@link IEnumContent}.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public interface IEnumValueContainer extends IIpsObject {

    /**
     * Returns a list containing all {@link IEnumValue IEnumValues} that belong to this
     * {@link IEnumValueContainer}.
     */
    List<IEnumValue> getEnumValues();

    /**
     * Returns a list of all {@link IEnumValue IEnumValues} belonging to this enum.
     * {@link IEnumValue IEnumValues} from inside AND outside the container will be taking into
     * account.
     * <p>
     * E.g: If the {@link IEnumValueContainer} is an {@link IEnumContent}, this method will return
     * all {@link IEnumValue IEnumValues} from the {@link IEnumContent} and also all
     * {@link IEnumValue} form its corresponding {@link EnumType}. Otherwise only the
     * {@link IEnumValue IEnumValues} of the {@link EnumType} are identified.
     */
    List<IEnumValue> findAggregatedEnumValues();

    /**
     * Returns the {@link IEnumValue} for the provided value of the identifier attribute.
     * <p>
     * This method can be applied to {@link IEnumValueContainer IEnumValueContainers} that contain
     * their own values and also those {@link IEnumValueContainer IEnumValueContainers} which are
     * extensible.
     * <p>
     * Returns {@code null} if no {@link IEnumValue} could be found for the given identifier
     * attribute value, if the referenced {@link IEnumType} isn't known or no valid identifier
     * attribute can be found.
     * 
     * @param identifierAttributeValue The value of the default identifier attribute that identifies
     *            the {@link IEnumValue} to retrieve
     * @param ipsProject The IPS project used as the starting point to search for the enumeration
     *            type if necessary
     * 
     * @throws IpsException If an error occurs while searching for the referenced {@link IEnumType}
     *             or the identifier {@link IEnumAttribute}
     */
    IEnumValue findEnumValue(String identifierAttributeValue, IIpsProject ipsProject) throws IpsException;

    /**
     * Creates a new list and collects the values of the enumeration attribute that is marked as the
     * identifier attribute of all enumeration values of this container and returns it.
     * <p>
     * If this is an extensible {@link IEnumType} then the returned list contains aggregated values
     * {@code findAggregatedEnumValues()}.
     * 
     * @param ipsProject The IPS project used as the starting point to search for the enumeration
     *            type if necessary.
     */
    List<String> findAllIdentifierAttributeValues(IIpsProject ipsProject);

    /**
     * Creates and returns a new {@link IEnumValue} that has as many {@link IEnumAttributeValue
     * IEnumAttributeValues} as the corresponding {@link IEnumType} has {@link IEnumAttribute
     * IEnumAttributes}.
     * <p>
     * If the {@link IEnumType} referenced by this {@link IEnumValueContainer} cannot be found then
     * no {@link IEnumValue} will be created and null will be returned.
     * 
     * @throws IpsException If an error occurs while searching for the referenced {@link IEnumType}
     *             (in case this {@link IEnumValueContainer} is an {@link IEnumContent}.
     */
    IEnumValue newEnumValue() throws IpsException;

    /**
     * Returns a reference to the {@link IEnumType} or {@code null} if no {@link IEnumType} can be
     * found.
     * 
     * @param ipsProject The IPS project which IPS object path is used for the search. This is not
     *            necessarily the project this {@link IEnumAttribute} is part of.
     * 
     * @throws NullPointerException If {@code ipsProject} is {@code null} .
     */
    IEnumType findEnumType(IIpsProject ipsProject);

    /**
     * Returns how many {@link IEnumValue IEnumValues} this {@link IEnumValueContainer} currently
     * contains.
     */
    int getEnumValuesCount();

    /**
     * Moves the given {@link IEnumValue IEnumValues} up or down by 1 and returns the their new
     * positions. This operation assures that there aren't any {@link ContentChangeEvent
     * ContentChangeEvents} fired while moving the individual {@link IEnumValue IEnumValues}.
     * Instead, a {@code WHOLE_CONTENT_CHANGED} event will be fired after every {@link IEnumValue
     * IEnumValues} has been moved.
     * 
     * @param enumValuesToMove A list containing the {@link IEnumValue IEnumValues} that shall be
     *            moved.
     * @param up Flag indicating whether to move up ({@code true}) or down ({@code false} ).
     * 
     * @throws IpsException If an error occurs while moving the {@link IEnumValue IEnumValues}.
     * @throws NullPointerException If {@code enumValuesToMove} is {@code null}.
     * @throws NoSuchElementException If any of the given {@link IEnumValue IEnumValues} is not part
     *             of this {@link IEnumValueContainer}.
     */
    int[] moveEnumValues(List<IEnumValue> enumValuesToMove, boolean up) throws IpsException;

    /**
     * Returns the index of the given {@link IEnumValue} or -1 if the given {@link IEnumValue} does
     * not exist in this {@link IEnumValueContainer}.
     * 
     * @param enumValue The {@link IEnumValue} to obtain its index for.
     * 
     * @throws NullPointerException If {@code enumValue} is {@code null}.
     */
    int getIndexOfEnumValue(IEnumValue enumValue);

    /**
     * Deletes all {@link IEnumValue IEnumValues} from this {@link IEnumValueContainer}.
     */
    void clear();

    /**
     * Deletes the given {@link IEnumValue IEnumValues} from this {@link IEnumValueContainer}. This
     * operation assures that no {@link ContentChangeEvent ContentChangeEvents} are fired during the
     * deletion of the individual {@link IEnumValue IEnumValues}. Instead a
     * {@code WHOLE_CONTENT_CHANGED} event will be fired after every {@link IEnumValue} has been
     * deleted.
     * <p>
     * If {@code null} is given nothing will happen. If an {@link IEnumValue} is not part of this
     * {@link IEnumValueContainer} the {@link IEnumValue} will be skipped.
     * <p>
     * Returns {@code true} if any {@link IEnumValue IEnumValues} were deleted, {@code false} if not
     * (following the behavior of the Java collections here).
     * 
     * @param enumValuesToDelete A list containing all {@link IEnumValue IEnumValues} that should be
     *            deleted from this {@link IEnumValueContainer}.
     */
    boolean deleteEnumValues(List<IEnumValue> enumValuesToDelete);

    /**
     * Returns whether this {@link IEnumValueContainer} is currently capable of holding
     * {@link IEnumValue IEnumValues} ({@code true}) or not ({@code false}).
     * 
     */
    boolean isCapableOfContainingValues();

    /**
     * Fix the {@link ValueType} in {@link IEnumAttributeValue enum attribute values} for the
     * {@link IEnumAttribute}
     * 
     * @param enumAttribute the attribute you want to fix
     */
    void fixEnumAttributeValues(IEnumAttribute enumAttribute);

    /**
     * Fix the {@link ValueType} in {@link IEnumAttributeValue enum attribute values} for all
     * {@link IEnumAttribute enum attributes}.
     */
    void fixAllEnumAttributeValues();

    /**
     * Checks if there is an mismatch of the {@link ValueType} in {@link IEnumAttributeValue} of all
     * {@link IEnumAttribute enum attributes}
     * 
     * @return Map with the name of {@link IEnumAttribute} and {@link ValueTypeMismatch}
     */
    Map<String, ValueTypeMismatch> checkAllEnumAttributeValueTypeMismatch();

    /**
     * Checks if there is an mismatch of the {@link ValueType} in {@link IEnumAttributeValue} of the
     * {@link IEnumAttribute}
     * 
     * @param enumAttribute the attribute you want to check
     * @return {@link ValueTypeMismatch}
     */
    ValueTypeMismatch checkValueTypeMismatch(IEnumAttribute enumAttribute);

    /**
     * @return <code>true</code> if the IDs of values in this value container must be less than the
     *             identifier boundary defined in the corresponding enum type. <code>false</code> if
     *             the IDs must greater than or equal to the boundary.
     * @see IEnumType#getIdentifierBoundary()
     */
    boolean isIdentifierNamespaceBelowBoundary();
}
