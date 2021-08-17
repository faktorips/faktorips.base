/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.IFunctionResolverFactory;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.util.SortorderSet;

/**
 * {@link SortorderSet}&lt;{@link IFunctionResolverFactory}&lt;{@link JavaCodeFragment}&gt;&gt;-supplier
 * for all implementations of the extension point
 * {@value #EXTENSION_POINT_ID_FL_FUNCTION_RESOLVER_FACTORY}.
 */
public class FunctionResolverFactoryExtensions extends
        LazyCollectionExtension<IFunctionResolverFactory<JavaCodeFragment>, SortorderSet<IFunctionResolverFactory<JavaCodeFragment>>> {

    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_FL_FUNCTION_RESOLVER_FACTORY}.
     */
    public static final String EXTENSION_POINT_ID_FL_FUNCTION_RESOLVER_FACTORY = "flFunctionResolverFactory"; //$NON-NLS-1$
    /**
     * The name of the {@link IConfigurationElement configuration element} property
     * {@value #CONFIG_ELEMENT_PROPERTY_SORTORDER} for
     * {@link #EXTENSION_POINT_ID_FL_FUNCTION_RESOLVER_FACTORY}.
     */
    public static final String CONFIG_ELEMENT_PROPERTY_SORTORDER = "sortorder"; //$NON-NLS-1$

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final Class<IFunctionResolverFactory<JavaCodeFragment>> JAVA_FUNCTION_RESOLVER_FACTORY_CLASS = (Class)IFunctionResolverFactory.class;

    public FunctionResolverFactoryExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_FL_FUNCTION_RESOLVER_FACTORY,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                JAVA_FUNCTION_RESOLVER_FACTORY_CLASS,
                SortorderSet<IFunctionResolverFactory<JavaCodeFragment>>::new,
                FunctionResolverFactoryExtensions::initializeFunctionResolverFactory);
    }

    private static void initializeFunctionResolverFactory(IConfigurationElement configElement,
            IFunctionResolverFactory<JavaCodeFragment> resolverFactory,
            SortorderSet<IFunctionResolverFactory<JavaCodeFragment>> sortOrderSet) {
        Integer sortOrder;
        try {
            sortOrder = Integer.valueOf(configElement.getAttribute(CONFIG_ELEMENT_PROPERTY_SORTORDER));
        } catch (NumberFormatException ex) {
            sortOrder = null;
        }
        sortOrderSet.add(resolverFactory, sortOrder);
    }

}
