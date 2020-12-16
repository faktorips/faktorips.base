/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.util;

import java.lang.ref.SoftReference;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.faktorips.annotation.UtilityClass;
import org.faktorips.values.NullObject;

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
 * In order to have a fallback if a message was requested in a not existing locale
 * {@link ResourceBundle} provides a strategy to find a message anyway: If the message was not found
 * the fallback strategy will try to find a resource for the system default language. If this also
 * does not succeed a resource bundle without language specification would be taken. This leads to a
 * bad behavior: Imagine you provide English as your default language and hence having your English
 * translation in a property file without locale suffix. Additionally you provide a German
 * translation with suffix _de. Now you start your runtime with system default language German but
 * you try to get a message in English. You would always get the German one because the strategy
 * does not find a resource for _en but finds one for _de (your system default). The file without
 * suffix would never be read. To avoid this problem we recommend to add a language suffix to every
 * resource. Additionally to get a fallback to your default language you have to provide the default
 * language to the {@link MessagesHelper}. When calling {@link #getMessage(String, Locale)}) the we
 * first check for the primary language, second then the {@link ResourceBundle} will check for
 * system default language and third we would provide the message in specified default language.
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
@UtilityClass
public class MessagesHelper {

    /** The qualified name. */
    private String name;

    /** The classloader used to access the property files. */
    private ClassLoader loader;

    private final Locale defaultLocale;

    /**
     * Creates a new StringsSet with the indicated qualified name. The property files are loaded
     * with the indicated classloader.
     * 
     * @param qualifiedName The qualified name of your resource without suffix nor extension for
     *            ".properties" example org.sample.messages
     * @param loader The {@link ClassLoader} to load the {@link ResourceBundle}
     * @param defaultLocale If no message was found the system default locale is used as fallback.
     *            If there is also no resource bundle in system's default language we try to find a
     *            message in defaultLocale
     */
    public MessagesHelper(String qualifiedName, ClassLoader loader, Locale defaultLocale) {
        this.name = qualifiedName;
        this.loader = loader;
        this.defaultLocale = defaultLocale;
    }

    /**
     * Getting the message for the given key in the specified locale.
     * 
     * @param key the key of the message
     * @param locale the locale of the message you want to get
     * @return the translated message located in the property file
     */
    public String getMessage(String key, Locale locale) {
        return getMessageInternal(key, locale);
    }

    private String getMessageInternal(String key, Locale locale) {
        try {
            return ResourceBundle.getBundle(name, locale, loader).getString(key);
        } catch (MissingResourceException e) {
            return ResourceBundle.getBundle(name, defaultLocale, loader).getString(key);
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
        IpsMessageFormat mf = new IpsMessageFormat(s, locale);
        return mf.format(replacements);
    }

    /**
     * Getting the message for the given key in the specified language. If there is no message in
     * the specified language the message helper searches the key in the default language. If there
     * is not message for the specified key in the requested language nor the default language, this
     * message returns the specified fallback text.
     * 
     * @param key The key to identify the message
     * @param locale the locale of the expected message
     * @param fallBack a fall back text if there is no message
     * 
     * @return The message for the specified key
     */
    public String getMessageOr(String key, Locale locale, String fallBack) {
        try {
            return getMessageInternal(key, locale);
        } catch (MissingResourceException e) {
            return fallBack;
        }
    }

    private static class IpsMessageFormat extends Format {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 1L;

        private final MessageFormat delegateFormat;

        public IpsMessageFormat(String pattern, Locale locale) {
            delegateFormat = new MessageFormat(pattern, locale);
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            if (obj.getClass().isArray()) {
                return delegateFormat.format(handleNullObjects((Object[])obj), toAppendTo, pos);
            } else {
                return delegateFormat.format(handleNullObject(obj), toAppendTo, pos);
            }
        }

        private Object[] handleNullObjects(Object[] arguments) {
            Object[] result = Arrays.copyOf(arguments, arguments.length);
            for (int i = 0; i < arguments.length; i++) {
                result[i] = handleNullObject(arguments[i]);
            }
            return result;
        }

        private Object handleNullObject(Object argument) {
            if (argument instanceof NullObject) {
                return null;
            } else {
                return argument;
            }
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return delegateFormat.parse(source, pos);
        }

    }

}
