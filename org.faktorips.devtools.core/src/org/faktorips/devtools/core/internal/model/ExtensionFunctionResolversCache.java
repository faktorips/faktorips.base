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

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IFunctionResolverFactory;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.fl.AbstractProjectRelatedFunctionResolverFactory;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.SortorderSet;
import org.faktorips.fl.FunctionResolver;

/**
 * Cache for function resolvers that are added to expression compilers (formula language) for a
 * specific IPS project by default. Loads Function resolvers via the extension point
 * <code>org.faktorips.fl.FunctionResolver</code>. Reuses them as long as the IPS model does not
 * change. This behavior is sufficient for now but may be refined in the future.
 * 
 * This class is not thread safe.
 * 
 */
public class ExtensionFunctionResolversCache {

    private final IIpsProject ipsProject;

    private List<FunctionResolver<JavaCodeFragment>> cachedFunctionResolvers;

    private SortorderSet<IFunctionResolverFactory<JavaCodeFragment>> resolverFactories;

    /**
     * @param ipsProject the project this class caches function resolvers for.
     */
    public ExtensionFunctionResolversCache(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
        resolverFactories = IpsPlugin.getDefault().getFlFunctionResolverFactories();
        registerListenerWithIpsModel();
    }

    /**
     * As of now only used for tests.
     * 
     * @param ipsProject the project this class caches function resolvers for.
     * @param resolverFactories factories creating resolvers. Used instead of the factories provided
     *            by the extension point <code>org.faktorips.fl.FunctionResolver</code>.
     */
    public ExtensionFunctionResolversCache(IIpsProject ipsProject,
            SortorderSet<IFunctionResolverFactory<JavaCodeFragment>> resolverFactories) {
        this.ipsProject = ipsProject;
        this.resolverFactories = resolverFactories;
        registerListenerWithIpsModel();
    }

    private void registerListenerWithIpsModel() {
        getIpsProject().getIpsModel().addChangeListener(new ContentsChangeListener() {
            @Override
            public void contentsChanged(ContentChangeEvent event) {
                clearCache();
            }
        });
    }

    /**
     * Creates a new expression compiler or returns the cached one if it was created before.
     */
    public void addExtensionFunctionResolversToCompiler(ExtendedExprCompiler compiler) {
        List<FunctionResolver<JavaCodeFragment>> resolvers = createFunctionResolversIfNeccessary();
        addFunctionResolversTo(resolvers, compiler);
    }

    protected List<FunctionResolver<JavaCodeFragment>> createFunctionResolversIfNeccessary() {
        if (cachedFunctionResolvers == null) {
            cachedFunctionResolvers = createExtendingFunctionResolvers();
        }
        return cachedFunctionResolvers;
    }

    protected List<FunctionResolver<JavaCodeFragment>> createExtendingFunctionResolvers() {
        ArrayList<FunctionResolver<JavaCodeFragment>> resolvers = new ArrayList<FunctionResolver<JavaCodeFragment>>();
        for (IFunctionResolverFactory<JavaCodeFragment> factory : resolverFactories.getSortedValues()) {
            if (isActive(factory)) {
                FunctionResolver<JavaCodeFragment> resolver = createFuntionResolver(factory);
                addIfNotNull(resolvers, resolver);
            }
        }
        return resolvers;
    }

    private void addIfNotNull(ArrayList<FunctionResolver<JavaCodeFragment>> resolverList,
            FunctionResolver<JavaCodeFragment> resolver) {
        if (resolver != null) {
            resolverList.add(resolver);
        }
    }

    private FunctionResolver<JavaCodeFragment> createFuntionResolver(IFunctionResolverFactory<JavaCodeFragment> factory) {
        try {
            FunctionResolver<JavaCodeFragment> resolver = createFunctionResolver(factory);
            return resolver;
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            IpsPlugin.log(new IpsStatus("Unable to create the function resolver for the following factory: " //$NON-NLS-1$
                    + factory.getClass(), e));
        }
        return null;
    }

    private boolean isActive(IFunctionResolverFactory<JavaCodeFragment> factory) {
        return getIpsProject().getReadOnlyProperties().isActive(factory);
    }

    private FunctionResolver<JavaCodeFragment> createFunctionResolver(IFunctionResolverFactory<JavaCodeFragment> factory) {
        Locale formulaLanguageLocale = getIpsProject().getFormulaLanguageLocale();
        if (factory instanceof AbstractProjectRelatedFunctionResolverFactory) {
            return ((AbstractProjectRelatedFunctionResolverFactory<JavaCodeFragment>)factory).newFunctionResolver(
                    getIpsProject(), formulaLanguageLocale);
        } else {
            return factory.newFunctionResolver(formulaLanguageLocale);
        }
    }

    private void addFunctionResolversTo(List<FunctionResolver<JavaCodeFragment>> resolvers,
            ExtendedExprCompiler compiler) {
        for (FunctionResolver<JavaCodeFragment> resolver : resolvers) {
            compiler.add(resolver);
        }
    }

    private IIpsProject getIpsProject() {
        return ipsProject;
    }

    protected void clearCache() {
        cachedFunctionResolvers = null;
    }
}
