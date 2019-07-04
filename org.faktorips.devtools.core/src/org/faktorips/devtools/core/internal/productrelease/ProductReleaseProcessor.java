/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.productrelease;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.faktorips.devtools.core.productrelease.ITeamOperationsFactory;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;
import org.faktorips.util.message.MessageList;

public class ProductReleaseProcessor {

    public static final String VERSION_FORMAT_REGEX = "versionFormatRegex"; //$NON-NLS-1$

    public static final String READABLE_VERSION_FORMAT = "readableVersionFormat"; //$NON-NLS-1$

    private static final String RELEASE_EXTENSION_POINT_NAME = "productReleaseExtension"; //$NON-NLS-1$

    private static final String EXTENSION_OPERATION_PROPERTY = "operation"; //$NON-NLS-1$

    private ITeamOperations teamOperation;
    private final IIpsProject ipsProject;
    private final IReleaseAndDeploymentOperation releaseAndDeploymentOperation;

    private final ObservableProgressMessages observableProgressMessages;

    public ProductReleaseProcessor(IIpsProject ipsProject, ObservableProgressMessages observableProgressMessages)
            throws CoreException {
        this.ipsProject = ipsProject;
        teamOperation = getTeamOperations(ipsProject, observableProgressMessages);
        this.observableProgressMessages = observableProgressMessages;
        releaseAndDeploymentOperation = loadReleaseDeploymentOperation(ipsProject);
        if (releaseAndDeploymentOperation != null) {
            releaseAndDeploymentOperation.setObservableProgressMessages(observableProgressMessages);
        }
    }

    private ITeamOperations getTeamOperations(final IIpsProject ipsProject,
            final ObservableProgressMessages observableProgressMessages) {
        for (ITeamOperationsFactory factory : IpsPlugin.getDefault().getTeamOperationsFactories()) {
            if (factory.canCreateTeamOperationsFor(ipsProject)) {
                return factory.createTeamOperations(observableProgressMessages);
            }
        }
        return new NoVersionControlTeamOperations();
    }

    public IReleaseAndDeploymentOperation getReleaseAndDeploymentOperation() {
        return releaseAndDeploymentOperation;
    }

    public boolean startReleaseBuilder(String newVersion,
            List<ITargetSystem> selectedTargetSystems,
            IProgressMonitor monitor) throws InterruptedException, CoreException {
        if (getReleaseAndDeploymentOperation() == null) {
            observableProgressMessages.error(Messages.ReleaseAndDeploymentOperation_exception_noDeploymentExtension);
            return false;
        }
        monitor.beginTask(null, 100);
        observableProgressMessages.info(Messages.ProductReleaseProcessor_status_start);

        try {
            String tag = buildRelease(ipsProject, newVersion, new SubProgressMonitor(monitor, 50));
            // start extended release
            boolean buildReleaseAndDeployment = getReleaseAndDeploymentOperation().buildReleaseAndDeployment(
                    ipsProject, tag, selectedTargetSystems, new SubProgressMonitor(monitor, 50));
            return buildReleaseAndDeployment;
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
        monitor.beginTask(null, 95);
        try {
            monitor.worked(1);

            // check project is synchrony with filesystem
            checkSyncWithFilesystem(ipsProject, new SubProgressMonitor(monitor, 4));

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

            if (!getReleaseAndDeploymentOperation().preCommit(ipsProject, new SubProgressMonitor(monitor, 5))) {
                throw new InterruptedException(Messages.ProductReleaseProcessor_error_custom_validation_failed);
            }

            // commit property file and toc file
            commitFiles(ipsProject, newVersion, new SubProgressMonitor(monitor, 10));

            // tag the project with the new version
            String tagProject = tagProject(ipsProject, newVersion, new SubProgressMonitor(monitor, 20));

            return tagProject;
        } finally {
            monitor.done();
        }
    }

    public IReleaseAndDeploymentOperation loadReleaseDeploymentOperation(IIpsProject ipsProject) throws CoreException {
        IConfigurationElement releaseExtensionElement = getReleaseExtensionElement(ipsProject);
        if (releaseExtensionElement == null) {
            return null;
        }
        IReleaseAndDeploymentOperation result = (IReleaseAndDeploymentOperation)releaseExtensionElement
                .createExecutableExtension(EXTENSION_OPERATION_PROPERTY);
        return result;
    }

    private void checkSyncWithFilesystem(IIpsProject ipsProject, IProgressMonitor monitor) throws CoreException {
        try {
            if (!ipsProject.getProject().isSynchronized(IResource.DEPTH_INFINITE)) {
                ipsProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
            }
        } finally {
            monitor.done();
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
        observableProgressMessages.info(NLS.bind(Messages.ProductReleaseProcessor_status_new_version_set, newVersion));
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
        } else {
            observableProgressMessages.info(Messages.ProductReleaseProcessor_status_build_success);
        }
    }

    private void commitFiles(IIpsProject ipsProject, String newVersion, IProgressMonitor monitor) throws CoreException,
            InterruptedException {
        List<IResource> resources = new ArrayList<IResource>();
        resources.add(ipsProject.getProject().getFile(IpsProject.PROPERTY_FILE_EXTENSION_INCL_DOT));
        for (IIpsPackageFragmentRoot root : ipsProject.getIpsPackageFragmentRoots()) {
            IFile tocFile = ipsProject.getIpsArtefactBuilderSet().getRuntimeRepositoryTocFile(root);
            if (tocFile != null) {
                resources.add(tocFile);
            }
        }

        resources.addAll(getReleaseAndDeploymentOperation().additionalResourcesToCommit(ipsProject));

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
        String tagName = getReleaseAndDeploymentOperation().getTagName(newVersion, ipsProject);
        teamOperation.tagProject(tagName, ipsProject.getProject(), monitor);
        return tagName;
    }

    private void buildProject(IIpsProject ipsProject, IProgressMonitor monitor) throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
    }

    public static IConfigurationElement getReleaseExtensionElement(IIpsProject ipsProject) {
        String releasaeExtensionId = ipsProject.getReadOnlyProperties().getReleaseExtensionId();
        IConfigurationElement[] configElements = IpsPlugin.getDefault().getExtensionRegistry()
                .getConfigurationElementsFor(IpsPlugin.PLUGIN_ID, RELEASE_EXTENSION_POINT_NAME);
        for (IConfigurationElement confElement : configElements) {
            if (confElement.getAttribute("id").equals(releasaeExtensionId)) { //$NON-NLS-1$
                return confElement;
            }
        }
        return null;
    }

}
