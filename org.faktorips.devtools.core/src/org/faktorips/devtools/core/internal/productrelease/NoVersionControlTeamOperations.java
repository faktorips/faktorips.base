/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
    public String tagProject(IProject project, String version, IProgressMonitor monitor) throws TeamException,
            InterruptedException {
        return version;
    }

    @Override
    public String getVersionControlSystem() {
        return "no version control"; //$NON-NLS-1$
    }

}
