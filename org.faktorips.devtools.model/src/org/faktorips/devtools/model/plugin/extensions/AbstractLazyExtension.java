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

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

public abstract class AbstractLazyExtension<P, T, E> extends CachingSupplier<E> {
    private final ExtensionPoints extensionPoints;

    private final String extensionPointId;
    private final String configElementAttribute;
    private final Class<P> extensionClass;
    private final Function<P, T> extensionCreator;

    public AbstractLazyExtension(
            ExtensionPoints extensionPoints,
            String extensionPointId,
            String configElementAttribute,
            Class<P> extensionClass,
            Function<P, T> extensionCreator) {
        this.extensionPoints = extensionPoints;
        this.extensionPointId = extensionPointId;
        this.configElementAttribute = configElementAttribute;
        this.extensionClass = extensionClass;
        this.extensionCreator = extensionCreator;
    }

    protected abstract E initializeExtension();

    @Override
    protected E initializeValue() {
        return initializeExtension();
    }

    protected Stream<IConfigurationElement> getConfigElements() {
        return Arrays.stream(extensionPoints.getRegistry().getConfigurationElementsFor(extensionPoints.getNameSpace(),
                extensionPointId));
    }

    protected T create(IConfigurationElement configElement) {
        P executable = ExtensionPoints.createExecutableExtension(getExtensionPointId(), configElement,
                getConfigElementAttribute(),
                getExtensionClass());
        return extensionCreator.apply(executable);
    }

    public String getExtensionPointId() {
        return extensionPointId;
    }

    public String getConfigElementAttribute() {
        return configElementAttribute;
    }

    public Class<P> getExtensionClass() {
        return extensionClass;
    }

}
