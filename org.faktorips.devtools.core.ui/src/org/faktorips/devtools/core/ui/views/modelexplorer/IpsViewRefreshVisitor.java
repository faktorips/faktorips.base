/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
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

    private ModelContentProvider contentProvider;

    public IpsViewRefreshVisitor(ModelContentProvider contentProvider) {
        super();
        ArgumentCheck.notNull(contentProvider);
        this.contentProvider = contentProvider;
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        if (resource.isTeamPrivateMember()) {
            return handlePrivateTeamMember(resource);
        }
        if (isJavaResource(resource)) {
            return false;
        }
        IIpsElement element = getIpsElement(resource);
        if (element == null) {
            return handleResource(delta);
        }
        return handleIpsElement(delta, element);
    }

    private boolean isJavaResource(IResource resource) {
        if (resource == null) {
            return false;
        }
        IProject project = resource.getProject();
        if (project == null || !project.isAccessible()) {
            // At least the workspace root does return null as project!
            return false;
        }
        IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(resource.getProject());
        if (ipsProject == null) {
            return false;
        }
        IJavaProject javaProject = ipsProject.getJavaProject();
        if (javaProject == null || !javaProject.exists()) {
            return false;
        }
        return contentProvider.isJavaResource(javaProject, resource);
    }

    /**
     * Team private members are resources maintained by a TeamProvider like the CVS and Subversion
     * Plugins. If a team private member has changed, the status of a none-team resource might have
     * changed and label decorations must be updated. So if a team private member is changed, we
     * refresh the parent resource/ips-element it is contained in.
     */
    private boolean handlePrivateTeamMember(IResource privateTeamMember) {
        IResource parentResource = privateTeamMember.getParent();
        if (isJavaResource(parentResource)) {
            // if the team status of a Java resource has changed, we must update the Project
            // to update is't label decoration as well!
            IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(parentResource.getProject());
            if (ipsProject != null) {
                registerForUpdate(ipsProject);
            }
            return false;
        }
        IIpsElement parentElement = getIpsElement(parentResource);
        if (parentElement != null) {
            registerForRefresh(parentElement);
        } else {
            registerForRefresh(parentResource);
        }
        return false;
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
                if (pack.isDefaultPackage()) {
                    // in version up to 2.5.0.rc2 we have the following optimization coded here
                    // if ( pack.isDefaultPackage() && pack.getChildren().length == 1) {
                    // We wanted to refresh the package fragment root only if the default package
                    // has
                    // been empty before. This optimization fails, when several files are created at
                    // one
                    // in a single workspace operation.
                    // See Bug
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
            } else {
                // This branch is for example executed if a team private member like a CVS folder
                // has changed.
                registerForUpdate(ipsElement);
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