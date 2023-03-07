/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * ViewerFilter for <code>ProductExplorer</code> viewpart. It is used to filter out all projects
 * that are not productdefinition projects.
 * 
 * @author Stefan Widmaier
 */
public class ProductExplorerFilter extends ViewerFilter {

    private boolean excludeNoIpsProductDefinitionProjects;

    /**
     * Returns <code>false</code> if the element is an <code>IFile</code> with name ".ipsproject".
     * <p>
     * Returns <code>false</code> if the resource is excluded in the ips project properties.
     * <p>
     * Returns <code>true</code> otherwise.
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (excludeNoIpsProductDefinitionProjects) {
            if (element instanceof IIpsProject) {
                if (!((IIpsProject)element).isProductDefinitionProject()) {
                    return false;
                }
            } else if (element instanceof IProject) {
                return false;
            }
        }

        if (!isAllowedReosource(element)) {
            return false;
        }

        if (element instanceof IFile) {
            // filter out rest of hidden files (e.g. ".ipsproject")
            if (((IFile)element).getName().equals(IIpsPackageFragment.SORT_ORDER_FILE_NAME)) {
                return true;
            }

            if (((IFile)element).getName().indexOf(".") == 0) { //$NON-NLS-1$
                return false;
            }
        }
        if (element instanceof IIpsObject) {
            return ((IIpsObject)element).getIpsObjectType().isProductDefinitionType();
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the element should be displayed.<br>
     * Returns <code>false</code> if the element shouldn't be displayed.<br>
     */
    private boolean isAllowedReosource(Object element) {
        IIpsProject ipsProject = null;
        AResource resource = null;
        if (element instanceof IResource) {
            resource = Wrappers.wrap(element).as(AResource.class);
            ipsProject = IIpsModel.get().getIpsProject(resource.getProject());
        } else if (element instanceof IIpsElement ipsElement) {
            resource = ipsElement.getCorrespondingResource();
            ipsProject = ipsElement.getIpsProject();
        } else {
            // unknown elements will always be displayed
            return true;
        }
        return !ipsProject.isResourceExcludedFromProductDefinition(resource);
    }

    /**
     * Set if no ips product definition projects should be excluded (<code>true</code>) or not (
     * <code>false</code>).
     */
    public void setExcludeNoIpsProductDefinitionProjects(boolean excludeNoIpsProductDefinitionProjects) {
        this.excludeNoIpsProductDefinitionProjects = excludeNoIpsProductDefinitionProjects;
    }
}
