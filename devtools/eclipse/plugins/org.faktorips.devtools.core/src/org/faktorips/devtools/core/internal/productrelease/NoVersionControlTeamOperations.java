/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.productrelease;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.faktorips.devtools.core.productrelease.ITeamOperations;

/**
 * {@link ITeamOperations} for projects without version control.
 */
public class NoVersionControlTeamOperations implements ITeamOperations {

    @Override
    public boolean isProjectSynchronized(IProject ipsProject, IProgressMonitor monitor) {
        return true;
    }

    @Override
    public void commitFiles(IProject project, IResource[] resources, String comment, IProgressMonitor monitor)
            throws TeamException, InterruptedException {
        // nothing to do
    }

    @Override
    public void tagProject(String version, IProject project, IProgressMonitor monitor) throws TeamException,
            InterruptedException {
        // do nothing
    }

    @Override
    public String getVersionControlSystem() {
        return "no version control"; //$NON-NLS-1$
    }

}
