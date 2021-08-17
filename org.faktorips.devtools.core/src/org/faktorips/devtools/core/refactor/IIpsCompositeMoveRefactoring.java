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

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * Allows to batch "Move" refactor multiple {@link IIpsObject}s.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsCompositeMoveRefactoring extends IIpsCompositeRefactoring {

    /**
     * Sets the target {@link IIpsPackageFragment}.
     * 
     * @param targetIpsPackageFragment The target {@link IIpsPackageFragment}
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setTargetIpsPackageFragment(IIpsPackageFragment targetIpsPackageFragment);

    /**
     * Returns the target {@link IIpsPackageFragment}.
     */
    public IIpsPackageFragment getTargetIpsPackageFragment();

}
