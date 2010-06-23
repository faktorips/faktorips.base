/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.util;

import java.text.MessageFormat;
import java.util.Locale;
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

    public String getString(String key, Locale locale, Object[] replacements) {
        String s = ResourceBundle.getBundle(name, locale, loader).getString(key);
        MessageFormat mf = new MessageFormat(s, locale);
        return mf.format(replacements);
    }

    public String getString(String key, Locale locale, Object replacement) {
        return getString(key, locale, new Object[] { replacement });
    }

    public String getString(String key, Locale locale) {
        return ResourceBundle.getBundle(name, locale, loader).getString(key);
    }

    public String getString(String key, Object[] replacements) {
        return getString(key, Locale.getDefault(), replacements);
    }

    public String getString(String key, Object replacement) {
        return getString(key, Locale.getDefault(), replacement);
    }

    public String getString(String key) {
        return ResourceBundle.getBundle(name, Locale.getDefault(), loader).getString(key);
    }

}
