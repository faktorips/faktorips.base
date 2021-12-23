/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AAdaptersFactory;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AWrapper;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.runtime.internal.IpsStringUtils;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public abstract class PlainJavaResource extends AWrapper<File> implements AResource {

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
        return Path.of(IpsStringUtils.EMPTY);
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
        return Path.of(IpsStringUtils.EMPTY);
    }

    @Override
    public void delete(IProgressMonitor monitor) {
        // TODO auch im IpsModel entsprechende Objekte löschen, siehe
        // org.faktorips.devtools.model.internal.PlainJavaIpsModelTest.testClearIpsSrcFileContentsCacheWhenFileDeleted()
        PlainJavaFileUtil.walk(file(), monitor, "Deleting", Files::delete); //$NON-NLS-1$
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
     * Executes the given {@link Consumer} for this resource and, according to the given depth, its
     * children.
     *
     * @param depth is ignored if this resource is not {@link AContainer}.
     */
    protected void recursive(Consumer<PlainJavaResource> consumer, AResourceTreeTraversalDepth depth) {
        consumer.accept(this);
    }

    /**
     * Executes the given {@link Consumer} for this resource and, according to the given depth, its
     * children, notifying the given monitor of progress of the given task.
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
        PlainJavaFileUtil.walk(file(), monitor, "Marking as derived", //$NON-NLS-1$
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
        PlainJavaFileUtil.walk(file(), monitor, "Copying", //$NON-NLS-1$
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