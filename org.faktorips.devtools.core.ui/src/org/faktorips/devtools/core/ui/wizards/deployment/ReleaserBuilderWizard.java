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

package org.faktorips.devtools.core.ui.wizards.deployment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.ui.operations.CommitOperation;
import org.eclipse.team.internal.ccvs.ui.operations.RepositoryProviderOperation;
import org.eclipse.team.internal.ccvs.ui.operations.TagOperation;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.message.MessageList;

/**
 * The deployment wizard provides the basic ui for deployments of product definition projects. On
 * the first site you have to select a project
 * 
 * @author dirmeier
 */
public class ReleaserBuilderWizard extends Wizard {

    private ReleaserBuilderWizardSelectionPage selectionPage;

    public ReleaserBuilderWizard() {
        selectionPage = new ReleaserBuilderWizardSelectionPage();
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        addPage(selectionPage);
    }

    @Override
    public boolean performFinish() {
        final Boolean result = new Boolean(false);
        IRunnableWithProgress progress = new WorkspaceModifyOperation() {

            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
                    InterruptedException {
                monitor.beginTask("Release", 106);

                IIpsProject ipsProject = selectionPage.getIpsProject();

                if (!IpsPlugin.getDefault().getWorkbench().saveAllEditors(true)
                        || ipsProject.getJavaProject().hasUnsavedChanges()) {
                    throw new InterruptedException("There are unsafed Files!");
                }
                monitor.worked(1);

                if (!ipsProject.getProject().isSynchronized(IResource.DEPTH_INFINITE)) {
                    throw new InterruptedException("Filesystem is not synchron!");
                }
                monitor.worked(3);

                if (!isSynchronProject(ipsProject, new SubProgressMonitor(monitor, 10))) {
                    throw new InterruptedException("Project not synchronized with CVS");
                }

                String newVersion = selectionPage.getNewVersion();
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
                commitFile(resources.toArray(new IResource[0]), "update version to " + newVersion,
                        new SubProgressMonitor(monitor, 10));
                if (!isSynchronProject(ipsProject, new SubProgressMonitor(monitor, 10))) {
                    throw new InterruptedException("Some files have changed, project no longer synchronized with CVS");
                }
                if (!tagProject(ipsProject.getProject(), newVersion, new SubProgressMonitor(monitor, 20))) {
                    throw new InterruptedException("Error while tagging project");
                }

                monitor.done();
            }

        };
        try {
            getContainer().run(false, true, progress);
            return true;
        } catch (InvocationTargetException e) {
            selectionPage.setErrorMessage(e.getTargetException().getMessage());
            selectionPage.setPageComplete(false);
            return false;
        } catch (InterruptedException e) {
            selectionPage.setErrorMessage(e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("restriction")
    // we neet to use restricted cvs api to check the project synchronized state
    private boolean isSynchronProject(IIpsProject ipsProject, IProgressMonitor monitor) {
        try {
            org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber subscriber = org.eclipse.team.internal.ccvs.core.CVSProviderPlugin
                    .getPlugin().getCVSWorkspaceSubscriber();
            if (subscriber != null) {
                SyncInfoSet syncInfoSet = new SyncInfoSet();
                subscriber.collectOutOfSync(new IResource[] { ipsProject.getProject() }, IResource.DEPTH_INFINITE,
                        syncInfoSet, monitor);
                return syncInfoSet.isEmpty();
            } else {
                // seems to be no cvs project
                return true;
            }
        } catch (RuntimeException e) {
            // runtime exceptions should not be thrown
            throw e;
        } catch (Exception e) {
            // any exception during cvs check would be catched and 'not synchron' is returned
            return false;
        }
    }

    @SuppressWarnings("restriction")
    private void commitFile(IResource[] resources, String comment, IProgressMonitor monitor) throws TeamException,
            InterruptedException {
        org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber subscriber = org.eclipse.team.internal.ccvs.core.CVSProviderPlugin
                .getPlugin().getCVSWorkspaceSubscriber();
        for (IResource aResource : resources) {
            SyncInfo syncInfo = subscriber.getSyncInfo(aResource);
            if (syncInfo.getKind() != 0 && (syncInfo.getKind() & SyncInfo.OUTGOING) != SyncInfo.OUTGOING) {
                throw new InterruptedException("Some files have remote changes");
            }
        }
        CommitOperation commitOperation = new CommitOperation(null, RepositoryProviderOperation
                .asResourceMappers(resources), new Command.LocalOption[0], comment);
        commitOperation.execute(monitor);
    }

    @SuppressWarnings("restriction")
    private boolean tagProject(IProject project, String tag, IProgressMonitor monitor) throws TeamException,
            InterruptedException {
        ResourceMapping[] mappers = new ResourceMapping[1];
        mappers[0] = (ResourceMapping)project.getAdapter(ResourceMapping.class);
        TagOperation tagOperation = new TagOperation(null, mappers);
        tagOperation.setTag(new CVSTag(tag, CVSTag.VERSION));
        tagOperation.execute(monitor);
        // TODO TAG geht noch nicht
        return true;
    }

    private void buildProject(IIpsProject ipsProject, IProgressMonitor monitor) throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
    }

    public void setIpsProject(IIpsProject ipsProject) {
        selectionPage.setIpsProject(ipsProject);
    }

}
