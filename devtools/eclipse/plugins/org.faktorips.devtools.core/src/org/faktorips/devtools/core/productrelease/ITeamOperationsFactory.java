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

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productrelease.ObservableProgressMessages;

/**
 * Implementations create {@link ITeamOperations} for {@link IIpsProject IIpsProjects} configured
 * for specific version control systems.
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
