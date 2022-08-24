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

import org.faktorips.devtools.model.fl.IFlIdentifierFilterExtension;
import org.faktorips.devtools.model.fl.IdentifierFilter;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IdentifierFilter}-supplier collecting all implementations of the extension point
 * {@value #FL_IDENTIFIER_FILTER_EXTENSION}.
 */
public class IdentifierFilterExtensions extends
        LazyIntermediateCollectionExtension<IFlIdentifierFilterExtension, List<IFlIdentifierFilterExtension>, IdentifierFilter> {

    /**
     * IpsModelPlugin relative id of the extension point {@value #FL_IDENTIFIER_FILTER_EXTENSION}.
     * 
     * @see IFlIdentifierFilterExtension
     */
    public static final String FL_IDENTIFIER_FILTER_EXTENSION = "flIdentifierFilterExtension"; //$NON-NLS-1$

    public IdentifierFilterExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                FL_IDENTIFIER_FILTER_EXTENSION,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IFlIdentifierFilterExtension.class,
                ArrayList::new,
                ($, f, l) -> l.add(f),
                IdentifierFilter::new);
    }

}
