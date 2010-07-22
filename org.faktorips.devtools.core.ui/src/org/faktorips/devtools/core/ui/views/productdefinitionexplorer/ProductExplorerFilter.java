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

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * ViewerFilter for <code>ProductExplorer<code> viewpart. It is used to
 * filter out all projects that are not productdefinition projects.
 * 
 * @author Stefan Widmaier
 */
public class ProductExplorerFilter extends ViewerFilter {

    private boolean excludeNoIpsProductDefinitionProjects;

    public ProductExplorerFilter() {
    }

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
            } else if (element instanceof IProject && ((IProject)element).isOpen()) {
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

    /*
     * Returns <code>true</code> if the element should be displayed.<br> Returns <code>false</code>
     * if the element shouldn't be displayed.<br>
     */
    private boolean isAllowedReosource(Object element) {
        IIpsProject ipsProject = null;
        IResource resource = null;
        if (element instanceof IResource) {
            resource = (IResource)element;
            ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(resource.getProject());
        } else if (element instanceof IIpsElement) {
            IIpsElement ipsElement = ((IIpsElement)element);
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
