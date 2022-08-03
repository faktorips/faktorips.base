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

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

public class SingleLazyExtension<P, T> extends AbstractLazyExtension<P, T, T> {

    private final Supplier<T> defaultSupplier;

    public SingleLazyExtension(ExtensionPoints extensionPoints, String extensionPointId,
            String configElementAttribute, Class<P> extensionClass,
            Function<P, T> extensionCreator, Supplier<T> defaultSupplier) {
        super(extensionPoints, extensionPointId, configElementAttribute, extensionClass, extensionCreator);
        this.defaultSupplier = defaultSupplier;
    }

    @Override
    protected T initializeExtension() {
        return getConfigElements()
                .map(this::create)
                .reduce(($1, $2) -> {
                    throw new IpsException("There are multiple extensions to the extension point " //$NON-NLS-1$
                            + getExtensionPointId() + " but only one is allowed."); //$NON-NLS-1$
                }).orElseGet(defaultSupplier);
    }

}
