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

import static org.faktorips.devtools.model.abstraction.Wrappers.run;
import static org.faktorips.devtools.model.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.model.abstraction.mapping.PathMapping.toEclipsePath;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.abstraction.AMarker.PlainJavaMarker;
import org.faktorips.devtools.model.abstraction.AMarker.PlainJavaMarkerImpl;
import org.faktorips.devtools.model.abstraction.AWorkspace.PlainJavaWorkspace;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A resource is a file or folder, possibly with a special role like a project or workspace.
 */
public interface AResource extends AAbstraction, IAdaptable, Comparable<AResource> {

    /**
     * A {@link Comparator} comparing resources by name.
     */
    static final Comparator<AResource> COMPARING_BY_NAME = Comparator.comparing(AResource::getName);

    /**
     * The local name of this resource inside its {@link #getParent() parent}.
     *
     * @return this resource's name
     */
    String getName();

    /**
     * A resource type can be used instead of checking for {@code instanceof} with concrete
     * implementations.
     *
     * @return the resource's {@link AResourceType type}
     */
    AResourceType getType();

    /**
     * A resource exists if its file-system equivalent exists (the path corresponds to an actual
     * file/folder).
     *
     * @return whether this resource exists
     */
    boolean exists();

    /**
     * An accessible resource {@link #exists()} and can be used. For example a project in Eclipse
     * needs to be open to be accessible.
     *
     * @return whether this resource is accessible
     */
    boolean isAccessible();

    /**
     * Returns whether this resource is derived (can be recreated from source files).
     */
    boolean isDerived();

    /**
     * Sets this resource (and, in case this is {@link AContainer a container}, its children) as
     * derived.
     *
     * @param isDerived whether to mark this resource as derived
     * @param monitor a progress monitor that is notified about this process. When marking a
     *            directory as (not) derived, individual file processing is reported to the monitor
     *            to allow fine-grained progress reporting. The monitor may be {@code null} when
     *            progress does not need to be reported.
     */
    void setDerived(boolean isDerived, @CheckForNull IProgressMonitor monitor);

    /**
     * A resource's parent ist the resource representing the containing folder. Not all resources
     * have a parent, for example a workspace only contains other resources but has not parent.
     * 
     * @return this resource's parent, or {@code null} if it has no parent
     */
    @CheckForNull
    AContainer getParent();

    /**
     * Returns the project containing this resource.
     *
     * @return the project containing this resource; the project itself if this resource is a
     *         {@link AResourceType#PROJECT project} and {@code null} if this resource is the
     *         {@link AResourceType#WORKSPACE workspace}.
     */
    @CheckForNull
    AProject getProject();

    /**
     * Returns the workspace containing this resource.
     *
     * @return the workspace containing this resource; the workspace itself if this resource is a
     *         {@link AResourceType#WORKSPACE workspace}.
     */
    AWorkspace getWorkspace();

    /**
     * Returns the absolute location of this resource in the file system.
     *
     * @return this resource's location
     */
    Path getLocation();

    /**
     * Returns the relative location of this resource in the containing project.
     *
     * @return this resource's location relative to its project or an empty path if the resource is
     *         not contained in a project
     */
    Path getProjectRelativePath();

    /**
     * Returns the relative location of this resource in the containing workspace.
     *
     * @return this resource's location relative to its project or an empty path if the resource is
     *         not contained in a workspace
     */
    Path getWorkspaceRelativePath();

    /**
     * Returns the corresponding file's modification stamp. This may correspond to the
     * {@link #getLocalTimeStamp()} from the file system or be more fine-grained.
     *
     * @return the file's modification time stamp, or {@code -1} if the file does not exist
     */
    long getModificationStamp();

    /**
     * Returns the corresponding file's last modification as milliseconds since the epoch
     * (1970-1-1).
     *
     * @return the file's time stamp, or {@code -1} if the file does not exist
     */
    long getLocalTimeStamp();

