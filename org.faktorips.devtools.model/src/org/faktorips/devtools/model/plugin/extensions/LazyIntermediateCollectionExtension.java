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

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.util.TriConsumer;

public class LazyIntermediateCollectionExtension<P, C, T> extends AbstractLazyExtension<P, P, T> {

    private final Supplier<C> collectionCreator;
    private final TriConsumer<IConfigurationElement, P, C> elementCollector;
    private final Function<C, T> extensionFinalizer;

    public LazyIntermediateCollectionExtension(ExtensionPoints extensionPoints, String extensionPointId,
            String configElementAttribute,
            Class<P> extensionClass, Supplier<C> collectionCreator,
            TriConsumer<IConfigurationElement, P, C> elementCollector,
            Function<C, T> extensionFinalizer) {
        super(extensionPoints, extensionPointId, configElementAttribute, extensionClass, Function.identity());
        this.collectionCreator = collectionCreator;
        this.elementCollector = elementCollector;
        this.extensionFinalizer = extensionFinalizer;
    }

    @Override
    protected T initializeExtension() {
        // TODO Superklasse
        C collection = collectionCreator.get();
        getConfigElements().forEach(configElement -> {
            P element = create(configElement);
            elementCollector.accept(configElement, element, collection);
        });
        return extensionFinalizer.apply(collection);
    }

}