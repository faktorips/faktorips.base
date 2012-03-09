/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.productrelease.ITeamOperations;
import org.faktorips.devtools.core.productrelease.ITeamOperationsFactory;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;

/**
 * Creates {@link CvsTeamOperations} for projects configured with CVS.
 */
public class CvsTeamOperationsFactory implements ITeamOperationsFactory {

    @Override
    public boolean canCreateTeamOperationsFor(IIpsProject ipsProject) {
        return CvsTeamOperations.isCvsProject(ipsProject);
    }

    @Override
    public ITeamOperations createTeamOperations(ObservableProgressMessages observableProgressMessages) {
        return new CvsTeamOperations(observableProgressMessages);
    }

}
