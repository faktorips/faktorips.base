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
import org.faktorips.fl.AssociationNavigationFunctionsResolver;
import org.faktorips.fl.FunctionResolver;

/**
 * A {@link IFunctionResolverFactory function resolver factory} that creates a function resolver for
 * a set of functions for association navigation.
 */
public class AssociationNavigationFunctionsResolverFactory implements IFunctionResolverFactory<JavaCodeFragment> {

    /**
     * Returns a function resolver factory that creates a function resolver for a set of functions
     * for association navigation.
     */
    @Override
    public FunctionResolver<JavaCodeFragment> newFunctionResolver(Locale locale) {
        return new AssociationNavigationFunctionsResolver(locale);
    }

}
