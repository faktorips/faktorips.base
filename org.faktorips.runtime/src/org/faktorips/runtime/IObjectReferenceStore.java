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

    public void putObject(Object id, Object entityObject);

    public void addUnresolvedReference(IUnresolvedReference reference);

    public Object getObject(Class<?> clazz, Object id);

    /**
     * Returns a list containing all objects registered.
     */
    public Collection<List<Object>> getAllObjects();

    public Collection<IUnresolvedReference> getAllUnresolvedReferences();

    public void resolveReferences() throws Exception;

}
