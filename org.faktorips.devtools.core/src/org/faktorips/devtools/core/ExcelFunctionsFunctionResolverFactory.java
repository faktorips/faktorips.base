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

package org.faktorips.devtools.core;

import java.util.Locale;

import org.faktorips.fl.ExcelFunctionsResolver;
import org.faktorips.fl.FunctionResolver;

/**
 * A function resolver factory that creates a function resolver for a set of functions similar to
 * excel functions.
 * 
 * @author Peter Erzberger
 */
public class ExcelFunctionsFunctionResolverFactory implements IFunctionResolverFactory {

    /**
     * Returns a function resolver factory that contains a set of functions similar to a subset of
     * functions found in excel.
     */
    @Override
    public FunctionResolver newFunctionResolver(Locale locale) {
        return new ExcelFunctionsResolver(locale);
    }

}
