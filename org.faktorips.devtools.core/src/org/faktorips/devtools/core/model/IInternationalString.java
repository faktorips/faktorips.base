/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model;

import java.util.Collection;
import java.util.Locale;

import org.faktorips.devtools.core.internal.model.LocalizedString;

/**
 * A {@link IInternationalString} could be used for string properties that could be translated in
 * different languages. The {@link IInternationalString} consists of a set of
 * {@link LocalizedString}. Setting a {@link ILocalizedString} means to set the string for a
 * specific language.
 * <p>
 * The {@link IInternationalString} has {@link XmlSupport} to save and load it.
 * 
 * @author dirmeier
 */
public interface IInternationalString extends XmlSupport {

    /**
     * Getting the {@link LocalizedString} for the specified locale. Returns null if no text exists
     * for the specified locale.
     * 
     * @param locale the locale of the text you want to get
     * @return return the text for the specified locale or null if no such text exists
     */
    ILocalizedString get(Locale locale);

    /**
     * Adding the specified {@link LocalizedString}. It is not allowed to add null.
     * 
     * @param localizedString the {@link LocalizedString} specifying the locale and text you want to
     *            set
     */
    void add(ILocalizedString localizedString);

    /**
     * Getting all {@link LocalizedString} stored in this {@link IInternationalString}
     * 
     * @return a collection of {@link LocalizedString} that is stored in this
     *         {@link IInternationalString}
     */
    Collection<ILocalizedString> values();

}