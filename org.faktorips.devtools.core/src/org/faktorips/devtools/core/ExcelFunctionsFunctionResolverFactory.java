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
    public FunctionResolver newFunctionResolver(Locale locale) {
        return new ExcelFunctionsResolver(locale);
    }

}
