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

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.fl.functions.Exists;

/**
 * A {@link FunctionResolver} that supports association navigation {@link FlFunction functions}. The
 * functions are available in different languages.
 */
public class AssociationNavigationFunctionsResolver extends LocalizedFunctionsResolver {

    public static final String EXISTS = "exists";

    /**
     * Creates a new resolver that contains a set of functions that support association navigation.
     * 
     * @param locale The locale that determines the language of the function names.
     */
    public AssociationNavigationFunctionsResolver(Locale locale) {
        super(locale);
        add(new Exists(getFctName(EXISTS), getFctDescription(EXISTS)));
    }

    @Override
    public String toString() {
        return AssociationNavigationFunctionsResolver.class.getSimpleName();
    }

    @Override
    protected String getLocalizationFileBaseName() {
        return "org.faktorips.fl.AssociationNavigationFunctions"; //$NON-NLS-1$
    }

}
