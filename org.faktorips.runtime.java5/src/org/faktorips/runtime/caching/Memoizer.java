/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.caching;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * This Memoizer is implemented as suggested by Brian Goetz in Java Concurrency in Practice. It is a
 * thread safe caching mechanism that loads not stored object by calling a {@link IComputable}.
 * 
 * It is extended by the soft reference mechanism so references could be garbage collected in case
 * of memory needs.
 * 
 * @author dirmeier
 */
public class Memoizer<K, V> implements IComputable<K, V> {

    private final ConcurrentMap<K, Future<SoftValue<V>>> cache;

    private final IComputable<K, V> computable;

    private final ReferenceQueue<V> queue = new ReferenceQueue<V>();

    /**
     * The constructor to create a memoizer with default values for the internal
     * {@link ConcurrentHashMap}
     * 
     * @param computable the {@link IComputable} to load new items
     */
    public Memoizer(IComputable<K, V> computable) {
        this.computable = computable;
        cache = new ConcurrentHashMap<K, Future<SoftValue<V>>>();
    }

    /**
     * This constructor needs next to the {@link IComputable} also the initial size, the load factor
     * and the concurrency level. These parameters are only for tuning purpose and are directly
     * forwarded to the internal {@link ConcurrentHashMap}.
     * 
     * @param computable The {@link IComputable} to load new items
     * @param initSize the initial size @see {@link ConcurrentHashMap}
     * @param loadFactor the load factor @see {@link ConcurrentHashMap}
     * @param concurrencyLevel the concurrency level @see {@link ConcurrentHashMap}
     */
    public Memoizer(IComputable<K, V> computable, int initSize, float loadFactor, int concurrencyLevel) {
        this.computable = computable;
        cache = new ConcurrentHashMap<K, Future<SoftValue<V>>>(initSize, loadFactor, concurrencyLevel);
    }

    public V compute(final K key) throws InterruptedException {
        // In case of SoftReference is garbaged or CancellationException we want to try again - in
        // all other cases we exit with return or throwing an exception
        while (true) {
            Future<SoftValue<V>> future = cache.get(key);
            if (future == null) {
                Callable<SoftValue<V>> eval = new Callable<SoftValue<V>>() {

                    public SoftValue<V> call() throws Exception {
                        V computed = computable.compute(key);
                        if (computed == null) {
                            return null;
                        }
                        return new SoftValue<V>(key, computed, queue);
                    }

                };
                FutureTask<SoftValue<V>> futureTask = new FutureTask<SoftValue<V>>(eval);
                processQueue();
                future = cache.putIfAbsent(key, futureTask);
                if (future == null) {
                    future = futureTask;
                    futureTask.run();
                }
            }
            try {
                SoftValue<V> softValue = future.get();
                if (softValue == null) {
                    // computable returned null
                    cache.remove(key);
                    return null;
                } else if (softValue.get() == null) {
                    // softreference was garbaged
                    cache.remove(softValue.key);
                    // try again: while (true)
                } else {
                    return softValue.get();
                }
            } catch (CancellationException e) {
                cache.remove(key, future);
                // try again: while (true)
            } catch (ExecutionException e) {
                throw launderThrowable(e.getCause());
            }
        }
    }

    /**
     * Go through the ReferenceQueue and remove garbage collected SoftValue objects.
     */
    private void processQueue() {
        Reference<? extends V> ref;
        while ((ref = queue.poll()) != null) {
            SoftValue<? extends V> sv = (SoftValue<? extends V>)ref;
            cache.remove(sv.key);
        }
    }

    /**
     * Coerce an unchecked Throwable to a RuntimeException
     * <p/>
     * If the Throwable is an Error, throw it; if it is a RuntimeException return it, otherwise
     * throw IllegalStateException
     */
    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException)t;
        } else if (t instanceof Error) {
            throw (Error)t;
        } else {
            throw new IllegalStateException("Not unchecked", t);
        }
    }

    public Class<? super V> getValueClass() {
        return computable.getValueClass();
    }

    /**
     * Inner subclass of SoftReference which contains additional the key to make it easier to find
     * the entry in the map, after it has been garbage collected
     */
    private static class SoftValue<V> extends SoftReference<V> {

        private final Object key;

        private SoftValue(Object key, V referent, ReferenceQueue<V> q) {
            super(referent, q);
            this.key = key;
        }
    }

}
