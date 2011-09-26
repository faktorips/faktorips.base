/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.util;

import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A MessagesHelper is a set of strings available in different locales. A localized string can be
 * looked up by a locale independent key and a {@link java.util.Locale locale}.
 * <p>
 * The locale dependent strings have to be stored in property files. They are accessed via Java's
 * {@link java.util.ResourceBundle ResourceBundle}. To access the property files, they all have to
 * be in the same package and must start with the same prefix. Therefore the set's qualified name is
 * the package name, followed by a dot (.), followed by the prefix, e.g.
 * "org.faktorips.internal.messages". For further information see the
 * {@link java.util.ResourceBundle ResourceBundle} documentation.
 * <p>
 * If a localized String contains sections that have to be replaced with replacements before the
 * String is presented to a user, you can use the method with replacement objects as a parameter.
 * The mechanism used here is the one of {@link java.text.MessageFormat}.
 * <p>
 * The property file loaded by the {@link ResourceBundle} is cached internally by
 * {@link SoftReference}s. So you do not have to worry about performance problems instantiation this
 * class multiple times.
 * <p>
 * Example:
 * <p>
 * In the message "The sum insured must be at least {0}." the sum insured must be inserted at
 * runtime depending on the chosen product.
 */
public class MessagesHelper {

    /** The qualified name. */
    private String name;

    /** The classloader used to access the property files. */
    private ClassLoader loader;

    /**
     * Creates a new StringsSet with the indicated qualified name. The property files are loaded
     * with the indicated classloader.
     * 
     * @throws IllegalArgumentException if the qualifiedName is null.
     */
    public MessagesHelper(String qualifiedName, ClassLoader loader) {
        this.name = qualifiedName;
        this.loader = loader;
    }

    /**
     * Getting the message for the given key in the specified locale
     * 
     * @param key the key of the message
     * @param locale the locale of the message you want to get
     * @return the translated message located in the property file
     */
    public String getMessage(String key, Locale locale) {
        try {
            return ResourceBundle.getBundle(name, locale, loader).getString(key);
        } catch (MissingResourceException e) {
            return "";
        }
    }

    /**
     * Getting the message for the given key in the specified language. For every replacement
     * parameter there must be a replacement mark (e.g. {0}) in the message. @see
     * {@link MessageFormat}.
     * 
     * @param key The key of the message
     * @param locale The locale of the message you want to get
     * @param replacements the replacements in the message text
     * @return the translated message located in the property file with replaced parameters
     */
    public String getMessage(String key, Locale locale, Object... replacements) {
        String s = getMessage(key, locale);
        MessageFormat mf = new MessageFormat(s, locale);
        return mf.format(replacements);
    }

}
