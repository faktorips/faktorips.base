/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.util.Locale;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.fl.FunctionResolver;

/**
 * FunctionResolverFactories can be registered with the <em>flfunctionResolverFactory</em> extension
 * point. The function resolvers of the registered factories augment the set of available formula
 * language functions.
 * 
 * @author Peter Erzberger
 */
public interface IFunctionResolverFactory<T extends CodeFragment> {

    /**
     * Creates a new FunctionResolver with respect to the provided local. It is in the
     * responsibility of the factory provider if the locale is considered.
     */
    FunctionResolver<T> newFunctionResolver(Locale locale);

}
