/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * 
 * The Version states the release when a specific change was introduced to a specific
 * {@link IIpsObjectPartContainer}. The user can decide what form the version should have.
 */
public interface IVersion {

    /**
     * Returns the version in a textually representation by a String.
     * 
     * @return String containing the specified Version
     */
    public String getVersionAsString();

}
