/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
    public boolean isProjectSynchronized(IProject ipsProject, IProgressMonitor monitor);

    /**
     * Commit the files in the project. All files have to be from the same project.
     */
    public void commitFiles(IProject project, IResource[] resources, String comment, IProgressMonitor monitor)
            throws TeamException, InterruptedException;

    /**
     * Tag the project with the specified version. Because the version could contain invalid
     * characters, the tagging operation may change the version to a valid tag name. This tag name
     * is returned by this method.
     * 
     * @param project the project to tag
     * @param version the version which is used as tab name base
     * @param monitor a progress monitor to indicate the progress
     * @return the tag name created from version
     */
    public String tagProject(IProject project, String version, IProgressMonitor monitor) throws TeamException,
            InterruptedException;

    /**
     * Returns the name of the version control system.
     * 
     * @return the name of the version control system
     */
    public String getVersionControlSystem();

}
