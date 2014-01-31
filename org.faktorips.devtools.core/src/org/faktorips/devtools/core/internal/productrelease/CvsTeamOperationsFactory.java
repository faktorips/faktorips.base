/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
