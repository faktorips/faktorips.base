/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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

    private Map<Object, List<Object>> objects = new HashMap<Object, List<Object>>(100);
    private List<IUnresolvedReference> references = new ArrayList<IUnresolvedReference>();

    public void resolveReferences() throws Exception {
        for (IUnresolvedReference ref : references) {
            ref.resolve(this);
        }
    }

    public void putObject(Object id, Object object) {
        List<Object> list = objects.get(id);
        if (list == null) {
            list = new ArrayList<Object>(1);
            objects.put(id, list);
        }
        if (!list.contains(object)) {
            /*
             * Assumption here is that there won't be too many objects with same id of different
             * classes in the store, so this implementation is fast and there is no need to use a
             * set class.
             */
            list.add(object);
        }
    }

    public void addUnresolvedReference(IUnresolvedReference reference) {
        references.add(reference);
    }

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

    public Collection<List<Object>> getAllObjects() {
        return objects.values();
    }

    public Collection<IUnresolvedReference> getAllUnresolvedReferences() {
        return references;
    }

}
