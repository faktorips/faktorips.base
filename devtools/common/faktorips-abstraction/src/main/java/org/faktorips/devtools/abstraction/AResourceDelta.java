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
import java.util.Set;

/**
 * A resource delta is the difference between two points in time for a given (container) resource.
 */
public interface AResourceDelta extends AAbstraction {

    /**
     * Returns the delta for the resource identified by the given path relative to this delta's
     * resource.
     *
     * @param path a relative path to a resource contained in this delta's resource
     * @return the child delta or {@code null} if no such resource exists
     */
    AResourceDelta findMember(Path path);

    /**
     * Returns the resource this delta refers to.
     */
    AResource getResource();

    /**
     * Returns the kind of change this delta represents.
     */
    AResourceDeltaKind getKind();

    /**
     * Returns {@link AResourceDeltaFlag AResourceDeltaFlags} specifying details about this delta.
     */
    Set<AResourceDeltaFlag> getFlags();

    void accept(AResourceDeltaVisitor visitor);

    /**
     * Different kinds of resource changes.
     */
    public enum AResourceDeltaKind {
        /** The resource was newly added to its parent */
        ADDED,
        /** The resource was newly removed from its parent */
        REMOVED,
        /** The resource was changed in any other way, for example its content changed */
        CHANGED;
    }

    /**
     * Different flags that define what a change might be.
     */
    public enum AResourceDeltaFlag {
        /** Content of the resource has changed */
        CONTENT,
        /** The derived flag of the resource has changed */
        DERIVED_CHANGED,
        /** That a project's description has changed */
        DESCRIPTION,
        /** The encoding of the resource has changed */
        ENCODING,
        /** The underlying file or folder of the linked resource has been added or removed */
        LOCAL_CHANGED,
        /** The resource was opened or closed or did not exist */
        OPEN,
        /** The resource was moved to another location */
        MOVED_TO,
        /** The resource was moved from another location */
        MOVED_FROM,
        /** The resource was copied from another location */
        COPIED_FROM,
        /** The type of the resource has changed */
        TYPE,
        /** the resource's sync status has changed */
        SYNC,
        /** Markers of a resource changed */
        MARKERS,
        /** The resource has been replaced by another at the same location */
        REPLACED;
    }
}