    /**
     * Synchronizes this resource with its corresponding file-system objects.
     *
     * @param depth whether to synchronize only this resource, or also its children (and their
     *            children...)
     * @param monitor a progress monitor that is notified about this process. When synchronizing a
     *            directory, individual file synchronization is reported to the monitor to allow
     *            fine-grained progress reporting. The monitor may be {@code null} when progress
     *            does not need to be reported.
     */
    void refreshLocal(AResourceTreeTraversalDepth depth, @CheckForNull IProgressMonitor monitor);

    /**
     * Checks whether this resource is in sync with its corresponding file-system resource. Both
     * need to
     * <ul>
     * <li>{@link #exists() exist} not)</li>
     * <li>have the same {@link #getLocalTimeStamp() time stamp}</li>
     * <li>be of the same {@link #getType() type}</li>
     * </ul>
     *
     * @param depth whether to check only this resource, or also its children (and their
     *            children...)
     * @return whether this resource (and its children) is/are in sync with its/their corresponding
     *         file-system resource(s)
     */
    boolean isSynchronized(AResourceTreeTraversalDepth depth);

    /**
     * Marks this resource as modified, updating the {@link #getLocalTimeStamp() local time stamp}
     * and {@link #getModificationStamp() modification stamp}.
     *
     * @param monitor a progress monitor that is notified about this process. The monitor may be
     *            {@code null} when progress does not need to be reported.
     */
    void touch(IProgressMonitor monitor);

    /**
     * Returns all markers of the given type on this resource. If {@code includeSubtypes} is
     * {@code true}, then markers of types that are subtypes of the given type are also returned.
     * The {@code depth} determines whether only markers from this resource are returned or also
     * from its children (if this resource is {@link AContainer a container}).
     *
     * @param type a {@link AMarker#getType() marker type}
     * @param includeSubtypes whether to include subtypes of the given marker type
     * @param depth whether to scan only this resource, or also its children (and their children...)
     * @return all markers matching the given type and search depth
     */
    Set<AMarker> findMarkers(String type, boolean includeSubtypes, AResourceTreeTraversalDepth depth);

    /**
     * Creates a new marker of the given type for this resource.
     *
     * @param markerType the {@link AMarker#getType() marker type}
     * @return the created marker
     */
    AMarker createMarker(String markerType);

    /**
     * Deletes all markers of the given type from this resource. If {@code includeSubtypes} is
     * {@code true}, then markers of types that are subtypes of the given type are also deleted. The
     * {@code depth} determines whether only markers from this resource are deleted or also from its
     * children.
     *
     * @param type a {@link AMarker#getType() marker type}
     * @param includeSubtypes whether to include subtypes of the given marker type
     * @param depth whether to clean only this resource, or also its children (and their
     *            children...)
     */
    void deleteMarkers(String type, boolean includeSubtypes, AResourceTreeTraversalDepth depth);

    /**
     * Deletes the resource from the file system.
     *
     * @param monitor a progress monitor that is notified about the deletion process. When deleting
     *            a directory, individual file deletions are reported to the monitor to allow
     *            fine-grained progress reporting. The monitor may be {@code null} when progress
     *            does not need to be reported.
     */
    void delete(@CheckForNull IProgressMonitor monitor);

    /**
     * Copies this resource to the given destination.
     *
     * @param destination an existing resource of the same {@link AResourceType type} as this
     *            resource
     * @param monitor a progress monitor that is notified about the copy process. When copying a
     *            directory, individual file copies are reported to the monitor to allow
     *            fine-grained progress reporting. The monitor may be {@code null} when progress
     *            does not need to be reported.
     */
    void copy(Path destination, @CheckForNull IProgressMonitor monitor);

    @Override
    public default int compareTo(AResource o) {
        return COMPARING_BY_NAME.compare(this, o);
    }

    /**
     * The type of {@link AResource a resource} can be used instead of checking for
     * {@code instanceof} with concrete implementations.
     */
    public enum AResourceType {
        /** A file, usually inside a {@link #FOLDER}, implemented by {@link AFile}. */
        FILE,
        /**
         * A folder/directory; can contain {@link #FILE FILEs} and other {@link #FOLDER FOLDERs},
         * implemented by {@link AFolder} and subclasses.
         */
        FOLDER,
        /** A project is a special {@link #FOLDER} inside a {@link #WORKSPACE}. */
        PROJECT,
        /** A workspace is a special folder that contains {@link #PROJECT PROJECTs}. */
        WORKSPACE;
    }

