/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.productrelease;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This interface is implemented for a release deployment extension. The implementation is
 * referenced by the extension <i>releaseDeploymentExtension</i> in the attribute <i>operation</i>.
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
    public List<ITargetSystem> getAvailableTargetSystems(IIpsProject ipsProject);

    /**
     * Returns a list of resources that have to be commited when the release operation commits the
     * .ipsproject file.
     * 
     * @param ipsProject the selected {@link IIpsProject}
     * 
     * @return List of files to commit
     */
    public List<IFile> additionalResourcesToCommit(IIpsProject ipsProject);

    /**
     * This method is called by the release build processor just before commiting changed files. The
     * new version is already set in the .ipsproject file and the project have been clean built.
     * 
     * @param ipsProject the project to release
     * @param progressMonitor a {@link IProgressMonitor} to view sate of work
     * @return true when everything was right, false to stop the release process
     */
    public boolean customReleaseSettings(IIpsProject ipsProject, IProgressMonitor progressMonitor);

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
    public boolean buildReleaseAndDeployment(IIpsProject ipsProject,
            String tag,
            List<ITargetSystem> selectedTargetSystems,
            IProgressMonitor progressMonitor);

    /**
     * Setting the observable progress messages to add messages that are displayed to the user
     * 
     * @see ObservableProgressMessages
     * 
     */
    public void setObservableProgressMessages(ObservableProgressMessages observableProgressMessages);

}
