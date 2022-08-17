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
 * Interface that defines a model object as dependent object, i.e. the object is part of another
 * object.
 * 
 * @author Jan Ortmann
 */
public interface IDependantObject {

    /**
     * Returns the parent this object belongs to. Returns <code>null</code> if this object is
     * (currently) not a part of another.
     */
    IModelObject getParentModelObject();

}
