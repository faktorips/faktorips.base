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
 * Visitor to visit a model object delta.
 * 
 * @author Jan Ortmann
 */
@FunctionalInterface
public interface IModelObjectDeltaVisitor {

    /**
     * Visits the given model object delta.
     * 
     * @param delta The delta to visit
     * 
     * @return <code>true</code> if the delta's children should be visited; <code>false</code> if
     *             they should be skipped.
     */
    boolean visit(IModelObjectDelta delta);

}
