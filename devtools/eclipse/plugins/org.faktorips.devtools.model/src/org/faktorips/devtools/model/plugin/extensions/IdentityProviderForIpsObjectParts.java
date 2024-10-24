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

import org.faktorips.devtools.model.ipsobject.IdentityProvider;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IdentityProvider} supplier for collecting all implementations of the extension point
 * {@value #EXTENSION_POINT_ID_IDENTITYPROVIDERS}
 */
public class IdentityProviderForIpsObjectParts extends
        LazyListExtension<IdentityProvider> {

    public static final String EXTENSION_POINT_ID_IDENTITYPROVIDERS = "ipsObjectPartIdentityProvider"; //$NON-NLS-1$

    public IdentityProviderForIpsObjectParts(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_IDENTITYPROVIDERS,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IdentityProvider.class);
    }

}