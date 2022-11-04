/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productrelease;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This interface is implemented for a release deployment extension. The implementation is
 * referenced by the extension <em>releaseDeploymentExtension</em> in the attribute
 * <em>operation</em>.
 * 
 * @author dirmeier
 */
public interface IReleaseAndDeploymentOperation {

    /**
     * This method should return a list of target system where the release could be deployed to.
     * 
     * @param ipsProject The selected {@link IIpsProject}
     * 
     * @return A List of available target systems
     */
    List<ITargetSystem> getAvailableTargetSystems(IIpsProject ipsProject);

    /**
     * Returns a list of resources that have to be committed when the release operation commits the
     * .ipsproject file.
     * 
     * @param ipsProject the selected {@link IIpsProject}
     * 
     * @return List of files to commit
     */
    List<IFile> additionalResourcesToCommit(IIpsProject ipsProject);

    /**
     * This method is called by the release build processor just before committing changed files.
     * The new version is already set in the .ipsproject file and the project have been clean built.
     * 
     * @param ipsProject the project to release
     * @param progressMonitor a {@link IProgressMonitor} to view sate of work
     * @return true when everything was right, false to stop the release process
     * 
     * @deprecated Since 3.7 use {@link #preCommit(IIpsProject, IProgressMonitor)} instead
     */
    @Deprecated
    boolean customReleaseSettings(IIpsProject ipsProject, IProgressMonitor progressMonitor);

    /**
     * This method is called by the release build processor just before committing changed files.
     * The new version is already set in the .ipsproject file and the project have been clean built.
     * 
     * @param ipsProject the project to release
     * @param progressMonitor a {@link IProgressMonitor} to view sate of work
     * @return true when everything was right, false to stop the release process
     */
    boolean preCommit(IIpsProject ipsProject, IProgressMonitor progressMonitor);

    /**
     * Customize the tag name for the new version. This hook is called directly before tagging the
     * source control.
     * 
     * @param version The name of the version that will be deployed
     * @param ipsProject The project that will be deployed
     * @return The comment that should be used for tagging the source control
     */
    String getTagName(String version, IIpsProject ipsProject);

    /**
     * This method should start the deployment on the selected target system. If this method does
     * not return before deployment is ready, the progress monitor should be used to indicate the
     * progress. When deployment is right, the method returns true, if something is wrong and the
     * user have to change some settings, the method returns false. In this case the messages in the
     * message list are displayed. If the method returns true, the messages are ignored!
     * 
     * @param ipsProject the project to be built
     * @param selectedTargetSystems the selected target systems
     * @param progressMonitor the progress monitor to indicate the progress
     * @return true if the deployment is ok
     */
    boolean buildReleaseAndDeployment(IIpsProject ipsProject,
            String tag,
            List<ITargetSystem> selectedTargetSystems,
            IProgressMonitor progressMonitor);

    /**
     * Setting the observable progress messages to add messages that are displayed to the user
     * 
     * @see ObservableProgressMessages
     * 
     */
    void setObservableProgressMessages(ObservableProgressMessages observableProgressMessages);

}
