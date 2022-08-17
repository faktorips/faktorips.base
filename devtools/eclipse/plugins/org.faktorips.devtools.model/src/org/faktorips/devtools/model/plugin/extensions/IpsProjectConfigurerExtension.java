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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.IIpsProjectConfigurator;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IIpsProjectConfigurator}-supplier for the single implementation of the extension point
 * {@value ExtensionPoints#ADD_IPS_NATURE}.
 */
public class IpsProjectConfigurerExtension
        extends LazyCollectionExtension<IIpsProjectConfigurator, List<IIpsProjectConfigurator>> {

    public IpsProjectConfigurerExtension(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                ExtensionPoints.ADD_IPS_NATURE,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsProjectConfigurator.class,
                ArrayList::new,
                ($, f, l) -> l.add(f));
    }
}
