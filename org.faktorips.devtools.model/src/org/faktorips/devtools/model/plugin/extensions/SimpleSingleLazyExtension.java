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