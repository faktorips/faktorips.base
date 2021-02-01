package org.faktorips.devtools.model.plugin.extensions;

import java.util.function.Function;
import java.util.function.Supplier;

import org.faktorips.devtools.model.exception.CoreRuntimeException;
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
                    throw new CoreRuntimeException("There are multiple extensions to the extension point " //$NON-NLS-1$
                            + getExtensionPointId() + " but only one is allowed."); //$NON-NLS-1$
                }).orElseGet(defaultSupplier);
    }

}