/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Utility class to wrap, unwrap and implement {@link AWrapper wrappers}.
 */
public class Wrappers {

    private Wrappers() {
        // utility
    }

    /**
     * Returns a {@link WrapperBuilder} for the given object that allows it to be wrapped in an
     * implementation-specific {@link AWrapper wrapper}.
     */
    public static WrapperBuilder wrap(Object original) {
        return Abstractions.getWrapperBuilder(original);
    }

    /**
     * Returns a {@link WrapperBuilder} for the object produced by the given supplier that allows
     * that object to be wrapped in an implementation-specific {@link AWrapper wrapper}.
     * <p>
     * Any {@link CoreException} thrown by the supplier is rethrown as a
     * {@link CoreRuntimeException}.
     */
    public static <T> WrapperBuilder wrap(CoreExceptionThrowingSupplier<T> supplier) {
        return get(() -> wrap(supplier.get()));
    }

    /**
     * Runs the given runnable.
     * <p>
     * Any {@link CoreException} thrown by the runnable is rethrown as a
     * {@link CoreRuntimeException}.
     */
    public static void run(CoreExceptionThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Calls the given supplier and returns the result.
     * <p>
     * Any {@link CoreException} thrown by the supplier is rethrown as a
     * {@link CoreRuntimeException}.
     */
    public static <T> T get(CoreExceptionThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Unwraps the given {@link AAbstraction abstraction} to the call-site's type
     *
     * @param <T> the expected unwrapped type
     * @param wrapper a wrapper wrapping the expected type
     * @return the unwrapped T object or {@code null} if the given wrapper is {@code null}
     * @throws ClassCastException if called with a wrapper not wrapping the expected type
     */
    @SuppressWarnings("unchecked")
    @CheckForNull
    public static <T> T unwrap(@CheckForNull AAbstraction wrapper) {
        return wrapper == null ? null : ((AWrapper<T>)wrapper).unwrap();
    }

    /**
     * Returns a {@link ArrayUnwrapper} that allows unwrapping the given array of wrappers to an
     * array or list of the wrapped class.
     */
    public static <A extends AAbstraction> ArrayUnwrapper<A> unwrap(A[] wrappers) {
        return new ArrayUnwrapper<>(wrappers);
    }

    /**
     * Returns a {@link CollectionUnwrapper} that allows unwrapping the given collection of wrappers
     * to an array of the wrapped class.
     */
    public static CollectionUnwrapper unwrap(Collection<? extends AAbstraction> wrappers) {
        return new CollectionUnwrapper(wrappers);
    }

    public static class ArrayUnwrapper<A extends AAbstraction> {
        private final A[] wrappers;

        protected ArrayUnwrapper(A[] wrappers) {
            this.wrappers = wrappers;
        }

        @SuppressWarnings({ "unchecked" })
        public <T> T[] asArrayOf(Class<T> clazz) {
            return wrappers == null
                    ? null
                    : Arrays.stream(wrappers)
                            .map(p -> (T)p.unwrap())
                            .toArray(l -> (T[])Array.newInstance(clazz, l));
        }

        @SuppressWarnings({ "unchecked" })
        public <T> List<T> asList() {
            return wrappers == null
                    ? null
                    : Arrays.stream(wrappers)
                            .map(p -> (T)p.unwrap())
                            .collect(Collectors.toList());
        }
    }

    public static class CollectionUnwrapper {

        private final Collection<? extends AAbstraction> wrappers;

        protected CollectionUnwrapper(Collection<? extends AAbstraction> wrappers) {
            this.wrappers = wrappers;
        }

        @SuppressWarnings({ "unchecked" })
        public <T> T[] asArrayOf(Class<T> clazz) {
            return wrappers == null
                    ? null
                    : wrappers.stream()
                            .map(p -> (T)p.unwrap())
                            .toArray(l -> (T[])Array.newInstance(clazz, l));
        }
    }

    /**
     * A {@link Supplier} that may throw a {@link CoreException}.
     */
    @FunctionalInterface
    public static interface CoreExceptionThrowingSupplier<T> {

        T get() throws CoreException;
    }

    /**
     * A {@link Runnable} that may throw a {@link CoreException}.
     */
    @FunctionalInterface
    public static interface CoreExceptionThrowingRunnable {

        void run() throws CoreException;
    }

    public abstract static class WrapperBuilder {

        private static Map<Object, Set<AAbstraction>> wrappers = new ConcurrentHashMap<>();

        private final Object original;

        protected WrapperBuilder(Object original) {
            this.original = original;
        }

        protected abstract <A extends AAbstraction> A wrapInternal(Object original, Class<A> aClass);

        /**
         * Wraps the implementation-specific object in {@link AWrapper a wrapper} implementing the
         * given {@link AAbstraction abstraction}.
         */
        public <A extends AAbstraction> A as(Class<A> abstractionClass) {
            if (original == null) {
                return null;
            }
            Set<AAbstraction> abstractions = wrappers.computeIfAbsent(original, $ -> new LinkedHashSet<>());
            synchronized (abstractions) {
                for (AAbstraction abstraction : abstractions) {
                    if (abstractionClass.isInstance(abstraction)) {
                        @SuppressWarnings("unchecked")
                        A wrapper = (A)abstraction;
                        return wrapper;
                    }
                }
                A wrapper = wrapInternal(original, abstractionClass);
                abstractions.add(wrapper);
                return wrapper;
            }
        }

        /**
         * Wraps the implementation-specific object-array in {@link AWrapper a wrapper-array}
         * implementing the given {@link AAbstraction abstraction}.
         */
        public <A extends AAbstraction> A[] asArrayOf(Class<A> abstraction) {
            @SuppressWarnings("unchecked")
            A[] wrapperArray = Arrays.stream((Object[])original).map(o -> Wrappers.wrap(o).as(abstraction))
                    .toArray(l -> (A[])Array.newInstance(abstraction, l));
            return wrapperArray;
        }

        /**
         * Wraps the implementation-specific object-array in a {@link Set} of {@link AWrapper
         * wrappers} implementing the given {@link AAbstraction abstraction}.
         */
        public <A extends AAbstraction> Set<A> asSetOf(Class<A> aClass) {
            return Arrays.stream((Object[])original).map(o -> Wrappers.wrap(o).as(aClass))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        /**
         * Wraps the implementation-specific object-array in a {@link SortedSet} of {@link AWrapper
         * wrappers} implementing the given {@link AAbstraction abstraction}.
         */
        public <A extends AAbstraction & Comparable<A>> SortedSet<A> asSortedSetOf(Class<A> aClass) {
            return Arrays.stream((Object[])original).map(o -> Wrappers.wrap(o).as(aClass))
                    .collect(Collectors.toCollection(TreeSet::new));
        }

    }

}