    public enum AResourceTreeTraversalDepth {
        /** This resource, but not any of its members. */
        RESOURCE_ONLY,
        /** This resource and its direct members. */
        RESOURCE_AND_DIRECT_MEMBERS,
        /** This resource and its direct and indirect members at any depth. */
        INFINITE;

        public AResourceTreeTraversalDepth decrement() {
            switch (this) {
                case RESOURCE_AND_DIRECT_MEMBERS:
                    return RESOURCE_ONLY;
                default:
                    return this;
            }
        }
    }

    public abstract static class AEclipseResource extends AWrapper<IResource> implements AResource {

        protected AEclipseResource(IResource resource) {
            super(resource);
        }

        @SuppressWarnings("unchecked")
        @Override
        public IResource unwrap() {
            return super.unwrap();
        }

        IResource resource() {
            return unwrap();
        }

        @Override
        public <T> T getAdapter(Class<T> adapter) {
            return resource().getAdapter(adapter);
        }

        @Override
        public boolean isAccessible() {
            return resource().isAccessible();
        }

        @Override
        public boolean exists() {
            return resource().exists();
        }

        @Override
        public AContainer getParent() {
            return wrap(resource().getParent()).as(AContainer.class);
        }

        @Override
        public String getName() {
            return resource().getName();
        }

        @Override
        public Path getLocation() {
            return Path.of(resource().getLocation().toOSString());
        }

        @Override
        public Path getProjectRelativePath() {
            return Path.of(resource().getProjectRelativePath().toOSString());
        }

        @Override
        public Path getWorkspaceRelativePath() {
            return Path.of(resource().getFullPath().toOSString());
        }

        @Override
        public void delete(IProgressMonitor monitor) {
            run(() -> resource().delete(true, monitor));
        }

        @Override
        public long getModificationStamp() {
            return resource().getModificationStamp();
        }

        @Override
        public long getLocalTimeStamp() {
            return resource().getLocalTimeStamp();
        }

        @Override
        public AResourceType getType() {
            int type = resource().getType();
            switch (type) {
                case IResource.FILE:
                    return AResourceType.FILE;
                case IResource.FOLDER:
                    return AResourceType.FOLDER;
                case IResource.PROJECT:
                    return AResourceType.PROJECT;
                case IResource.ROOT:
                    return AResourceType.WORKSPACE;
                default:
                    throw new IllegalStateException(getName() + " uses the undefined resource type " + type); //$NON-NLS-1$
            }
        }

        @Override
        public Set<AMarker> findMarkers(String type, boolean includeSubtypes, AResourceTreeTraversalDepth depth) {
            return wrap(() -> resource().findMarkers(type, includeSubtypes, to(depth))).asSetOf(AMarker.class);
        }

        @Override
        public AMarker createMarker(String markerType) {
            return wrap(() -> resource().createMarker(markerType)).as(AMarker.class);
        }

        @Override
        public void deleteMarkers(String type, boolean includeSubtypes, AResourceTreeTraversalDepth depth) {
            run(() -> resource().deleteMarkers(type, includeSubtypes, to(depth)));
        }

        @Override
        public boolean isDerived() {
            return resource().isDerived();
        }

        @Override
        public void setDerived(boolean isDerived, IProgressMonitor monitor) {
            run(() -> resource().setDerived(isDerived, monitor));
        }

        @Override
        public AProject getProject() {
            return wrap(resource().getProject()).as(AProject.class);
        }

        @Override
        public void refreshLocal(AResourceTreeTraversalDepth depth, IProgressMonitor monitor) {
            run(() -> resource().refreshLocal(to(depth), monitor));
        }

        @Override
        public AWorkspace getWorkspace() {
            return wrap(resource().getWorkspace()).as(AWorkspace.class);
        }

        @Override
        public void copy(Path destination, IProgressMonitor monitor) {
            run(() -> resource().copy(toEclipsePath(destination), true, monitor));
        }

        @Override
        public void touch(IProgressMonitor monitor) {
            run(() -> resource().touch(monitor));
        }

