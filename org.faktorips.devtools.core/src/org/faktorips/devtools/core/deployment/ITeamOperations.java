/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.deployment;

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
     * Check if the given project is synchronized with configured version control.
     * 
     * @return true if synchrony or false when there are any changes
     */
    public boolean isProjectSynchronized(IProject ipsProject, IProgressMonitor monitor);

    /**
     * Commit the files in the project. All files have to be from the same project.
     * 
     */
    public void commitFile(IProject project, IResource[] resources, String comment, IProgressMonitor monitor)
            throws TeamException, InterruptedException;

    /**
     * tag the project with the specified version. Because the version could contain invalid
     * characters, maybe the tagging operation change the version to a valid tag name. This tag name
     * is returned by this method
     * 
     * @param project the project to tag
     * @param version the version which is used as tab name base
     * @param monitor a progress monitor to indicate the progress
     * @return the tag name created from version
     */
    public String tagProject(IProject project, String version, IProgressMonitor monitor) throws TeamException,
            InterruptedException;

    /**
     * Return the name of the version system
     * 
     * @return
     */
    public String getName();

}
