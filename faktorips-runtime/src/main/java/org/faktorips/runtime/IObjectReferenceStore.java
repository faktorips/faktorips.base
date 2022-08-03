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

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Jan Ortmann
 */
public interface IObjectReferenceStore {

    void putObject(Object id, Object entityObject);

    void addUnresolvedReference(IUnresolvedReference reference);

    Object getObject(Class<?> clazz, Object id);

    /**
     * Returns a list containing all objects registered.
     */
    Collection<List<Object>> getAllObjects();

    Collection<IUnresolvedReference> getAllUnresolvedReferences();

    void resolveReferences() throws Exception;

}
