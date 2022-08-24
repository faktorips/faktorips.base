/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.eclipse.ipsproject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsproject.IpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class EclipseIpsProject extends IpsProject {
    /** The underlying platform project */
    private IProject project;

    public EclipseIpsProject(IProject project) {
        super(IIpsModel.get(), project.getName());
    }

    public EclipseIpsProject(IIpsModel model, String name) {
        super(model, name);
    }

    public IProject getEclipseProject() {
        if (project == null) {
            // we don't have a threading problem here, as projects are only handles!
            project = ResourcesPlugin.getWorkspace().getRoot().getProject(getName());
        }
        return project;
    }

    @Override
    public boolean exists() {
        if (!super.exists()) {
            return false;
        }
        try {
            String[] natures = getEclipseProject().getDescription().getNatureIds();
            for (String nature : natures) {
                if (nature.equals(IIpsProject.NATURE_ID)) {
                    return true;
                }
            }
        } catch (CoreException e) {
            // does not exist
        }
        return false;
    }
}
