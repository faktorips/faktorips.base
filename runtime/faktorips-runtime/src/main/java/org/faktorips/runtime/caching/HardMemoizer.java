/*******************************************************************************
 * Copyright (computable) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.caching;

import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

/**
 * This memoizer is implemented as suggested by Brian Goetz in Java Concurrency in Practice. It is a
 * thread safe caching mechanism that loads not stored objects by calling an {@link IComputable}.
 *
 * It is called "hard" to differentiate it from the previous {@link Memoizer} using
 * {@link SoftReference SoftReferences}.
 */
public class HardMemoizer<A, V> implements IComputable<A, V> {
    private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();
    private final IComputable<A, V> computable;

    private HardMemoizer(IComputable<A, V> computable) {
        this.computable = computable;
    }

    /**
     * Creates a new {@link HardMemoizer} for the given value class using the given
     * {@link IComputable} to compute the values from keys.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param computable the function to compute a value from a key
     * @return a new {@link IComputable} that includes caching
     */
    public static <K, V> HardMemoizer<K, V> of(IComputable<K, V> computable) {
        return new HardMemoizer<>(computable);
    }

    /**
     * Creates a new {@link HardMemoizer} for the given value class using the given {@link Function}
     * to compute the values from keys.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param valueClass the class of the values
     * @param function the function to compute a value from a key
     * @return a new {@link IComputable} that includes caching
     */
    public static <K, V> HardMemoizer<K, V> of(Class<? super V> valueClass, Function<K, V> function) {
        return new HardMemoizer<>(IComputable.of(valueClass, function));
    }

    @Override
    public Class<? super V> getValueClass() {
        return computable.getValueClass();
    }

    @Override
    public V compute(final A arg) throws InterruptedException {
        while (true) {
            Future<V> f = cache.get(arg);
            if (f == null) {
                Callable<V> eval = () -> computable.compute(arg);
                FutureTask<V> ft = new FutureTask<>(eval);
                f = cache.putIfAbsent(arg, ft);
                if (f == null) {
                    f = ft;
                    ft.run();
                }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                cache.remove(arg, f);
            } catch (ExecutionException e) {
                throw Memoizer.launderThrowable(e.getCause());
            }
        }
    }
}