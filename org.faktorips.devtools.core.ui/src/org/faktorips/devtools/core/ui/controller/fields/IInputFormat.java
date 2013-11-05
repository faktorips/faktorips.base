/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controller.fields;

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
     * @param stringToBeparsed the String typed by the user that will be parsed to a value of the
     *            data type this format adheres to.
     * @return the value object that was parsed from the users input
     */
    public T parse(String stringToBeparsed);

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