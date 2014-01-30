/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.productrelease;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public abstract class AbstractReleaseAndDeploymentOperation implements IReleaseAndDeploymentOperation {

    private ObservableProgressMessages observableProgressMessages;

    @Override
    public List<ITargetSystem> getAvailableTargetSystems(IIpsProject ipsProject) {
        return new ArrayList<ITargetSystem>();
    }

    @Override
    public List<IFile> additionalResourcesToCommit(IIpsProject ipsProject) {
        return new ArrayList<IFile>();
    }

    @Override
    @Deprecated
    public boolean customReleaseSettings(IIpsProject ipsProject, IProgressMonitor progressMonitor) {
        return true;
    }

    @Override
    public boolean preCommit(IIpsProject ipsProject, IProgressMonitor progressMonitor) {
        return customReleaseSettings(ipsProject, progressMonitor);
    }

    @Override
    public void setObservableProgressMessages(ObservableProgressMessages observableProgressMessages) {
        this.observableProgressMessages = observableProgressMessages;
    }

    /**
     * @return Returns the observableProgressMessages.
     */
    public ObservableProgressMessages getObservableProgressMessages() {
        return observableProgressMessages;
    }

}
