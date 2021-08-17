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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.util.Consumers;
import org.faktorips.devtools.model.util.TriConsumer;

public class LazyCollectionExtension<P, C> extends AbstractLazyExtension<P, P, C> {

    private final Supplier<C> collectionCreator;
    private final TriConsumer<IConfigurationElement, P, C> elementCollector;
    private final Consumer<C> collectionFinalizer;

    public LazyCollectionExtension(ExtensionPoints extensionPoints, String extensionPointId,
            String configElementAttribute,
            Class<P> extensionClass, Supplier<C> collectionCreator,
            TriConsumer<IConfigurationElement, P, C> elementCollector,
            Consumer<C> collectionFinalizer) {
        super(extensionPoints, extensionPointId, configElementAttribute, extensionClass, Function.identity());
        this.collectionCreator = collectionCreator;
        this.elementCollector = elementCollector;
        this.collectionFinalizer = collectionFinalizer;
    }

    public LazyCollectionExtension(ExtensionPoints extensionPoints, String extensionPointId,
            String configElementAttribute,
            Class<P> extensionClass, Supplier<C> collectionCreator,
            TriConsumer<IConfigurationElement, P, C> elementCollector) {
        this(extensionPoints, extensionPointId, configElementAttribute, extensionClass, collectionCreator,
                elementCollector, Consumers.ignore());
    }

    @Override
    protected C initializeExtension() {
        C collection = collectionCreator.get();
        getConfigElements().forEach(configElement -> {
            P element = ExtensionPoints.createExecutableExtension(getExtensionPointId(), configElement,
                    getConfigElementAttribute(),
                    getExtensionClass());
            if (element != null) {
                elementCollector.accept(configElement, element, collection);
            }
        });
        collectionFinalizer.accept(collection);
        return collection;
    }

}