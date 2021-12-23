/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import static org.faktorips.devtools.abstraction.Wrappers.run;
import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.abstraction.mapping.PathMapping.toEclipsePath;

import java.nio.file.Path;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.AWrapper;

public abstract class AEclipseResource extends AWrapper<IResource> implements AResource {

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