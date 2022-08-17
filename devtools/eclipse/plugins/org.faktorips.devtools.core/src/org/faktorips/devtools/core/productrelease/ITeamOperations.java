/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.productrelease;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * These team operations are used to check synchronize state, commit files an tag files in a version
 * control system.
 * 
 * @author dirmeier
 */
public interface ITeamOperations {

    /**
     * Check if the given project is synchronized with the configured version control.
     * 
     * @return true if synchronized or false if there are any changes
     */
    boolean isProjectSynchronized(IProject ipsProject, IProgressMonitor monitor);

    /**
     * Commit the files in the project. All files have to be from the same project.
     */
    void commitFiles(IProject project, IResource[] resources, String comment, IProgressMonitor monitor)
            throws TeamException, InterruptedException;

    /**
     * Tag the project with the specified tag name.
     * 
     * @param tagName the tag name used to for source control tagging
     * @param project TODO
     * @param monitor a progress monitor to indicate the progress
     */
    void tagProject(String tagName, IProject project, IProgressMonitor monitor)
            throws TeamException, InterruptedException;

    /**
     * Returns the name of the version control system.
     * 
     * @return the name of the version control system
     */
    String getVersionControlSystem();

}
