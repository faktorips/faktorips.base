/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.Collection;
import java.util.Locale;

import org.faktorips.devtools.model.internal.InternationalString;
import org.faktorips.values.LocalizedString;

/**
 * A {@link IInternationalString} could be used for string properties that could be translated in
 * different languages. The {@link IInternationalString} consists of a set of
 * {@link LocalizedString}. Setting a {@link LocalizedString} means to set the string for a specific
 * language.
 * <p>
 * The {@link IInternationalString} has {@link XmlSupport} to save and load it.
 * 
 * @author dirmeier
 */
public interface IInternationalString extends XmlSupport, Comparable<IInternationalString> {

    public static final String XML_TAG = "InternationalString"; //$NON-NLS-1$

    public static final String XML_ELEMENT_LOCALIZED_STRING = "LocalizedString"; //$NON-NLS-1$

    public static final String XML_ATTR_LOCALE = "locale"; //$NON-NLS-1$

    /**
     * The name of the attribute for the default locale. Although this class does not support a
     * default locale, such a locale is needed when
     * {@link org.faktorips.values.DefaultInternationalString} is initialized from the XML written
     * by this class.
     * 
     * @see InternationalString
     */
    public static final String XML_ATTR_DEFAULT_LOCALE = "defaultLocale"; //$NON-NLS-1$

    public static final String XML_ATTR_TEXT = "text"; //$NON-NLS-1$

    /**
     * Getting the {@link LocalizedString} for the specified locale. Returns a new
     * {@link LocalizedString} with an empty String value if there is no existing one for the
     * specified locale.
     * 
     * @param locale the locale of the text you want to get
     * @return return the text for the specified locale or an empty {@link LocalizedString} if no
     *         such text exists
     * @see #hasValueFor(Locale)
     */
    LocalizedString get(Locale locale);

    /**
     * Adding the specified {@link LocalizedString}. It is not allowed to add null. If you add an
     * {@link LocalizedString} with a null value it is automatically converted to an empty
     * {@link LocalizedString}
     * 
     * @param localizedString the {@link LocalizedString} specifying the locale and text you want to
     *            set
     */
    void add(LocalizedString localizedString);

    /**
     * Getting all {@link LocalizedString} stored in this {@link IInternationalString}
     * 
     * @return a collection of {@link LocalizedString} that is stored in this
     *         {@link IInternationalString}
     */
    Collection<LocalizedString> values();

    /**
     * Returns whether this {@link IInternationalString} contains a {@link LocalizedString} for the
     * given {@link Locale}.
     *
     * @since 21.12
     */
    boolean hasValueFor(Locale locale);

}