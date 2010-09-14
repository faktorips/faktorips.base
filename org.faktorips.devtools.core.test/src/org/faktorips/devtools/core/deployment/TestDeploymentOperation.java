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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.deployment.TargetSystem;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

public class TestDeploymentOperation implements IDeploymentOperation {

    @Override
    public boolean buildReleaseAndDeployment(IIpsProject ipsProject,
            List<ITargetSystem> selectedTargetSystems,
            IProgressMonitor progressMonitor,
            MessageList messageList) {
        return true;
    }

    @Override
    public List<ITargetSystem> getAvailableTargetSystems() {
        ArrayList<ITargetSystem> result = new ArrayList<ITargetSystem>();
        result.add(new TargetSystem("System A"));
        result.add(new TargetSystem("System B"));
        result.add(new TargetSystem("System C"));
        result.add(new TargetSystem("System D"));
        return result;
    }

}
