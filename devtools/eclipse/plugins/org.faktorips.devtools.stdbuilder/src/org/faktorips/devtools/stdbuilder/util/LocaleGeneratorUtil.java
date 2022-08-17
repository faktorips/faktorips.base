/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
