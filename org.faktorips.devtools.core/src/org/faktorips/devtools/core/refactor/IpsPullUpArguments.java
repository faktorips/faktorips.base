/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

/**
 * Arguments for Faktor-IPS "Pull Up" refactorings.
 * 
 * @author Alexander Weickmann
 */
public final class IpsPullUpArguments extends RefactoringArguments {

    private final IIpsObjectPartContainer target;

    public IpsPullUpArguments(IIpsObjectPartContainer target) {
        this.target = target;
    }

    public IIpsObjectPartContainer getTarget() {
        return target;
    }

}
