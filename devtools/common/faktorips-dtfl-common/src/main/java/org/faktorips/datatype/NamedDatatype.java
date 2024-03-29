/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.datatype;

import java.util.Locale;

/**
 * A {@link ValueDatatype} that can have a human readable name (for example currency symbols like $
 * and € or full names like "annual payment" or "monthly payment") for each of its values. That name
 * can be displayed in addition to or instead of the actual value (like USD or EUR or a payment mode
 * 1 or 12), which is used as the technical ID and to persist values of the datatype.
 * <p>
 * Note that due to the inheritance hierarchy of datatypes, presence of this interface is no
 * guarantee that the implementation actually supports names - only those that return {@code true}
 * from their {@link #isSupportingNames()} method do.
 */
public interface NamedDatatype extends ValueDatatype {

    /**
     * Returns {@code true} if an implementation of this interface supports names that describe the
     * datatype's value. E.g. an enum datatype PaymentMode might return the name "annual" for the
     * annual payment mode with ID "1". If this method returns {@code false} a call to the
     * {@link #getValueName(String)} method is supposed to throw an {@link IllegalStateException}.
     */
    boolean isSupportingNames();

    /**
     * Returns a short description of the value of this datatype specified by the ID.
     * 
     * @throws IllegalArgumentException if the given ID is not a valid ID of this datatype
     * @throws IllegalStateException if the datatype {@link #isSupportingNames() does not support
     *             names}
     */
    String getValueName(String id);

    /**
     * Returns a short description of the value of this datatype specified by the ID. The
     * {@link Locale} can be used for internationalization. If no locale is provide the
     * {@code Locale.getDefault()} will be used.
     *
     * @param id the ID for the value
     * @param locale the locale used for internationalization
     * 
     * @throws IllegalArgumentException if the given ID is not a valid ID of this datatype
     * @throws IllegalStateException if the datatype {@link #isSupportingNames() does not support
     *             names}
     */
    default String getValueName(String id, Locale locale) {
        return getValueName(id);
    }

    /**
     * This method parses the given string and returns the value as an instance of the class this
     * value datatype represents.
     * <p>
     * The difference to {@link #getValue(String)} is that the given string is not a representation
     * of the ID (as used by {@link #getValue(String)}) but of the name as returned by
     * {@link #getValueName(String)}.
     * <p>
     * Use with caution: During development time Faktor-IPS maintains all values with their string
     * representation. This allows to change the value's datatype without the need to convert the
     * value from one class to another (e.g. if the string representation is 42 you can change the
     * datatype from integer to string without converting the integer object to a string object.
     * 
     * @param name the name representation of a value
     * @return the value as instance of the class this datatype represents
     */
    Object getValueByName(String name);

    /**
     * This method parses the given string and returns the value as an instance of the class this
     * value datatype represents. The {@link Locale} can be used for internationalization. If no
     * locale is provide the {@code Locale.getDefault()} will be used.
     * <p>
     * The difference to {@link #getValue(String)} is that the given string is not a representation
     * of the ID (as used by {@link #getValue(String)}) but of the name as returned by
     * {@link #getValueName(String,Locale)}.
     * <p>
     * Use with caution: During development time Faktor-IPS maintains all values with their string
     * representation. This allows to change the value's datatype without the need to convert the
     * value from one class to another (e.g. if the string representation is 42 you can change the
     * datatype from integer to string without converting the integer object to a string object.
     * 
     * @param name the name representation of a value
     * @param locale the locale used for internationalization
     * @return the value as instance of the class this datatype represents
     */
    default Object getValueByName(String name, Locale locale) {
        return getValueByName(name);
    }

}
