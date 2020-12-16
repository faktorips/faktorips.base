/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Interface indicating that the model object can create a copy of itself.
 * 
 * @author Jan Ortmann
 */
public interface ICopySupport {

    /**
     * Creates and returns new copy of this object.
     */
    public IModelObject newCopy();

}
