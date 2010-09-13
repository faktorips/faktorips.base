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
import org.faktorips.util.message.MessageList;

public class TestDeploymentOperation implements IDeploymentOperation {

    @Override
    public boolean buildReleaseAndDeployment(List<String> selectedTargetSystems,
            IProgressMonitor progressMonitor,
            MessageList messageList) {
        return true;
    }

    @Override
    public List<String> getAvailableTargetSystems() {
        ArrayList<String> result = new ArrayList<String>();
        result.add("System A");
        result.add("System B");
        result.add("System C");
        result.add("System D");
        return result;
    }

}
