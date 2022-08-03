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
 * Interface indicating that it is possible to compute a delta between two instances of the class
 * implementing this interface.
 * 
 * @author Jan Ortmann
 */
public interface IDeltaSupport {

    /**
     * Computes a delta between this object and the given other object.
     * 
     * @param otherObject The object this one is compared too.
     * 
     * @throws ClassCastException if otherObject is not an instance of the same class as 'this'.
     * @throws NullPointerException if otherObject is <code>null</code>.
     */
    IModelObjectDelta computeDelta(IModelObject otherObject, IDeltaComputationOptions options);

}
