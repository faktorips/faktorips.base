/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.devtools.model.abstraction.AFile.AEclipseFile;
import org.faktorips.devtools.model.abstraction.AFile.PlainJavaFile;
import org.faktorips.devtools.model.abstraction.AFolder.AEclipseFolder;
import org.faktorips.devtools.model.abstraction.AFolder.PlainJavaFolder;
import org.faktorips.devtools.model.abstraction.AJavaElement.AEclipseJavaElement;
import org.faktorips.devtools.model.abstraction.AJavaElement.PlainJavaJavaElement;
import org.faktorips.devtools.model.abstraction.AJavaProject.AEclipseJavaProject;
import org.faktorips.devtools.model.abstraction.AJavaProject.PlainJavaJavaProject;
import org.faktorips.devtools.model.abstraction.AMarker.AEclipseMarker;
import org.faktorips.devtools.model.abstraction.AMarker.PlainJavaMarker;
import org.faktorips.devtools.model.abstraction.AMarker.PlainJavaMarkerImpl;
import org.faktorips.devtools.model.abstraction.APackageFragmentRoot.AEclipsePackageFragmentRoot;
import org.faktorips.devtools.model.abstraction.APackageFragmentRoot.PlainJavaPackageFragmentRoot;
import org.faktorips.devtools.model.abstraction.AProject.AEclipseProject;
import org.faktorips.devtools.model.abstraction.AProject.PlainJavaProject;
import org.faktorips.devtools.model.abstraction.AResourceDelta.EclipseResourceDelta;
import org.faktorips.devtools.model.abstraction.AWorkspace.AEclipseWorkspace;
import org.faktorips.devtools.model.abstraction.AWorkspace.PlainJavaWorkspace;
import org.faktorips.devtools.model.abstraction.AWorkspaceRoot.AEclipseWorkspaceRoot;
import org.faktorips.devtools.model.abstraction.AWorkspaceRoot.PlainJavaWorkspaceRoot;
import org.faktorips.devtools.model.abstraction.Wrappers.WrapperBuilder.EclipseWrapperBuilder;
import org.faktorips.devtools.model.abstraction.Wrappers.WrapperBuilder.PlainJavaWrapperBuilder;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

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
    public static WrapperBuilder wrap(Object o) {
        if (Abstractions.isEclipseRunning()) {
            return new EclipseWrapperBuilder(o);
        } else {
            return new PlainJavaWrapperBuilder(o);
        }
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
     * @return the unwrapped T object
     * @throws ClassCastException if called with a wrapper not wrapping the expected type
     */
    @SuppressWarnings("unchecked")
    public static <T> T unwrap(AAbstraction wrapper) {
        return ((AWrapper<T>)wrapper).unwrap();
    }

    /**
     * Returns a {@link CollectionUnwrapper} that allows unwrapping the given collection of wrappers
     * to an array of the wrapped class.
     */
    public static CollectionUnwrapper unwrap(Collection<? extends AAbstraction> wrappers) {
        return new CollectionUnwrapper(wrappers);
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

        private static Map<Object, AAbstraction> wrappers = new ConcurrentHashMap<>();

        private final Object original;

        protected WrapperBuilder(Object original) {
            this.original = original;
        }

        protected abstract <A extends AAbstraction> A wrapInternal(Object original, Class<A> aClass);

        /**
         * Wraps the implementation-specific object in {@link AWrapper a wrapper} implementing the
         * given {@link AAbstraction abstraction}.
         */
        public <A extends AAbstraction> A as(Class<A> abstraction) {
            @SuppressWarnings("unchecked")
            A wrapper = original == null ? null
                    : (A)wrappers.computeIfAbsent(original, o -> wrapInternal(o, abstraction));
            return wrapper;
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

        public static class EclipseWrapperBuilder extends WrapperBuilder {

            protected EclipseWrapperBuilder(Object original) {
                super(original);
            }

            // CSOFF: CyclomaticComplexity
            @SuppressWarnings("unchecked")
            @Override
            protected <A extends AAbstraction> A wrapInternal(Object original, Class<A> aClass) {
                if (AResourceDelta.class.isAssignableFrom(aClass)) {
                    return (A)new EclipseResourceDelta((IResourceDelta)original);
                }
                if (AFolder.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipseFolder((IFolder)original);
                }
                if (AFile.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipseFile((IFile)original);
                }
                if (AProject.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipseProject((IProject)original);
                }
                if (AWorkspaceRoot.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipseWorkspaceRoot((IWorkspaceRoot)original);
                }
                if (AContainer.class.isAssignableFrom(aClass)) {
                    IContainer container = (IContainer)original;
                    if (container instanceof IWorkspaceRoot) {
                        return (A)new AEclipseWorkspaceRoot((IWorkspaceRoot)container);
                    }
                    if (container instanceof IProject) {
                        return (A)new AEclipseProject((IProject)container);
                    }
                    if (container instanceof IFolder) {
                        return (A)new AEclipseFolder((IFolder)container);
                    }
                }
                if (AResource.class.isAssignableFrom(aClass)) {
                    IResource resource = (IResource)original;
                    if (resource instanceof IWorkspaceRoot) {
                        return (A)new AEclipseWorkspaceRoot((IWorkspaceRoot)resource);
                    }
                    if (resource instanceof IProject) {
                        return (A)new AEclipseProject((IProject)resource);
                    }
                    if (resource instanceof IFolder) {
                        return (A)new AEclipseFolder((IFolder)resource);
                    }
                    if (resource instanceof IFile) {
                        return (A)new AEclipseFile((IFile)resource);
                    }
                }
                if (APackageFragmentRoot.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipsePackageFragmentRoot((IPackageFragmentRoot)original);
                }
                if (AJavaElement.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipseJavaElement((IJavaElement)original);
                }
                if (AJavaProject.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipseJavaProject((IJavaProject)original);
                }
                if (AMarker.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipseMarker((IMarker)original);
                }
                if (AWorkspace.class.isAssignableFrom(aClass)) {
                    return (A)new AEclipseWorkspace((IWorkspace)original);
                }
                throw new IllegalArgumentException("Unknown wrapper class: " + aClass); //$NON-NLS-1$
            }
            // CSON: CyclomaticComplexity

        }

        public static class PlainJavaWrapperBuilder extends WrapperBuilder {

            protected PlainJavaWrapperBuilder(Object original) {
                super(original);
            }

            // CSOFF: CyclomaticComplexity
            @SuppressWarnings("unchecked")
            @Override
            protected <A extends AAbstraction> A wrapInternal(Object original, Class<A> aClass) {
                if (AResourceDelta.class.isAssignableFrom(aClass)) {
                    // TODO
                    throw new IllegalArgumentException(
                            "Resource deltas are currently not supported in plain Java mode"); //$NON-NLS-1$
                }
                if (AFolder.class.isAssignableFrom(aClass)) {
                    return (A)new PlainJavaFolder((java.io.File)original);
                }
                if (AFile.class.isAssignableFrom(aClass)) {
                    return (A)new PlainJavaFile((java.io.File)original);
                }
                if (AProject.class.isAssignableFrom(aClass)) {
                    return (A)new PlainJavaProject((java.io.File)original);
                }
                if (AWorkspaceRoot.class.isAssignableFrom(aClass)
                        || AContainer.class.isAssignableFrom(aClass)
                        || AResource.class.isAssignableFrom(aClass)) {
                    java.io.File originalFolder = (File)original;
                    return (A)((PlainJavaWorkspaceRoot)Abstractions.getWorkspace().getRoot())
                            .get(originalFolder.toPath());
                }
                if (APackageFragmentRoot.class.isAssignableFrom(aClass)) {
                    return (A)new PlainJavaPackageFragmentRoot((java.io.File)original);
                }
                if (AJavaElement.class.isAssignableFrom(aClass)) {
                    return (A)new PlainJavaJavaElement((java.io.File)original);
                }
                if (AJavaProject.class.isAssignableFrom(aClass)) {
                    return (A)new PlainJavaJavaProject((PlainJavaProject)original);
                }
                if (AMarker.class.isAssignableFrom(aClass)) {
                    return (A)new PlainJavaMarker((PlainJavaMarkerImpl)original);
                }
                if (AWorkspace.class.isAssignableFrom(aClass)) {
                    return (A)new PlainJavaWorkspace((java.io.File)original);
                }
                throw new IllegalArgumentException("Unknown wrapper class: " + aClass); //$NON-NLS-1$
            }
            // CSON: CyclomaticComplexity

        }
    }

}
