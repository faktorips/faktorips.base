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
 * Visitor for hierarchical model object structures.
 * 
 * @author Jan Ortmann
 */
@FunctionalInterface
public interface IModelObjectVisitor {

    /**
     * Visits the given model object.
     * 
     * @return <code>true</code> if the visitor should continue visiting the object's children,
     *         otherwise <code>false</code>.
     */
    public boolean visit(IModelObject modelObject);

}
