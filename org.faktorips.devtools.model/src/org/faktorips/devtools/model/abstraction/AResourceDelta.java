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

import static org.faktorips.devtools.model.abstraction.Wrappers.wrap;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

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

    public static class EclipseResourceDelta extends AWrapper<IResourceDelta> implements AResourceDelta {

        public EclipseResourceDelta(IResourceDelta resourceDelta) {
            super(resourceDelta);
        }

        @SuppressWarnings("unchecked")
        @Override
        public IResourceDelta unwrap() {
            return super.unwrap();
        }

        IResourceDelta resourceDelta() {
            return unwrap();
        }

        @Override
        public AResourceDelta findMember(IPath path) {
            return wrap(resourceDelta().findMember(path)).as(AResourceDelta.class);
        }

        @Override
        public AResource getResource() {
            return wrap(resourceDelta().getResource()).as(AResource.class);
        }

        @Override
        public AResourceDeltaKind getKind() {
            int kind = resourceDelta().getKind();
            switch (kind) {
                case IResourceDelta.ADDED:
                    return AResourceDeltaKind.ADDED;
                case IResourceDelta.REMOVED:
                    return AResourceDeltaKind.REMOVED;
                default:
                    return AResourceDeltaKind.CHANGED;
            }
        }

        @Override
        public int getFlags() {
            return resourceDelta().getFlags();
        }

        @Override
        public void accept(AResourceDeltaVisitor visitor) {
            try {
                resourceDelta().accept(new IResourceDeltaVisitor() {

                    @Override
                    public boolean visit(IResourceDelta delta) throws CoreException {
                        return visitor.visit(new EclipseResourceDelta(delta));
                    }
                });
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

    }
}
