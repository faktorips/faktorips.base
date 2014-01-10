/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Visitor for hierarchical model object structures.
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectVisitor {

    /**
     * Visits the given model object.
     * 
     * @return <code>true</code> if the visitor should continue visiting the object's children,
     *         otherwise <code>false</code>.
     */
    public boolean visit(IModelObject modelObject);

}
