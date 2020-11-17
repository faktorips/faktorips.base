/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.fl;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.fl.ExcelFunctionsResolver;
import org.faktorips.fl.FunctionResolver;

/**
 * A function resolver factory that creates a function resolver for a set of functions similar to
 * excel functions.
 * 
 * @author Peter Erzberger
 */
public class ExcelFunctionsFunctionResolverFactory implements IFunctionResolverFactory<JavaCodeFragment> {

    /**
     * Returns a function resolver factory that contains a set of functions similar to a subset of
     * functions found in excel.
     */
    @Override
    public FunctionResolver<JavaCodeFragment> newFunctionResolver(Locale locale) {
        return new ExcelFunctionsResolver(locale);
    }

}