        @Override
        public boolean isSynchronized(AResourceTreeTraversalDepth depth) {
            return resource().isSynchronized(to(depth));
        }

        private static int to(AResourceTreeTraversalDepth depth) {
            switch (depth) {
                case RESOURCE_ONLY:
                    return IResource.DEPTH_ZERO;
                case RESOURCE_AND_DIRECT_MEMBERS:
                    return IResource.DEPTH_ONE;
                case INFINITE:
                    return IResource.DEPTH_INFINITE;

                default:
                    throw new IllegalArgumentException("Unknown depth: " + depth); //$NON-NLS-1$
            }
        }

    }

    public abstract static class PlainJavaResource extends AWrapper<File> implements AResource {

        private Long lastModified;
        private volatile Set<PlainJavaMarker> markers = null;

        // TODO ggf. aus Git/Maven initialisieren?
        private boolean derived;

        public PlainJavaResource(File wrapped) {
            super(wrapped);
        }

        /**
         * Creates this resource.
         */
        abstract void create();

        File file() {
            return unwrap();
        }

        @Override
        public <T> T getAdapter(Class<T> adapter) {
            return AAdaptersFactory.getAdapter(this, adapter);
        }

        @Override
        public boolean isAccessible() {
            return exists();
        }

        @Override
        public boolean exists() {
            return file().exists();
        }

        @Override
        public AContainer getParent() {
            File parentFile = file().getParentFile();
            return parentFile == null ? null
                    : (AContainer)getWorkspace().getRoot().get(parentFile.toPath());
        }

        @Override
        public String getName() {
            return file().getName();
        }

        @Override
        public Path getLocation() {
            return file().toPath();
        }

        @Override
        public Path getProjectRelativePath() {
            AProject project = getProject();
            if (project != null) {
                File projectDirectory = project.unwrap();
                if (projectDirectory != null) {
                    return projectDirectory.toPath().relativize(file().toPath());
                }
            }
            return Path.of(StringUtils.EMPTY);
        }

        @Override
        public Path getWorkspaceRelativePath() {
            PlainJavaWorkspace workspace = getWorkspace();
            if (workspace != null) {
                File workspaceDirectory = workspace.unwrap();
                if (workspaceDirectory != null) {
                    return workspaceDirectory.toPath().relativize(file().toPath());
                }
            }
            return Path.of(StringUtils.EMPTY);
        }

        @Override
        public void delete(IProgressMonitor monitor) {
            // TODO auch im IpsModel entsprechende Objekte löschen, siehe
            // org.faktorips.devtools.model.internal.PlainJavaIpsModelTest.testClearIpsSrcFileContentsCacheWhenFileDeleted()
            PlainJavaUtil.walk(file(), monitor, "Deleting", Files::delete); //$NON-NLS-1$
        }

        @Override
        public long getModificationStamp() {
            lastModified = file().lastModified();
            return lastModified;
        }

        @Override
        public long getLocalTimeStamp() {
            return lastModified != null ? lastModified : getModificationStamp();
        }

        @Override
        public Set<AMarker> findMarkers(String type, boolean includeSubtypes, AResourceTreeTraversalDepth depth) {
            Set<AMarker> foundMarkers = new LinkedHashSet<>();
            recursive(r -> r.findMarkersInternal(type, includeSubtypes, foundMarkers), depth);
            return foundMarkers;
        }

        private void findMarkersInternal(String type, boolean includeSubtypes, Set<AMarker> foundMarkers) {
            if (markers != null) {
                synchronized (markers) {
                    markers.stream().filter(m -> m.unwrap().equalsType(type, includeSubtypes))
                            .forEach(foundMarkers::add);
                }
            }
        }

        /**
         * Executes the given {@link Consumer} for this resource and, according to the given depth,
         * its children.
         *
         * @param depth is ignored if this resource is not {@link AContainer}.
         */
        protected void recursive(Consumer<PlainJavaResource> consumer, AResourceTreeTraversalDepth depth) {
            consumer.accept(this);
        }

