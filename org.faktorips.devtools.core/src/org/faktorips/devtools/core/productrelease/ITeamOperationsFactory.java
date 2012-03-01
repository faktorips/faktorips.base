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

package org.faktorips.devtools.core.productrelease;

import org.faktorips.devtools.core.internal.productrelease.CvsTeamOperations;
import org.faktorips.devtools.core.internal.productrelease.CvsTeamOperationsFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Implementations create {@link ITeamOperations} for {@link IIpsProject IIpsProjects} configured
 * for specific version control systems. For example the {@link CvsTeamOperationsFactory} creates a
 * {@link CvsTeamOperations} for projects using CVS.
 */
public interface ITeamOperationsFactory {

    /**
     * Returns whether this factory can create {@link ITeamOperations} that handle the version
     * control system used in the given project.
     */
    boolean canCreateTeamOperationsFor(IIpsProject ipsProject);

    /**
     * Returns {@link ITeamOperations} using the given {@link ObservableProgressMessages}.
     */
    ITeamOperations createTeamOperations(ObservableProgressMessages observableProgressMessages);
}
