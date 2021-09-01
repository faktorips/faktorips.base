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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.model.IPreSaveProcessor;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

/**
 * {@link IPreSaveProcessor}-by-{@link IpsObjectType}-{@link Map}-supplier for all implementations
 * of the extension point {@value IPreSaveProcessor#EXTENSION_POINT_ID_PRE_SAVE_PROCESSOR}.
 * 
 * @since 21.12
 */
public class PreSaveProcessorExtensions extends
        LazyCollectionExtension<IPreSaveProcessor, Map<IpsObjectType, List<IPreSaveProcessor>>> {

    public PreSaveProcessorExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                IPreSaveProcessor.EXTENSION_POINT_ID_PRE_SAVE_PROCESSOR,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IPreSaveProcessor.class,
                HashMap::new,
                ($configElement, processor, map) -> map
                        .computeIfAbsent(processor.getIpsObjectType(), $ -> new ArrayList<>(1)).add(processor));
    }

}
