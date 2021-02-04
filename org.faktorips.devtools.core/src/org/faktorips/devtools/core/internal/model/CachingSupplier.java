package org.faktorips.devtools.core.internal.model;

import java.util.function.Supplier;

/**
 * A {@link Supplier} that only computes its value once and then returns that value in a thread-safe
 * manner.
 */
public abstract class CachingSupplier<T> implements Supplier<T> {

    private volatile T value;

    @Override
    public T get() {
        T result = value;
        if (result == null) {
            synchronized (this) {
                if (value == null) {
                    // CSOFF: InnerAssignmentCheck
                    value = result = initializeValue();
                    // CSON: InnerAssignmentCheck
                }
            }
        }
        return result;
    }

    /**
     * Computes the value. Will be called only once.
     */
    protected abstract T initializeValue();

    /**
     * Creates a new {@link CachingSupplier} wrapping the given {@link Supplier}.
     */
    public static <T> CachingSupplier<T> caching(Supplier<T> originalSupplier) {
        return new CachingSupplier<T>() {

            @Override
            protected T initializeValue() {
                return originalSupplier.get();
            }
        };
    }

}