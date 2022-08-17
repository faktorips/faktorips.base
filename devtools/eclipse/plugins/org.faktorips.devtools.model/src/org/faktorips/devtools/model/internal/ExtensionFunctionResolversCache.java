/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.internal.fl.AbstractProjectRelatedFunctionResolverFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.SortorderSet;
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
        resolverFactories = IIpsModelExtensions.get().getFlFunctionResolverFactories();
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
        getIpsProject().getIpsModel().addChangeListener($ -> clearCache());
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
        ArrayList<FunctionResolver<JavaCodeFragment>> resolvers = new ArrayList<>();
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

    private FunctionResolver<JavaCodeFragment> createFuntionResolver(
            IFunctionResolverFactory<JavaCodeFragment> factory) {
        try {
            return createFunctionResolver(factory);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            IpsLog.log(new IpsStatus("Unable to create the function resolver for the following factory: " //$NON-NLS-1$
                    + factory.getClass(), e));
        }
        return null;
    }

    private boolean isActive(IFunctionResolverFactory<JavaCodeFragment> factory) {
        return getIpsProject().getReadOnlyProperties().isActive(factory);
    }

    private FunctionResolver<JavaCodeFragment> createFunctionResolver(
            IFunctionResolverFactory<JavaCodeFragment> factory) {
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
