/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple cache that just uses a HashMap to cache objects and never releases them.
 * 
 * @author Jan Ortmann
 */
public class SimpleCache implements ICache {

    private Map<Object, Object> objects;

    /**
     * 
     */
    public SimpleCache(int initialCapacity) {
        objects = new HashMap<Object, Object>(initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        objects.clear();
    }

    /**
     * {@inheritDoc}
     */
    public Object getObject(Object key) {
        return objects.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public void put(Object key, Object o) {
        objects.put(key, o);
    }

}
