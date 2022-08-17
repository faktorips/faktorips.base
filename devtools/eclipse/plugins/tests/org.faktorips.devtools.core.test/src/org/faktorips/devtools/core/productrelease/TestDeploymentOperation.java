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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.productrelease.DefaultTargetSystem;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productrelease.ITargetSystem;

public class TestDeploymentOperation extends AbstractReleaseAndDeploymentOperation {

    @Override
    public boolean buildReleaseAndDeployment(IIpsProject ipsProject,
            String tag,
            List<ITargetSystem> selectedTargetSystems,
            IProgressMonitor progressMonitor) {
        getObservableProgressMessages().info("Test Deployment Operation");
        getObservableProgressMessages().warning("Nothing happens");
        return true;
    }

    @Override
    public List<ITargetSystem> getAvailableTargetSystems(IIpsProject ipsProject) {
        ArrayList<ITargetSystem> result = new ArrayList<>();
        result.add(new DefaultTargetSystem("System A") {

            @Override
            public boolean isValidAuthentication() {
                return getPasswordAuthentication() != null;
            }
        });
        result.add(new DefaultTargetSystem("System B"));
        result.add(new DefaultTargetSystem("System C"));
        result.add(new DefaultTargetSystem("System D"));
        return result;
    }

}
