/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.util.LocalizedStringsSet;

/**
 * A {@link FunctionResolver} that supports localized names for {@link FlFunction functions}.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public abstract class LocalizedFunctionsResolver<T extends CodeFragment> extends DefaultFunctionResolver<T> {

    private LocalizedStringsSet localizedStrings;

    // the locale used for function names and descriptions.
    private Locale locale;

    /**
     * Creates a new resolver that contains a set of functions that use locale dependent names.
     * 
     * @param locale The locale that determines the language of the function names.
     */
    public LocalizedFunctionsResolver(Locale locale) {
        super();
        this.locale = locale;
        localizedStrings = new LocalizedStringsSet(getLocalizationFileBaseName(), getClass().getClassLoader());
    }

    /**
     * Returns the base name for the localization files, for example
     * {@code "org.faktorips.fl.MyFunctionResolver"} where localization files are
     * {@code "org/faktorips/fl/MyFunctionResolver_de.properties"} and
     * {@code "org/faktorips/fl/MyFunctionResolver_en.properties"}
     * 
     * @return the base name for the localization files
     */
    protected abstract String getLocalizationFileBaseName();

    /**
     * Returns the localized name for the {@link FlFunction} identified by the key.
     * 
     * @param key the key of the {@link FlFunction}.
     * @return the localized name for the {@link FlFunction}
     */
    public String getFctName(String key) {
        return localizedStrings.getString(key + ".name", locale); //$NON-NLS-1$
    }

    /**
     * Returns the localized description for the {@link FlFunction} identified by the key.
     * 
     * @param key the key of the {@link FlFunction}.
     * @return the localized description for the {@link FlFunction}
     */
    public String getFctDescription(String key) {
        return localizedStrings.getString(key + ".description", locale); //$NON-NLS-1$
    }

    class NameDescription {

        String name;
        String description;

        NameDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }

    }

}
