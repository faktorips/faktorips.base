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

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

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

    /**
     * Moves this resource to the given destination.
     *
     * @param destination an existing resource of the same {@link AResourceType type} as this
     *            resource
     * @param monitor a progress monitor that is notified about the move process. When moving a
     *            directory, individual file copies are reported to the monitor to allow
     *            fine-grained progress reporting. The monitor may be {@code null} when progress
     *            does not need to be reported.
     */
    void move(Path destination, @CheckForNull IProgressMonitor monitor);

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
}
