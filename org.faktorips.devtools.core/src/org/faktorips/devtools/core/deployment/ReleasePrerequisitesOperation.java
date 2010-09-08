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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.message.MessageList;

public class ReleasePrerequisitesOperation {

    private ITeamOperations teamOperation;

    public ReleasePrerequisitesOperation() {
        teamOperation = new CvsTeamOperations();
    }

    public void runReleasePrerequisites(IIpsProject ipsProject, String newVersion, IProgressMonitor monitor)
            throws InterruptedException, CoreException {
        monitor.beginTask("Release", 106);

        if (!IpsPlugin.getDefault().getWorkbench().saveAllEditors(true)
                || ipsProject.getJavaProject().hasUnsavedChanges()) {
            throw new InterruptedException("There are unsafed Files!");
        }
        monitor.worked(1);

        if (!ipsProject.getProject().isSynchronized(IResource.DEPTH_INFINITE)) {
            throw new InterruptedException("Filesystem is not synchron!");
        }
        monitor.worked(3);

        if (!teamOperation.isProjectSynchronized(ipsProject.getProject(), new SubProgressMonitor(monitor, 10))) {
            throw new InterruptedException("Project not synchronized with CVS");
        }

        IIpsProjectProperties projectProperties = ipsProject.getProperties();
        projectProperties.setVersion(newVersion);
        ipsProject.setProperties(projectProperties);
        monitor.worked(2);

        buildProject(ipsProject, new SubProgressMonitor(monitor, 40));

        MessageList messages = ipsProject.validate();
        if (messages.containsErrorMsg()) {
            throw new InterruptedException("Project contains Faktor-IPS errors");
        }
        monitor.worked(1);

        if (ipsProject.getProject().findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE) == IMarker.SEVERITY_ERROR) {
            throw new InterruptedException("Project contains errors");
        }
        monitor.worked(1);

        List<IResource> resources = new ArrayList<IResource>();
        resources.add(ipsProject.getProject());
        resources.add(ipsProject.getProject().getFile(".ipsproject"));
        for (IIpsPackageFragmentRoot root : ipsProject.getIpsPackageFragmentRoots()) {
            resources.add(ipsProject.getIpsArtefactBuilderSet().getRuntimeRepositoryTocFile(root));
        }
        teamOperation.commitFile(ipsProject.getProject(), resources.toArray(new IResource[0]), "update version to "
                + newVersion, new SubProgressMonitor(monitor, 10));
        if (!teamOperation.isProjectSynchronized(ipsProject.getProject(), new SubProgressMonitor(monitor, 10))) {
            throw new InterruptedException("Some files have changed, project no longer synchronized with CVS");
        }
        teamOperation.tagProject(ipsProject.getProject(), "v" + newVersion, new SubProgressMonitor(monitor, 20));

        monitor.done();
    }

    private void buildProject(IIpsProject ipsProject, IProgressMonitor monitor) throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
    }

}
