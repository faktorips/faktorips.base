/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * A resource delta visitor that computes the ips elements and the resources that needs to be
 * refreshed in the model / product definition explorer.
 * 
 * @author Jan Ortmann
 */
public class IpsViewRefreshVisitor implements IResourceDeltaVisitor {

    private Set<Object> objectsToRefresh = new HashSet<>();
    private Set<Object> objectsToUpdate = new HashSet<>();

    private ModelContentProvider contentProvider;

    public IpsViewRefreshVisitor(ModelContentProvider contentProvider) {
        super();
        ArgumentCheck.notNull(contentProvider);
        this.contentProvider = contentProvider;
    }

    @Override
    public boolean visit(IResourceDelta delta) {
        IResource resource = delta.getResource();
        if (resource.isTeamPrivateMember()) {
            return handlePrivateTeamMember(resource);
        }
        IIpsElement element = getIpsElement(resource);
        if (element == null) {
            return handleResource(delta);
        } else {
            return handleIpsElement(delta, element);
        }
    }

    private boolean isJavaResource(IResource resource) {
        if (resource == null) {
            return false;
        }
        IProject project = resource.getProject();
        if (project == null || !project.isAccessible()) {
            return false;
        }
        IIpsProject ipsProject = IIpsModel.get().getIpsProject(Wrappers.wrap(resource.getProject()).as(AProject.class));
        if (ipsProject == null) {
            return false;
        }
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
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
            IIpsProject ipsProject = IIpsModel.get()
                    .getIpsProject(Wrappers.wrap(parentResource.getProject()).as(AProject.class));
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
        if (isJavaResource(resource)) {
            return false;
        }
        if (isIpsProjectPropertiesFile(resource)) {
            // we have to return the ips-model here, as a change might lead to removal or addition
            // of the project. Also before the ips nature is added to a project, the element
            // displayed in the explorer is an IProject not an IIpsProject. So registering the new
            // IpsProject for refresh has no effect.
            registerForRefresh(IIpsModel.get());
            return false;
        }
        if (isManifestFile(resource)) {
            if (getIpsProject(resource).getIpsObjectPath().isUsingManifest()) {
                registerForRefresh(IIpsModel.get());
            }
            return false;
        }
        if (isAddedOrRemoved(delta)) {
            registerForRefresh(getParent(resource));
            return false;
        } else if (isSortOrderFile(resource)) {
            registerForRefresh(getParent(resource));
            return false;
        } else {
            registerForUpdate(delta.getResource());
            return true;
        }
    }

    private boolean handleIpsElement(IResourceDelta delta, IIpsElement ipsElement) {
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
        } else {
            return processChangedIpsElement(delta, ipsElement);
        }
    }

    private boolean processChangedIpsElement(IResourceDelta delta, IIpsElement ipsElement) {
        if (delta.getResource().getType() == IResource.FILE) {
            // this applies for ips source files *and* ips package fragment roots based on
            // ips archives!
            registerForRefresh(ipsElement);
            return false;
        } else {
            // This branch is for example executed if a team private member like a CVS folder
            // has changed.
            registerForUpdate(ipsElement);
            return true;
        }
    }

    private IIpsProject getIpsProject(IResource resource) {
        return IIpsModel.get().getIpsProject(Wrappers.wrap(resource.getProject()).as(AProject.class));
    }

    private boolean isManifestFile(IResource resource) {
        return IpsBundleManifest.MANIFEST_NAME.equals(resource.getProjectRelativePath().toPortableString());
    }

    private boolean isSortOrderFile(IResource resource) {
        return IIpsPackageFragment.SORT_ORDER_FILE_NAME.equals(resource.getName());
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
        objectsToUpdate.remove(resourceOrIpsElement);
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
        IIpsElement element = IIpsModel.get().getIpsElement(Wrappers.wrap(resource).as(AResource.class));
        return element;
    }

    private Object getParent(Object resourceOrIpsElement) {
        return contentProvider.getParent(resourceOrIpsElement);
    }

}