        /**
         * Executes the given {@link Consumer} for this resource and, according to the given depth,
         * its children, notifying the given monitor of progress of the given task.
         *
         * @param depth is ignored if this resource is not {@link AContainer}.
         * @param monitor a progress monitor that is notified about this process. When processing a
         *            directory, individual file processing is reported to the monitor to allow
         *            fine-grained progress reporting. The monitor may be {@code null} when progress
         *            does not need to be reported.
         * @param taskName the name of the task, to be reported to the progress monitor
         */
        protected void recursive(Consumer<PlainJavaResource> consumer,
                AResourceTreeTraversalDepth depth,
                @CheckForNull IProgressMonitor monitor,
                String taskName) {
            if (monitor == null) {
                recursive(consumer, depth);
            } else {
                AtomicInteger count = new AtomicInteger();
                recursive(r -> count.incrementAndGet(), depth);
                monitor.beginTask(taskName + ' ' + getName(), count.get());
                recursive(resource -> {
                    consumer.accept(resource);
                    monitor.internalWorked(1);
                }, depth);
                monitor.done();
            }
        }

        @Override
        public AMarker createMarker(String markerType) {
            PlainJavaMarker marker = new PlainJavaMarker(this, markerType);
            if (markers == null) {
                synchronized (this) {
                    if (markers == null) {
                        markers = new LinkedHashSet<>();
                    }
                }
            }
            synchronized (markers) {
                if (markers.add(marker)) {
                    return marker;
                } else {
                    return markers.stream().filter(Predicate.isEqual(marker)).findFirst().get();
                }
            }
        }

        @Override
        public void deleteMarkers(String type, boolean includeSubtypes, AResourceTreeTraversalDepth depth) {
            recursive(r -> r.deletMarkersInternal(type, includeSubtypes), depth);
        }

        private void deletMarkersInternal(String type, boolean includeSubtypes) {
            if (markers != null) {
                synchronized (markers) {
                    for (Iterator<PlainJavaMarker> iterator = markers.iterator(); iterator.hasNext();) {
                        PlainJavaMarkerImpl marker = iterator.next().unwrap();
                        if (marker.equalsType(type, includeSubtypes)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        public void deleteMarker(PlainJavaMarker plainJavaMarker) {
            if (markers != null) {
                synchronized (markers) {
                    markers.remove(plainJavaMarker);
                }
            }
        }

        @Override
        public boolean isDerived() {
            return derived;
        }

        @Override
        public void setDerived(boolean isDerived, IProgressMonitor monitor) {
            PlainJavaUtil.walk(file(), monitor, "Marking as derived", //$NON-NLS-1$
                    p -> getWorkspace().getRoot().get(p).derived = isDerived);
        }

        @Override
        public AProject getProject() {
            for (AResource resource = this; resource != null; resource = resource.getParent()) {
                if (resource instanceof AProject) {
                    return (AProject)resource;
                }
            }
            return null;
        }

        @Override
        public void refreshLocal(AResourceTreeTraversalDepth depth, IProgressMonitor monitor) {
            recursive(PlainJavaResource::refreshInternal, depth, monitor, "refreshing"); //$NON-NLS-1$
        }

        protected void refreshInternal() {
            getModificationStamp();
        }

        @Override
        public PlainJavaWorkspace getWorkspace() {
            return (PlainJavaWorkspace)Abstractions.getWorkspace();
        }

        @Override
        public void copy(Path destination, IProgressMonitor monitor) {
            Path source = file().toPath();
            PlainJavaUtil.walk(file(), monitor, "Copying", //$NON-NLS-1$
                    p -> Files.copy(p, destination.resolve(source.relativize(p)),
                            StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING));
        }

        @Override
        public void touch(IProgressMonitor monitor) {
            if (!exists()) {
                create();
            }
            file().setLastModified(System.currentTimeMillis());
        }

        @Override
        public boolean isSynchronized(AResourceTreeTraversalDepth depth) {
            AtomicBoolean isSynchronized = new AtomicBoolean(true);
            recursive(PlainJavaResource::isSynchronizedInternal, depth);
            return isSynchronized.get();
        }

        protected boolean isSynchronizedInternal() {
            return lastModified == getLocalTimeStamp();
        }

    }
}
