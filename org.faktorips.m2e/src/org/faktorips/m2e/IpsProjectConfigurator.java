/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.m2e;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.ProjectUtil;

/**
 * The {@link IpsProjectConfigurator} checks if there is an existing .ipsproject configuration file.
 * In this case it re-adds the project nature {@link IIpsProject#NATURE_ID} if it is missing.
 */
public class IpsProjectConfigurator extends AbstractProjectConfigurator {

    @Override
    public void configure(ProjectConfigurationRequest request, IProgressMonitor progressMonitor)
            throws CoreRuntimeException {
        progressMonitor.beginTask("Adding Faktor IPS nature and builder", 2); //$NON-NLS-1$
        IFile file = request.getProject().getFile(IIpsProject.PROPERTY_FILE_EXTENSION_INCL_DOT);
        if (!file.exists()) {
            progressMonitor.done();
            return;
        }
        IProject project = request.getProject();
        IJavaProject javaProject = JavaCore.create(project);
        if (javaProject == null) {
            progressMonitor.done();
            return;
        }
        progressMonitor.worked(1);
        configureIpsProject(javaProject);
        progressMonitor.worked(1);
        progressMonitor.done();
    }

    public void configureIpsProject(IJavaProject javaProject) throws CoreRuntimeException {
        try {
            if (javaProject.getProject().getNature(IIpsProject.NATURE_ID) == null) {
                ProjectUtil.addIpsNature(javaProject.getProject());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
