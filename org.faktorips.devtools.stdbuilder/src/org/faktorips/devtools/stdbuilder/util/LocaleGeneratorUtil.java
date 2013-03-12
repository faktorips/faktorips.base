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

package org.faktorips.devtools.stdbuilder.util;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;

public final class LocaleGeneratorUtil {

    private LocaleGeneratorUtil() {
        // do not instantiate
    }

    /**
     * Returns a {@link JavaCodeFragment} that represents the java code of the given locale.
     */
    public static JavaCodeFragment getLocaleCodeFragment(Locale locale) {
        JavaCodeFragment result = new JavaCodeFragment();
        result.appendClassName(Locale.class);
        if (Locale.GERMAN.equals(locale)) {
            result.append(".GERMAN");
        } else if (Locale.ENGLISH.equals(locale)) {
            result.append(".ENGLISH");
        } else if (Locale.FRENCH.equals(locale)) {
            result.append(".FRENCH");
        } else {
            result = new JavaCodeFragment();
            result.append("new ").appendClassName(Locale.class).append("(\"").append(locale.getLanguage())
                    .append("\")");
        }
        return result;
    }

}
