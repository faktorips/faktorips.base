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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation.
 * 
 * @author Jan Ortmann
 */
public class DefaultObjectReferenceStore implements IObjectReferenceStore {

    private Map<Object, List<Object>> objects = new HashMap<>(100);
    private List<IUnresolvedReference> references = new ArrayList<>();

    @Override
    public void resolveReferences() throws Exception {
        for (IUnresolvedReference ref : references) {
            ref.resolve(this);
        }
    }

    @Override
    public void putObject(Object id, Object object) {
        List<Object> list = objects.computeIfAbsent(id, $ -> new ArrayList<>(1));
        if (!list.contains(object)) {
            /*
             * Assumption here is that there won't be too many objects with same id of different
             * classes in the store, so this implementation is fast and there is no need to use a
             * set class.
             */
            list.add(object);
        }
    }

    @Override
    public void addUnresolvedReference(IUnresolvedReference reference) {
        references.add(reference);
    }

    @Override
    public Object getObject(Class<?> clazz, Object id) {
        List<Object> objectsWithId = objects.get(id);
        if (objectsWithId == null) {
            return null;
        }
        for (Object obj : objectsWithId) {
            if (clazz.isAssignableFrom(obj.getClass())) {
                return obj;
            }
        }
        return null;
    }

    @Override
    public Collection<List<Object>> getAllObjects() {
        return objects.values();
    }

    @Override
    public Collection<IUnresolvedReference> getAllUnresolvedReferences() {
        return references;
    }

}
