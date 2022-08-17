/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

/**
 * Implementation of the visitor pattern for {@link AResourceDelta resource deltas}.
 */
@FunctionalInterface
public interface AResourceDeltaVisitor {

    /**
     * Visits the given resource delta.
     *
     * @param delta the resource delta to visit
     * @return whether the visitor should continue visiting the delta's children
     */
    boolean visit(AResourceDelta delta);
}
