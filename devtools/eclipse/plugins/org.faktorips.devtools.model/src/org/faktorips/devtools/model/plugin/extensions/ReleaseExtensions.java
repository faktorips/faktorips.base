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

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.productrelease.ReleaseExtension;

/**
 * {@link ReleaseExtension}-{@link List}-supplier for all implementations of the extension point
 * {@value #EXTENSION_POINT_ID_PRODUCT_RELEASE_EXTENSION}.
 */
public class ReleaseExtensions extends LazyListExtension<ReleaseExtension> {

    public static final String EXTENSION_POINT_ID_PRODUCT_RELEASE_EXTENSION = "productReleaseExtension"; //$NON-NLS-1$

    public ReleaseExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_PRODUCT_RELEASE_EXTENSION,
                ReleaseExtension.CONFIG_ELEMENT_OPERATION,
                ReleaseExtension.class);
    }

    @Override
    protected ReleaseExtension create(IConfigurationElement configElement) {
        return new ReleaseExtension(configElement);
    }

}
