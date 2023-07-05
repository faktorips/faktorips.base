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

import org.faktorips.devtools.model.internal.productcmpt.IImplementationClassProvider;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * {@link IImplementationClassProvider}-supplier collecting all implementations of the extension
 * point {@value #EXTENSION_POINT_ID_IMPLEMENTATION_CLASS_PROVIDER}.
 *
 * @since 23.6
 */
public class ImplementationClassProviderExtension extends SimpleSingleLazyExtension<IImplementationClassProvider> {

    public static final String EXTENSION_POINT_ID_IMPLEMENTATION_CLASS_PROVIDER = "implementationClassProvider"; //$NON-NLS-1$

    public ImplementationClassProviderExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints, EXTENSION_POINT_ID_IMPLEMENTATION_CLASS_PROVIDER,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IImplementationClassProvider.class,
                () -> IProductCmpt::getProductCmptType);
    }
}
