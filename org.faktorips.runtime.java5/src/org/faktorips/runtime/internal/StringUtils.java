/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

/**
 * A collection of utility methods for Strings. We don't use a class library like apache-commons
 * here to minimize the dependencies for the generated code.
 * 
 * @author Jan Ortmann
 */
public class StringUtils {

    public static final String EMPTY = "";

    private StringUtils() {
        // Utility class not to be instantiated.
    }

    /**
     * Returns <code>true</code> if s is either null or the empty string, otherwise
     * <code>false</code>.
     */
    public static final boolean isEmpty(String s) {
        return s == null || EMPTY.equals(s.trim());
    }

}
