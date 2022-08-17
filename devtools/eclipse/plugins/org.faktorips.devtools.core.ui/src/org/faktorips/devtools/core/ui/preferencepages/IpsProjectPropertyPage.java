/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.dialogs.PropertyPage;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Property page for configuring an aspect of a Faktor-IPS project.
 */
public abstract class IpsProjectPropertyPage extends PropertyPage {

    /**
     * Determine IPS project instance for which the property page has to be created
     */
    protected IIpsProject getIpsProject() {
        IProject project = getProject();
        IIpsProject ipsProject = null;

        if (project != null) {
            ipsProject = IIpsModel.get().getIpsProject(Wrappers.wrap(project).as(AProject.class));
        }
        return ipsProject;
    }

    private IProject getProject() {
        IAdaptable adaptable = getElement();
        IProject project = null;

        if (adaptable instanceof IProject) {
            project = (IProject)adaptable;
        } else {
            IJavaElement elem = adaptable.getAdapter(IJavaElement.class);
            if (elem instanceof IJavaProject) {
                project = ((IJavaProject)elem).getProject();
            }
        }
        return project;
    }

}
