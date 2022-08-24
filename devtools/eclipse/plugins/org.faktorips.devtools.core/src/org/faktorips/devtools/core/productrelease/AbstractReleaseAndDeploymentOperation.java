/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.productrelease;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.model.eclipse.productrelease.IReleaseAndDeploymentOperation;
import org.faktorips.devtools.model.eclipse.productrelease.ITargetSystem;
import org.faktorips.devtools.model.eclipse.productrelease.ObservableProgressMessages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public abstract class AbstractReleaseAndDeploymentOperation implements IReleaseAndDeploymentOperation {

    private ObservableProgressMessages observableProgressMessages;

    @Override
    public List<ITargetSystem> getAvailableTargetSystems(IIpsProject ipsProject) {
        return new ArrayList<>();
    }

    @Override
    public List<IFile> additionalResourcesToCommit(IIpsProject ipsProject) {
        return new ArrayList<>();
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
    public String getTagName(String version, IIpsProject ipsProject) {
        String tag = version;
        if (tag.matches("[0-9].*")) { //$NON-NLS-1$
            // tag must start with a letter
            tag = "v" + tag; //$NON-NLS-1$
        }
        return tag.replaceAll("[\\$,\\.:;@]", "_");
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
