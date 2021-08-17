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
        if (result != null) {
            return result;
        }
        synchronized (this) {
            if (value == null) {
                value = initializeValue();
            }
            return value;
        }
    }

    /**
     * Computes the value. Will be called only once.
     */
    protected abstract T initializeValue();

    /**
     * Creates a new {@link CachingSupplier} wrapping the given {@link Supplier}.
     */
    public static <T> CachingSupplier<T> caching(Supplier<T> originalSupplier) {
        return new CachingSupplier<>() {

            @Override
            protected T initializeValue() {
                return originalSupplier.get();
            }
        };
    }

}