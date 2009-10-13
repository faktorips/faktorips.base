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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;

/**
 * A resource delta visitor that computed the ips elements and the resources that needs to be
 * refreshed in the model / product definition explorer.
 * 
 * @author Jan Ortmann
 */
class IpsViewRefreshVisitor implements IResourceDeltaVisitor {

    private Set<IIpsElement> ipsElementsToRefresh = new HashSet<IIpsElement>();
    private Set<IResource> resourcesToRefresh = new HashSet<IResource>();

    public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        IIpsElement element = getIpsElement(resource);
        if (element == null) {
            return handleResource(delta);
        }
        return handleIpsElement(delta, element);
    }

    /**
     * Returns the ips elements that needs to be refreshed for the givne delta.
     */
    Set<IIpsElement> getIpsElementsToRefresh() {
        return ipsElementsToRefresh;
    }

    /**
     * Returns the resources that needs to be refreshed for the givne delta.
     */
    Set<IResource> getResourcesToRefresh() {
        return resourcesToRefresh;
    }

    private boolean handleResource(IResourceDelta delta) {
        IResource parentResource = delta.getResource().getParent();
        IIpsElement parentIpsElement = getIpsElement(parentResource);
        if (isAddedOrRemoved(delta)) {
            if (parentIpsElement != null) {
                // if the parent resource is an ips element, the parent ips element must be
                // refreshed
                // e.g. a package containing a JPEG or an ips project containing a "normal"
                // folder
                registerIpsElementForRefresh(parentIpsElement);
            } else {
                registerResourceForRefresh(parentResource);
            }
        } else {
            if (parentIpsElement != null) {
                registerResourceForRefresh(delta.getResource());
            }
        }
        return true;
    }

    private void registerResourceForRefresh(IResource resource) {
        if (resourcesToRefresh.contains(resource.getParent())) {
            return;
        }
        if (ipsElementsToRefresh.contains(getIpsElement(resource.getParent()))) {
            return;
        }
        resourcesToRefresh.add(resource);
    }

    private boolean handleIpsElement(IResourceDelta delta, IIpsElement ipsElement) {
        if (isAddedOrRemoved(delta)) {
            registerIpsElementForRefresh(ipsElement.getParent());
            return false;
        } else { // changed ips element
            if (delta.getResource().getType() == IResource.FILE) {
                // this applies for ips source files *and* ips package fragment roots based on
                // ips archives!
                registerIpsElementForRefresh(ipsElement);
                return false;
            }
        }
        return true;
    }

    private void registerIpsElementForRefresh(IIpsElement ipsElement) {
        if (!ipsElementsToRefresh.contains(ipsElement.getParent())) {
            ipsElementsToRefresh.add(ipsElement);
        }
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

}