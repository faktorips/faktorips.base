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

import java.util.function.Function;
import java.util.function.Supplier;

import org.faktorips.devtools.model.plugin.ExtensionPoints;

public class SimpleSingleLazyExtension<T> extends SingleLazyExtension<T, T> {

    public SimpleSingleLazyExtension(ExtensionPoints extensionPoints, String extensionPointId,
            String configElementAttribute, Class<T> extensionClass, Supplier<T> defaultSupplier) {
        super(extensionPoints, extensionPointId, configElementAttribute, extensionClass, Function.identity(),
                defaultSupplier);
    }

}
