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

package org.faktorips.devtools.core.util;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * A utility class for persistence (validation of table names, ..).
 * 
 * @author Roman Grutza
 */
public class PersistenceUtil {

    private static final Pattern NAME_PATTERN = Pattern.compile("[_\\p{Alpha}]\\w*");

    /**
     * Checks if given String is a valid table or column name.
     * <p/>
     * The name musst start with an ASCII character , thus starting with a character and then
     * containing only ASCII-characters, numbers and the underscore character.
     * 
     * @param name The String to check
     * @return <code>true</code> if name is valid, <code>false</code> otherwise
     */
    public static boolean isValidDatabaseIdentifier(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }
}
