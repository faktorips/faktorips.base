/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.IIpsProblemChangedListener;

/**
 * Listens to resource deltas and filters for marker changes of type
 * {@link IpsPlugin#PROBLEM_MARKER} Viewers or editors showing error ticks should register as
 * listener to this type.
 * 
 * @author Joerg Ortmann
 */
public class IpsProblemMarkerManager implements IResourceChangeListener {

    private final List<IIpsProblemChangedListener> listeners = new CopyOnWriteArrayList<IIpsProblemChangedListener>();

    public IpsProblemMarkerManager() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD);
    }

    /**
     * Removes the given ips problem change listener.
     */
    public void removeListener(IIpsProblemChangedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Adds the given ips problem change listener.
     */
    public void addListener(IIpsProblemChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        HashSet<IResource> changedElements = new HashSet<IResource>();

        try {
            IResourceDelta delta = event.getDelta();
            if (delta != null) {
                delta.accept(new ProjectErrorVisitor(changedElements));
            }
        } catch (CoreException e) {
            IpsPlugin.log(e.getStatus());
        }

        if (!changedElements.isEmpty()) {
            fireChanges(changedElements.toArray(new IResource[changedElements.size()]));
        }
    }

    /**
     * inform all registered ips problem change listener about the ips problem changes
     */
    private void fireChanges(IResource[] changes) {
        // copy to avoid concurrent modifications!
        for (IIpsProblemChangedListener listener : listeners) {
            listener.problemsChanged(changes);
        }
    }

    /**
     * Visitors used to look if the element change delta contains a relevant marker change.
     */
    private static class ProjectErrorVisitor implements IResourceDeltaVisitor {

        private HashSet<IResource> changedElements;

        public ProjectErrorVisitor(HashSet<IResource> changedElements) {
            this.changedElements = changedElements;
        }

        @Override
        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource res = delta.getResource();
            if (res instanceof IProject && delta.getKind() == IResourceDelta.CHANGED) {
                IProject project = (IProject)res;
                if (!project.isAccessible()) {
                    // Only track open projects
                    return false;
                }
            }

            checkInvalidate(delta, res);

            return true;
        }

        /**
         * Check the delta for relevant resources
         */
        private void checkInvalidate(IResourceDelta delta, IResource resource) {
            int kind = delta.getKind();
            if (kind == IResourceDelta.REMOVED || kind == IResourceDelta.ADDED
                    || (kind == IResourceDelta.CHANGED && isErrorDelta(delta))) {
                // invalidate the resource and all parents
                while (resource.getType() != IResource.ROOT && changedElements.add(resource)) {
                    resource = resource.getParent();
                }
            }
        }

        /**
         * Check if there is an ips problem marker change on the given delta
         */
        private boolean isErrorDelta(IResourceDelta delta) {
            if ((delta.getFlags() & IResourceDelta.MARKERS) != 0) {
                IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
                for (IMarkerDelta markerDelta : markerDeltas) {
                    if (markerDelta.isSubtypeOf(IpsPlugin.PROBLEM_MARKER)) {
                        return true;
                    }
                }
            }

            return false;
        }

    }

}
