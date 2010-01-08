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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * A resource delta visitor that computes the ips elements and the resources that needs to be
 * refreshed in the model / product definition explorer.
 * 
 * @author Jan Ortmann
 */
public class IpsViewRefreshVisitor implements IResourceDeltaVisitor {

    private Set<Object> objectsToRefresh = new HashSet<Object>();
    private Set<Object> objectsToUpdate = new HashSet<Object>();

    private ITreeContentProvider contentProvider;

    public IpsViewRefreshVisitor(ITreeContentProvider contentProvider) {
        super();
        ArgumentCheck.notNull(contentProvider);
        this.contentProvider = contentProvider;
    }

    public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        IIpsElement element = getIpsElement(resource);
        if (element == null) {
            return handleResource(delta);
        }
        return handleIpsElement(delta, element);
    }

    /**
     * Returns the elements (IpsElements and Resources) that needs to be refreshed for the given
     * delta.
     */
    public Set<Object> getElementsToRefresh() {
        return objectsToRefresh;
    }

    /**
     * Returns the elements (IpsElements and Resources) that needs to be updates for the givne
     * delta.
     */
    public Set<Object> getElementsToUpdate() {
        return objectsToUpdate;
    }

    private boolean handleResource(IResourceDelta delta) {
        IResource resource = delta.getResource();
        if (isIpsProjectPropertiesFile(resource)) {
            // we have to return the ips-model here, as a change might lead to removal or addition
            // of the project. Also before the ips nature is added to a project, the element
            // displayed in the explorer is an IProject not an IIpsProject. So registering the new
            // IpsProject for refresh has no effect.
            registerForRefresh(IpsPlugin.getDefault().getIpsModel());
            return false;
        }
        if (isAddedOrRemoved(delta)) {
            registerForRefresh(getParent(resource));
            return false;
        } else {
            registerForUpdate(delta.getResource());
            return true;
        }
    }

    private boolean handleIpsElement(IResourceDelta delta, IIpsElement ipsElement) throws CoreException {
        if (isAddedOrRemoved(delta)) {
            IIpsElement parentEl = (IIpsElement)getParent(ipsElement);
            if (parentEl instanceof IIpsPackageFragment) {
                IIpsPackageFragment pack = (IIpsPackageFragment)parentEl;
                if (pack.isDefaultPackage() && pack.getChildren().length == 1) {
                    // This is the first element in the default package. As the default package is
                    // only
                    // shown, when it contains at least one file/element, we must refresh the
                    // package root!
                    parentEl = pack.getParent();
                }

            }
            registerForRefresh(parentEl);
            return false;
        } else { // changed ips element
            if (delta.getResource().getType() == IResource.FILE) {
                // this applies for ips source files *and* ips package fragment roots based on
                // ips archives!
                registerForRefresh(ipsElement);
                return false;
            }
        }
        return true;
    }

    private IIpsProject getIpsProject(IResource resource) {
        return IpsPlugin.getDefault().getIpsModel().getIpsProject(resource.getProject());
    }

    private boolean isIpsProjectPropertiesFile(IResource resource) {
        IIpsProject ipsProject = getIpsProject(resource);
        if (ipsProject == null) {
            return false;
        }
        return resource.equals(ipsProject.getIpsProjectPropertiesFile());
    }

    private void registerForRefresh(Object resourceOrIpsElement) {
        if (resourceOrIpsElement == null) {
            return;
        }
        if (!objectsToRefresh.add(resourceOrIpsElement)) {
            return;
        }
        objectsToUpdate.remove(resourceOrIpsElement); // if the element is refresh, no need to
        // update it.
        registerForUpdate(getParent(resourceOrIpsElement));
    }

    private void registerForUpdate(Object resourceOrIpsElement) {
        if (resourceOrIpsElement == null) {
            return;
        }
        if (!objectsToUpdate.add(resourceOrIpsElement)) {
            return;
        }
        registerForUpdate(getParent(resourceOrIpsElement));
    }

    private boolean isAddedOrRemoved(IResourceDelta delta) {
        if ((delta.getKind() & IResourceDelta.CHANGED) > 0) {
            return false;
        }
        return delta.getKind() != IResourceDelta.NO_CHANGE;
    }

    private IIpsElement getIpsElement(IResource resource) {
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(resource);
        return element;
    }

    private Object getParent(Object resourceOrIpsElement) {
        return contentProvider.getParent(resourceOrIpsElement);
    }

}