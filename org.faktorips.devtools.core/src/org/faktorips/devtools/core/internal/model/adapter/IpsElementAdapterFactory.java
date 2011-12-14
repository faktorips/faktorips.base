/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.eclipse.jface.resource.ResourceManager;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This {@link IAdapterFactory} is able to adapt {@link IIpsElement}s to other objects like
 * {@link IResource}
 * 
 * @author dirmeier
 */
public class IpsElementAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("rawtypes")
    // the eclipse API uses raw type
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (!(adaptableObject instanceof IIpsElement)) {
            return null;
        }

        IIpsElement ipsElement = (IIpsElement)adaptableObject;

        try {
            IResource enclosingResource = ipsElement.getEnclosingResource();
            if (adapterType.isInstance(enclosingResource)) {
                return enclosingResource;
            }
            if (adapterType.equals(ResourceMapping.class)) {
                return new IpsElementResourceMapping(ipsElement);
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    // the eclipse API uses raw type
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IResource.class, IProject.class, IFolder.class, IFile.class, ResourceManager.class };
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

}
