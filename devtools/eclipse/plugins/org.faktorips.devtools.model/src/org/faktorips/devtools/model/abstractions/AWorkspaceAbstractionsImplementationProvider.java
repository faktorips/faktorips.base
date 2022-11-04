/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstractions;

import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;

public interface AWorkspaceAbstractionsImplementationProvider {
    AWorkspaceAbstractionsImplementation getWorkspaceAbstractionsImplementation();

    boolean canRun();

    int getPriority();
}
