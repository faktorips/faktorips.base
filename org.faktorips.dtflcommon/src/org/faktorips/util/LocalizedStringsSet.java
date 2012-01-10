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

package org.faktorips.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A LocalizedStringsSet is a set of strings available in different locales. A localized string can
 * be looked up by a locale independant key and a {@link java.util.Locale locale}.
 * <p>
 * The locale dependent strings have to be stored in property files. They are accessed via Java's
 * {@link java.util.ResourceBundle ResourceBundle}. To access the property files, they all have to
 * be in the same package and must start with the same prefix. Therefore the set's qualified name is
 * the package name, followed by a dot (.), followed by the prefix, e.g.
 * "org.faktorips.fl.Messages". For further information see the {@link java.util.ResourceBundle
 * ResourceBundle} documentation.
 * <p>
 * If a localized String contains sections that have to be replaced with locale dependant
 * replacements before the String is presented to a user, you can use one of the methods taking
 * replacement objects as a parameter. The mechanism used here is the one of
 * {@link java.text.MessageFormat}.
 * <p>
 * Example:
 * <p>
 * In the message "The sum insured must be at least {0}." the sum insured must be inserted at
 * runtime depending on the chosen product.
 */
public class LocalizedStringsSet {

    /** The qualified name. */
    private String name;

    /** The classloader used to access the property files. */
    private ClassLoader loader;

    /**
     * Creates a new set that uses the indicated class' name as qualified name.
     * 
     * @throws IllegalArgumentException if the clazz is null.
     */
    public LocalizedStringsSet(Class<?> clazz) {
        this(clazz.getName(), clazz.getClassLoader());
    }

    /**
     * Creates a new StringsSet with the indicated qualified name. The property files are loaded
     * with the indicated classloader.
     * 
     * @throws IllegalArgumentException if the qualifiedName is null.
     */
    public LocalizedStringsSet(String qualifiedName, ClassLoader loader) {
        ArgumentCheck.notNull(qualifiedName);
        ArgumentCheck.notNull(loader);
        this.name = qualifiedName;
        this.loader = loader;
    }

    /**
     * Checking whether the resource is accessible in given locale. That does not necessary means
     * that the resource is available in exactly this language when there is a default resource.
     * 
     * @param locale The locale you want be check.
     * @return true if there is a resource bundle could be found for the given locale
     * 
     * @see ResourceBundle#getBundle(String)
     */
    public boolean isAccessible(Locale locale) {
        try {
            ResourceBundle.getBundle(name, locale, loader);
            return true;
        } catch (MissingResourceException e) {
            return false;
        }
    }

    /**
     * Try to load the resource bundle in given locale and searches for the given key. This method
     * also replaces the replacement parameters in the found message by using the
     * {@link MessageFormat}.
     * 
     * @param key The key of the message
     * @param locale the locale of the message
     * @param replacements optional replacement parameters
     * @return the message for the key in specified locale
     * @throws MissingResourceException if the resource or the specified key is not found
     * 
     * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
     * @see MessageFormat#format(Object)
     */
    public String getString(String key, Locale locale, Object... replacements) {
        String s = ResourceBundle.getBundle(name, locale, loader).getString(key);
        MessageFormat mf = new MessageFormat(s, locale);
        return mf.format(replacements);
    }

    /**
     * Try to load the resource bundle in given locale and searches for the given key.
     * 
     * @param key The key of the message
     * @param locale the locale of the message
     * @return the message for the key in specified locale
     * @throws MissingResourceException if the resource or the specified key is not found
     * 
     * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
     */
    public String getString(String key, Locale locale) {
        return ResourceBundle.getBundle(name, locale, loader).getString(key);
    }

    /**
     * Try to load the resource bundle in the jvm's default language and searches for the given key.
     * This method also replaces the replacement parameters in the found message by using the
     * {@link MessageFormat}.
     * 
     * @param key The key of the message
     * @param replacements optional replacement parameters
     * @return the message for the key in specified locale
     * @throws MissingResourceException if the resource or the specified key is not found
     * 
     * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
     * @see MessageFormat#format(Object)
     */
    public String getString(String key, Object... replacements) {
        return getString(key, Locale.getDefault(), replacements);
    }

    /**
     * Try to load the resource bundle in the jvm's default language and searches for the given key.
     * 
     * @param key The key of the message
     * @return the message for the key in specified locale
     * @throws MissingResourceException if the resource or the specified key is not found
     * 
     * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
     */
    public String getString(String key) {
        return ResourceBundle.getBundle(name, Locale.getDefault(), loader).getString(key);
    }

}
