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

package org.faktorips.devtools.core.model;

import java.util.Collection;
import java.util.Locale;

/**
 * A {@link IInternationalString} could be used for string properties that could be translated in
 * different languages. The {@link IInternationalString} consists of a set of
 * {@link ILocalizedString}. Setting a {@link ILocalizedString} means to set the string for a
 * specific language.
 * <p>
 * The {@link IInternationalString} has {@link XmlSupport} to save and load it.
 * 
 * @author dirmeier
 */
public interface IInternationalString extends XmlSupport {

    /**
     * Getting the {@link ILocalizedString} for the specified locale. Returns a new
     * {@link ILocalizedString} with an empty String value if there is no existing one for the
     * specified locale.
     * 
     * @param locale the locale of the text you want to get
     * @return return the text for the specified locale or an empty {@link ILocalizedString} if no
     *         such text exists
     */
    ILocalizedString get(Locale locale);

    /**
     * Adding the specified {@link ILocalizedString}. It is not allowed to add null. If you add an
     * {@link ILocalizedString} with a null value it is automatically converted to an empty
     * {@link ILocalizedString}
     * 
     * @param localizedString the {@link ILocalizedString} specifying the locale and text you want
     *            to set
     */
    void add(ILocalizedString localizedString);

    /**
     * Getting all {@link ILocalizedString} stored in this {@link IInternationalString}
     * 
     * @return a collection of {@link ILocalizedString} that is stored in this
     *         {@link IInternationalString}
     */
    Collection<ILocalizedString> values();

}