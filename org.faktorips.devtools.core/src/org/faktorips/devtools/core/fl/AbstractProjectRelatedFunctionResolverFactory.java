/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.fl;

import java.util.Locale;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.devtools.core.IFunctionResolverFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.FunctionResolver;

/**
 * This AbstractProjectRelatedFunctionResolverFactory can be registered with the
 * <i>flfunctionResolverFactory</i> extension point. Subclasses resolves functions according to a
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
