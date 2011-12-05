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

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.util.LocalizedStringsSet;

/**
 * A {@link FunctionResolver} that supports localized names for {@link FlFunction functions}.
 */
public abstract class LocalizedFunctionsResolver extends DefaultFunctionResolver {

    private LocalizedStringsSet localizedStrings;

    // the locale used for function names and descriptions.
    private Locale locale;

    /**
     * Creates a new resolver that contains a set of functions that are similiar by name and
     * argument list as those provided by Microsoft's Excel.
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
    abstract protected String getLocalizationFileBaseName();

    protected String getFctName(String key) {
        return localizedStrings.getString(key + ".name", locale); //$NON-NLS-1$
    }

    protected String getFctDescription(String key) {
        return localizedStrings.getString(key + ".description", locale); //$NON-NLS-1$
    }

    class NameDescription {

        NameDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }

        String name;
        String description;
    }

}
