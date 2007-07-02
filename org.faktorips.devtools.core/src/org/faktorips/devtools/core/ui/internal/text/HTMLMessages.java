/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.text;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.ibm.icu.text.MessageFormat;


/**
 * Helper class to get NLSed messages.
 *
 * @since 3.3
 */
class HTMLMessages {

    private static final String RESOURCE_BUNDLE= HTMLMessages.class.getName();

    private static ResourceBundle fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);

    private HTMLMessages() {
    }

    /**
     * Gets a string from the resource bundle.
     *
     * @param key the string used to get the bundle value, must not be null
     * @return the string from the resource bundle
     */
    public static String getString(String key) {
        try {
            return fgResourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
        }
    }

    /**
     * Gets a string from the resource bundle and formats it with the given arguments.
     *
     * @param key the string used to get the bundle value, must not be null
     * @param args the arguments used to format the string
     * @return the formatted string
     */
    public static String getFormattedString(String key, Object[] args) {
        String format= null;
        try {
            format= fgResourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
        }
        return java.text.MessageFormat.format(format, args);
    }

    /**
     * Gets a string from the resource bundle and formats it with the given argument.
     *
     * @param key the string used to get the bundle value, must not be null
     * @param arg the argument used to format the string
     * @return the formatted string
     */
    public static String getFormattedString(String key, Object arg) {
        String format= null;
        try {
            format= fgResourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
        }
        if (arg == null)
            arg= ""; //$NON-NLS-1$
        return MessageFormat.format(format, new Object[] { arg });
    }
}
