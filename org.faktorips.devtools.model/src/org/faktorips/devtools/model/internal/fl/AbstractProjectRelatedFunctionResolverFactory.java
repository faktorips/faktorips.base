/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.fl;

import java.util.Locale;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.fl.FunctionResolver;

/**
 * This AbstractProjectRelatedFunctionResolverFactory can be registered with the
 * <em>flfunctionResolverFactory</em> extension point. Subclasses resolves functions according to a
 * related {@link IIpsProject}. Therefore the Interface is enhanced and calling
 * {@link #newFunctionResolver(Locale)} is forbidden. Call the new method
 * {@link #newFunctionResolver(IIpsProject, Locale)} instead.
 * 
 * @author Peter Erzberger
 */
public abstract class AbstractProjectRelatedFunctionResolverFactory<T extends CodeFragment> implements
        IFunctionResolverFactory<T> {

    /**
     * Creates a new FunctionResolver with respect to the provided locale and the related project.
     * It is in the responsibility of the factory provider if the locale is considered.
     */
    public abstract FunctionResolver<T> newFunctionResolver(IIpsProject ipsProject, Locale locale);

    /**
     * This methods throws an {@link UnsupportedOperationException}, because the resolving of
     * functions is projected related. Call {@link #newFunctionResolver(IIpsProject, Locale)}
     * instead.
     * 
     */
    @Override
    public final FunctionResolver<T> newFunctionResolver(Locale locale) {
        throw new UnsupportedOperationException();
    }

}
