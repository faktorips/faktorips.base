/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;

/**
 * A described element is an element that supports attaching {@link IDescription IDescriptions} in
 * different languages to it.
 * <p>
 * Descriptions can be attached for all languages that are supported by the IPS project the
 * described element belongs to.
 * 
 * @since 3.1
 * 
 * @author Alexander Weickmann
 * 
 * @see IDescription
 * @see IIpsProjectProperties#getSupportedLanguages()
 */
public interface IDescribedElement extends IIpsElement {

    /**
     * Returns the {@link IDescription} for the given {@link Locale}.
     * <p>
     * If no description for the locale exists, null is returned.
     * 
     * @param locale The locale to retrieve the description for
     * 
     * @throws NullPointerException If the parameter is null
     */
    IDescription getDescription(Locale locale);

    /**
     * Returns the text of the {@link IDescription} that has the given {@link Locale}.
     * <p>
     * An empty string is returned if no description for the locale exists.
     * 
     * @param locale The {@link Locale} of the {@link IDescription} to retrieve the text from
     * 
     * @throws NullPointerException If the parameter is null
     */
    String getDescriptionText(Locale locale);

    /**
     * Returns the list of descriptions this element currently has attached.
     * <p>
     * Note that only a defensive copy is returned. The descriptions are ordered according to the
     * order of the supported languages as they occur in the {@code .ipsproject} file.
     */
    List<IDescription> getDescriptions();

    /**
     * Creates a new {@link IDescription} for this element.
     */
    IDescription newDescription();

    /**
     * Sets the text of the {@link IDescription} that has the given {@link Locale}.
     * 
     * @param locale The {@link Locale} of the {@link IDescription} to set the text for
     * @param text The text to set or null to set the description text to the empty string
     * 
     * @throws NullPointerException If the parameter locale is null
     * @throws IllegalArgumentException If there is no description with the given locale
     */
    void setDescriptionText(Locale locale, String text);

}
