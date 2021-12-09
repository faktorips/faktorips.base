/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.adapter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.abstraction.AResource;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This {@link IAdapterFactory} is able to adapt {@link IIpsElement}s to other objects like
 * {@link IResource}
 * 
 * @author dirmeier
 */
public class IpsElementAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (!(adaptableObject instanceof IIpsElement)) {
            return null;
        }

        IIpsElement ipsElement = (IIpsElement)adaptableObject;
        if (ipsElement instanceof IIpsObjectPart) {
            return null;
        }

        try {
            AResource enclosingResource = ipsElement.getEnclosingResource();
            if (adapterType.isInstance(enclosingResource)) {
                return (T)enclosingResource;
            }
            IResource eclipseResource = enclosingResource.unwrap();
            if (adapterType.isInstance(eclipseResource)) {
                return (T)eclipseResource;
            }
            if (adapterType.equals(ResourceMapping.class)) {
                return (T)new IpsElementResourceMapping(ipsElement);
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class[] { IResource.class, IProject.class, IFolder.class, IFile.class, ResourceMapping.class };
    }

    /**
     * Resource mapping based on the mapping for the resource model.
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
            return new IProject[] { ipsProject.getProject().unwrap() };
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

}
