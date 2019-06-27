/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

/**
 * Marks an model object as accepting visitors.
 * 
 * @author Jan Ortmann
 */
public interface IVisitorSupport {

    /**
     * Accepts the given visitor. This results in a call of the visitor's visit method for this
     * object and all its children.
     * 
     * @param visitor The visitor to accept.
     * 
     * @return The result of the visitor's visit method.
     * 
     * @see IModelObjectVisitor#visit(IModelObject)
     */
    public boolean accept(IModelObjectVisitor visitor);

}
