/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
