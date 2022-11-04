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

import org.faktorips.devtools.model.plugin.ExtensionPoints;

public class LazyListExtension<E> extends LazyCollectionExtension<E, List<E>> {

    public LazyListExtension(ExtensionPoints extensionPoints, String extensionPointId,
            String configElementAttribute,
            Class<E> extensionClass) {
        super(extensionPoints,
                extensionPointId,
                configElementAttribute,
                extensionClass,
                ArrayList::new,
                ($, containerType, list) -> list.add(containerType));
    }

}
