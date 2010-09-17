/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.ipsobject;

import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;

/**
 * A described element is an element that supports attaching {@link IDescription}s in different
 * languages to it. Descriptions can be attached for all languages that are supported by the IPS
 * project the described element belongs to.
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
    public IDescription getDescription(Locale locale);

    /**
     * Returns the text of the {@link IDescription} that has the given {@link Locale}.
     * <p>
     * An empty string is returned if no description for the locale exists.
     * 
     * @param locale The {@link Locale} of the {@link IDescription} to retrieve the text from
     * 
     * @throws NullPointerException If the parameter is null
     */
    public String getDescriptionText(Locale locale);

    /**
     * Returns the list of descriptions this element currently has attached.
     * <p>
     * Note that only a defensive copy is returned. The descriptions are ordered according to the
     * order of the supported languages as they occur in the {@code .ipsproject} file.
     */
    public List<IDescription> getDescriptions();

    /**
     * Creates a new {@link IDescription} for this element.
     */
    public IDescription newDescription();

    /**
     * Sets the text of the {@link IDescription} that has the given {@link Locale}.
     * 
     * @param locale The {@link Locale} of the {@link IDescription} to set the text for
     * @param text The text to set or null to set the description text to the empty string
     * 
     * @throws NullPointerException If the parameter locale is null
     * @throws IllegalArgumentException If there is no description with the given locale
     */
    public void setDescriptionText(Locale locale, String text);

}
