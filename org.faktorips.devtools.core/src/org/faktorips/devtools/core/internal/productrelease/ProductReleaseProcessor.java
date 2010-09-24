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

package org.faktorips.devtools.core.internal.productrelease;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.productrelease.IReleaseAndDeploymentOperation;
import org.faktorips.devtools.core.productrelease.ITargetSystem;
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class ProductReleaseProcessor {

    private static final String RELEASE_EXTENSION_POINT_NAME = "productReleaseExtension"; //$NON-NLS-1$
    private static final String EXTENSION_OPERATION_PROPERTY = "operation"; //$NON-NLS-1$
    private ITeamOperations teamOperation;
    private final IIpsProject ipsProject;
    private final IReleaseAndDeploymentOperation releaseAndDeploymentOperation;

    public ProductReleaseProcessor(IIpsProject ipsProject) throws CoreException {
        this.ipsProject = ipsProject;
        teamOperation = new CvsTeamOperations();
        releaseAndDeploymentOperation = getDeploymentOperation(ipsProject);
    }

    public List<ITargetSystem> getTargetSystems() {
        if (releaseAndDeploymentOperation != null) {
            return releaseAndDeploymentOperation.getAvailableTargetSystems();
        } else {
            return new ArrayList<ITargetSystem>();
        }
    }

    public IReleaseAndDeploymentOperation getReleaseAndDeploymentOperation() {
        return releaseAndDeploymentOperation;
    }

    public boolean startReleaseBuilder(String newVersion,
            List<ITargetSystem> selectedTargetSystems,
            MessageList messageList,
            IProgressMonitor monitor) throws InterruptedException, CoreException {
        if (releaseAndDeploymentOperation == null) {
            messageList.add(new Message("ERROR", //$NON-NLS-1$
                    Messages.ReleaseAndDeploymentOperation_exception_noDeploymentExtension, Message.ERROR));
            return false;
        }
        monitor.beginTask(Messages.ReleaseAndDeploymentOperation_taskName_release, 100);
        try {
            String tag = buildRelease(ipsProject, newVersion, monitor);
            // start extended release
            return getDeploymentOperation(ipsProject).buildReleaseAndDeployment(ipsProject, tag, selectedTargetSystems,
                    monitor, messageList);
        } finally {
            monitor.done();
        }
    }

    /**
     * calls all the operations to build a new release
     * 
     * @return the tag used to tag the project in version control system
     */
    private String buildRelease(IIpsProject ipsProject, String newVersion, IProgressMonitor monitor)
            throws JavaModelException, InterruptedException, CoreException, TeamException {
        // save all
        saveAll(ipsProject);
        monitor.worked(1);

        // check project is synchrony with filesystem
        checkSyncWithFilesystem(ipsProject);
        monitor.worked(4);

        // check project is synchrony with repository
        checkSynchronization(ipsProject, new SubProgressMonitor(monitor, 10));

        // update version in project
        updateVersionProperty(ipsProject, newVersion);
        monitor.worked(2);

        // build project
        buildProject(ipsProject, new SubProgressMonitor(monitor, 40));

        // check for fips error markers
        validateIpsProject(ipsProject);
        monitor.worked(2);

        // check for other error markers
        checkProblemMarkers(ipsProject);
        monitor.worked(1);

        // TODO call custom release settings!

        // commit property file and toc file
        commitFiles(ipsProject, newVersion, new SubProgressMonitor(monitor, 10));

        // tag the project with the new version
        return tagProject(ipsProject, newVersion, new SubProgressMonitor(monitor, 20));
    }

    public static IReleaseAndDeploymentOperation getDeploymentOperation(IIpsProject ipsProject) throws CoreException {
        IConfigurationElement releaseExtensionElement = getReleaseExtensionElement(ipsProject);
        if (releaseExtensionElement == null) {
            return null;
        }
        IReleaseAndDeploymentOperation result = IReleaseAndDeploymentOperation.class.cast(releaseExtensionElement
                .createExecutableExtension(EXTENSION_OPERATION_PROPERTY));
        return result;
    }

    public static IConfigurationElement getReleaseExtensionElement(IIpsProject ipsProject) {
        String releasaeExtensionId = ipsProject.getProperties().getReleaseExtensionId();
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(IpsPlugin.PLUGIN_ID,
                RELEASE_EXTENSION_POINT_NAME);
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement confElement : configElements) {
                if (confElement.getAttribute("id").equals(releasaeExtensionId)) { //$NON-NLS-1$
                    return confElement;
                }
            }
        }
        return null;
    }

    private void saveAll(IIpsProject ipsProject) throws JavaModelException, InterruptedException {
        if (!IpsPlugin.getDefault().getWorkbench().saveAllEditors(true)
                || ipsProject.getJavaProject().hasUnsavedChanges()) {
            throw new InterruptedException(Messages.ReleaseAndDeploymentOperation_exception_unsavedChanges);
        }
    }

    private void checkSyncWithFilesystem(IIpsProject ipsProject) throws InterruptedException {
        if (!ipsProject.getProject().isSynchronized(IResource.DEPTH_INFINITE)) {
            throw new InterruptedException(Messages.ReleaseAndDeploymentOperation_exception_refreshFilesystem);
        }
    }

    private void checkSynchronization(IIpsProject ipsProject, IProgressMonitor monitor) throws InterruptedException {
        if (!teamOperation.isProjectSynchronized(ipsProject.getProject(), monitor)) {

            throw new InterruptedException(NLS.bind(Messages.ReleaseAndDeploymentOperation_exception_notSynchron,
                    teamOperation.getVersionControlSystem()));
        }
    }

    private void updateVersionProperty(IIpsProject ipsProject, String newVersion) throws CoreException {
        IIpsProjectProperties projectProperties = ipsProject.getProperties();
        projectProperties.setVersion(newVersion);
        ipsProject.setProperties(projectProperties);
    }

    private void validateIpsProject(IIpsProject ipsProject) throws CoreException, InterruptedException {
        MessageList messages = ipsProject.validate();
        if (messages.containsErrorMsg()) {
            throw new InterruptedException(Messages.ReleaseAndDeploymentOperation_exception_fipsErrors);
        }
    }

    private void checkProblemMarkers(IIpsProject ipsProject) throws CoreException, InterruptedException {
        if (ipsProject.getProject().findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE) == IMarker.SEVERITY_ERROR) {
            throw new InterruptedException(Messages.ReleaseAndDeploymentOperation_exception_errors);
        }
    }

    private void commitFiles(IIpsProject ipsProject, String newVersion, IProgressMonitor monitor) throws CoreException,
            TeamException, InterruptedException {
        List<IResource> resources = new ArrayList<IResource>();
        resources.add(ipsProject.getProject().getFile(IpsProject.PROPERTY_FILE_EXTENSION_INCL_DOT));
        for (IIpsPackageFragmentRoot root : ipsProject.getIpsPackageFragmentRoots()) {
            resources.add(ipsProject.getIpsArtefactBuilderSet().getRuntimeRepositoryTocFile(root));
        }

        // TODO add additional files from extension

        teamOperation
                .commitFiles(ipsProject.getProject(), resources.toArray(new IResource[0]),
                        Messages.ReleaseAndDeploymentOperation_commit_comment + newVersion, new SubProgressMonitor(
                                monitor, 10));
        if (!teamOperation.isProjectSynchronized(ipsProject.getProject(), monitor)) {
            throw new InterruptedException(NLS.bind(Messages.ReleaseAndDeploymentOperation_exception_noLongerSynchron,
                    teamOperation.getVersionControlSystem()));
        }
    }

    private String tagProject(IIpsProject ipsProject, String newVersion, IProgressMonitor monitor)
            throws TeamException, InterruptedException {
        return teamOperation.tagProject(ipsProject.getProject(), newVersion, monitor);
    }

    private void buildProject(IIpsProject ipsProject, IProgressMonitor monitor) throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
    }

}
