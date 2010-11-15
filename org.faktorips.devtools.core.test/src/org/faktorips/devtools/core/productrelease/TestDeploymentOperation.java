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

package org.faktorips.devtools.core.productrelease;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.productrelease.DefaultTargetSystem;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class TestDeploymentOperation extends AbstractReleaseAndDeploymentOperation {

    @Override
    public boolean buildReleaseAndDeployment(IIpsProject ipsProject,
            String tag,
            List<ITargetSystem> selectedTargetSystems,
            IProgressMonitor progressMonitor) {
        return true;
    }

    @Override
    public List<ITargetSystem> getAvailableTargetSystems(IIpsProject ipsProject) {
        ArrayList<ITargetSystem> result = new ArrayList<ITargetSystem>();
        result.add(new DefaultTargetSystem("System A"));
        result.add(new DefaultTargetSystem("System B"));
        result.add(new DefaultTargetSystem("System C"));
        result.add(new DefaultTargetSystem("System D"));
        return result;
    }

}
