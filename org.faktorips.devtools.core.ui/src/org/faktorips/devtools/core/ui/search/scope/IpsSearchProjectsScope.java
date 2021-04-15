/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.scope;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Scope for projects
 * 
 * @author dicker
 */
public class IpsSearchProjectsScope extends AbstractIpsSearchScope {

    protected final ISelection selection;

    public IpsSearchProjectsScope(ISelection selection) {
        this.selection = selection;
    }

    @Override
    protected List<?> getSelectedObjects() {
        List<IProject> selectedProjects = new ArrayList<>();

        if (selection instanceof IStructuredSelection) {
            List<?> list = ((IStructuredSelection)selection).toList();

            for (Object object : list) {
                if (object instanceof IAdaptable) {
                    IAdaptable adaptable = (IAdaptable)object;
                    IResource resource = adaptable.getAdapter(IResource.class);
                    if (resource == null) {
                        continue;
                    }
                    IProject project = resource.getProject();
                    if (project != null && !selectedProjects.contains(project)) {
                        selectedProjects.add(project);
                    }
                }
            }
        }
        return selectedProjects;
    }

    @Override
    protected String getScopeTypeLabel(boolean singular) {
        return singular ? Messages.IpsSearchProjectsScope_scopeTypeLabelSingular
                : Messages.IpsSearchProjectsScope_scopeTypeLabelPlural;
    }

}
