/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
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
 * This is Memoizer is implemented as suggested by Brian Goetz in Java Concurrency in Practice. It
 * is a thread safe caching mechanism that loads not stored object by calling a {@link Computable}.
 * 
 * It is extended by the soft reference mechanism so references could be garbage collected in case
 * of memory needs.
 * 
 * @author dirmeier
 */
public class Memoizer<K, V> implements Computable<K, V> {

    private final ConcurrentMap<K, Future<SoftReference<V>>> cache = new ConcurrentHashMap<K, Future<SoftReference<V>>>();

    private final Computable<K, V> computable;

    private final ReferenceQueue<V> queue = new ReferenceQueue<V>();

    public Memoizer(Computable<K, V> computable) {
        this.computable = computable;
    }

    public V compute(final K key) throws InterruptedException {
        // In case of CancellationException we want to try again - in all other cases we exit with
        // return or throwing an exception
        while (true) {
            Future<SoftReference<V>> future = cache.get(key);
            if (future == null) {
                Callable<SoftReference<V>> eval = new Callable<SoftReference<V>>() {

                    public SoftReference<V> call() throws Exception {
                        return new SoftValue<V>(key, computable.compute(key), queue);
                    }
                };
                FutureTask<SoftReference<V>> futureTask = new FutureTask<SoftReference<V>>(eval);
                processQueue();
                future = cache.putIfAbsent(key, futureTask);
                if (future == null) {
                    future = futureTask;
                    futureTask.run();
                }
            }
            try {
                return future.get().get();
            } catch (CancellationException e) {
                cache.remove(key, future);
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
