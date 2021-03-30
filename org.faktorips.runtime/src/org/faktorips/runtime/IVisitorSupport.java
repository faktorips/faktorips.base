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
 * Marks a model object as accepting visitors.
 * 
 * @author Jan Ortmann
 */
public interface IVisitorSupport {

    /**
     * Accepts the given visitor. This results in a call to the visitor's visit method for this
     * object and, if it returns {@code true}, to all its children recursively.
     * 
     * @param visitor the visitor to accept
     * 
     * @return the result of the visitor's visit method
     * 
     * @see IModelObjectVisitor#visit(IModelObject)
     */
    boolean accept(IModelObjectVisitor visitor);

    /**
     * Returns the given model object if it implements {@link IVisitorSupport} otherwise a
     * {@link GenericVisitorSupport} wrapping it.
     *
     * @since 21.6
     */
    static IVisitorSupport orGenericVisitorSupport(IModelObject modelObject) {
        return modelObject instanceof IVisitorSupport ? (IVisitorSupport)modelObject
                : new GenericVisitorSupport(modelObject);
    }

}
