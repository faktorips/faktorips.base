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

import java.util.List;

import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.model.productcmpt.IProductCmptNamingStrategyFactory;

/**
 * {@link IProductCmptNamingStrategyFactory}-{@link List}-supplier for all implementations of the
 * extension point {@value #EXTENSION_POINT_ID_PRODUCT_COMPONENT_NAMING_STRATEGY}.
 */
public class ProductCmptNamingStrategyFactoryExtensions extends LazyListExtension<IProductCmptNamingStrategyFactory> {

    /**
     * IpsModelPlugin relative id of the extension point for product component naming strategies.
     * 
     * @see IProductCmptNamingStrategy
     * @see IProductCmptNamingStrategyFactory
     */
    public static final String EXTENSION_POINT_ID_PRODUCT_COMPONENT_NAMING_STRATEGY = "productComponentNamingStrategy"; //$NON-NLS-1$

    /**
     * Name of the attribute that holds the name of the factory class
     */
    public static final String ATTRIBUTE_FACTORY_CLASS = "factoryClass"; //$NON-NLS-1$

    public ProductCmptNamingStrategyFactoryExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_PRODUCT_COMPONENT_NAMING_STRATEGY,
                ATTRIBUTE_FACTORY_CLASS,
                IProductCmptNamingStrategyFactory.class);
    }

}
