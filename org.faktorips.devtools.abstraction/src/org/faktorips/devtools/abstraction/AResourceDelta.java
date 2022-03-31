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

import org.eclipse.core.runtime.IPath;

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
    AResourceDelta findMember(IPath path);

    /**
     * Returns the resource this delta refers to.
     */
    AResource getResource();

    /**
     * Returns the kind of change this delta represents.
     */
    AResourceDeltaKind getKind();

    /**
     * Returns bit-masked flags specifying details about this delta.
     */
    int getFlags();

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
}
