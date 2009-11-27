/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 *
 */
public abstract class IpsElement extends PlatformObject implements IIpsElement, IAdaptable {

    protected String name;
    protected IIpsElement parent;

    final static IIpsElement[] NO_CHILDREN = new IIpsElement[0];

    /*
     * Resource mapping based on the mapping for the resource model
     */
    private class IpsElementResourceMapping extends ResourceMapping {
        private IIpsElement ipsElement;

        public IpsElementResourceMapping(IIpsElement ipsElement) {
            this.ipsElement = ipsElement;
        }

        @Override
        public Object getModelObject() {
            return ipsElement.getEnclosingResource();
        }

        @Override
        public String getModelProviderId() {
            return ModelProvider.RESOURCE_MODEL_PROVIDER_ID;
        }

        @Override
        public IProject[] getProjects() {
            IIpsProject ipsProject = ipsElement.getIpsProject();
            return new IProject[] { ipsProject.getProject() };
        }

        @Override
        public ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor monitor) {
            Object modelObject = getModelObject();
            if (modelObject instanceof IResource) {
                final IResource resource = (IResource)modelObject;
                if (resource.getType() == IResource.ROOT) {
                    return new ResourceTraversal[] { new ResourceTraversal(((IWorkspaceRoot)resource).getProjects(),
                            IResource.DEPTH_INFINITE, IResource.NONE) };
                }
                return new ResourceTraversal[] { new ResourceTraversal(new IResource[] { resource },
                        IResource.DEPTH_INFINITE, IResource.NONE) };
            }
            return null;
        }
    }

    public IpsElement(IIpsElement parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Constructor for testing purposes.
     */
    public IpsElement() {
    }

    /**
     * This method does not query any <code>AdapterManager</code>s to get the adapter - if the
     * requested adapter is an <code>IResource</code>, the enclosing resource is returned.
     * 
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    // IAdaptable uses unchecked Class
    public Object getAdapter(Class adapter) {
        if (adapter == null) {
            return null;
        }
        IResource enclosingResource = getEnclosingResource();
        if (adapter.isInstance(enclosingResource)) {
            return enclosingResource;
        }
        if (adapter.equals(ResourceMapping.class)) {
            return new IpsElementResourceMapping(this);
        }
        return super.getAdapter(adapter);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public final IIpsElement getParent() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists() {
        if (!getParent().exists()) {
            return false;
        }
        if (getCorrespondingResource() == null) {
            // if no corresponding resource exists, the EnclosingResource.exists() is handled
            // by calling getParent().exists() above. So if we have arrived here, we have
            // to return true (the parent exists) to avoid a NullPointerException in the
            // rest of the code.
            return true;
        }
        return getCorrespondingResource().exists();
    }

    /**
     * {@inheritDoc}
     */
    public IResource getEnclosingResource() {
        IResource resource = getCorrespondingResource();
        if (resource != null) {
            return resource;
        }
        return getParent().getEnclosingResource();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsModel getIpsModel() {
        return IpsPlugin.getDefault().getIpsModel();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getIpsProject();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() throws CoreException {
        return NO_CHILDREN;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren() throws CoreException {
        return getChildren().length > 0;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IIpsElement)) {
            return false;
        }
        IIpsElement other = (IIpsElement)o;
        return other.getName().equals(getName())
                && ((parent == null && other.getParent() == null) || (parent != null && parent
                        .equals(other.getParent())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (getParent() == null) {
            return getName();
        }
        return getParent().toString() + "/" + getName(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isContainedInArchive() {
        return getParent().isContainedInArchive();
    }

}
