/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.inputformat;

/**
 * Implementations of this interface are registered by the extension point "InputFormat". The
 * {@link IInputFormat} determines for a given input if the entered Datatype is supported.
 * 
 * 
 */
public interface IInputFormat<T> {

    /**
     * Calls the initFormat with the input locale configured in the preferences.
     */
    public void initFormat();

    /**
     * Parses a string to a value object. Supports the FIPS null-Presentation mechanism.
     * 
     * @param stringToBeParsed the String typed by the user that will be parsed to a value of the
     *            data type this format adheres to.
     * @param supportNull whether or not this method should return <code>null</code> if the string
     *            matches the null-presentation value.
     * @return the value object that was parsed from the users input
     */
    public T parse(String stringToBeParsed, boolean supportNull);

    /**
     * Returns a formatted string for the given value object. Supports the FIPS null-Presentation
     * mechanism.
     * 
     * @param objectValue the value to be formatted for display
     * @param supportNull whether or not this method should return null-presentation value in case
     *            the given object value is <code>null</code>.
     * @return the formatted string representing the given value
     */
    public String format(T objectValue, boolean supportNull);

    /**
     * Parses a string to a value object. Semantically equivalent to parse(String stringToBeparsed,
     * true).
     * 
     * @param stringToBeParsed the String typed by the user that will be parsed to a value of the
     *            data type this format adheres to.
     * @return the value object that was parsed from the users input
     */
    public T parse(String stringToBeParsed);

    /**
     * 
     * Returns a formatted string for the given value object. Semantically equivalent to
     * format(Object objectValue, true).
     * 
     * @param objectValue the value to be formatted for display
     * @return the formatted string representing the given value
     */
    public String format(T objectValue);

}