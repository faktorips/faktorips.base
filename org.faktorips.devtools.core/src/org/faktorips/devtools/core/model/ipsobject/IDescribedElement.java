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

import java.util.Locale;
import java.util.Set;

import org.faktorips.devtools.core.IpsPlugin;
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
public interface IDescribedElement {

    /**
     * Returns the {@link IDescription} for the given {@link Locale}. If no description for the
     * locale exists, <tt>null</tt> is returned.
     * 
     * @param locale The locale to retrieve the description for.
     * 
     * @throws NullPointerException If <tt>locale</tt> is <tt>null</tt>.
     */
    public IDescription getDescription(Locale locale);

    /**
     * Returns an unmodifiable view on the set of descriptions of this element.
     */
    public Set<IDescription> getDescriptions();

    /**
     * Returns the description for the locale that Faktor-IPS uses at the time this operation is
     * called to internationalize Faktor-IPS models.
     * <p>
     * If there is no description for that locale, <tt>null</tt> is returned.
     * 
     * @see IpsPlugin#getIpsModelLocale()
     */
    public IDescription getDescriptionForIpsModelLocale();

    /**
     * Returns the description for the default language. The default language is specified trough
     * the IPS project. Returns <tt>null</tt> if no description for the default language exists or
     * no default language is specified.
     */
    public IDescription getDescriptionForDefaultLocale();

    /**
     * Creates a new description for this element.
     */
    public IDescription newDescription();

    /**
     * Returns the text of the element's current {@link IDescription}. That is primarily the
     * description for the IPS model locale as returned by
     * {@link #getDescriptionForIpsModelLocale()}.
     * <p>
     * Should no {@link IDescription} exist for that locale, the next in question is the description
     * for the default locale as returned by {@link #getDescriptionForDefaultLocale()}.
     * <p>
     * Should no description exist for that locale as well, an empty string is returned.
     */
    public String getCurrentDescription();

}
