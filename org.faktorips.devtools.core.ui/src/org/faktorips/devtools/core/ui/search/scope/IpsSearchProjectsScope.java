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
        List<IProject> selectedProjects = new ArrayList<IProject>();

        if (selection instanceof IStructuredSelection) {
            List<?> list = ((IStructuredSelection)selection).toList();

            for (Object object : list) {
                if (object instanceof IAdaptable) {
                    IAdaptable adaptable = (IAdaptable)object;
                    IResource resource = (IResource)adaptable.getAdapter(IResource.class);
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
