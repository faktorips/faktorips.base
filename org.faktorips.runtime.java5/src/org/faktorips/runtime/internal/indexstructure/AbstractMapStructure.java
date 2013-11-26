/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal.indexstructure;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract implementation for all {@link Structure structures} that uses any kind of {@link Map} as
 * data structure. It expects that every nested {@link Structure} implements the {@link Mergeable}
 * interface to be able to merge two structures with the same key.
 * <p>
 * This abstract implementation makes no assumption about the underlying map implementation. The map
 * instance is provided by the subclasses via the constructor. Because the implementation of
 * {@link #get(Object)} depends on the underlying data structure it is not implemented in this
 * abstract class and needs to be implemented in subclasses.
 * 
 * @param <K> The type of the key in the underlying map - could be restricted in subclasses
 * @param <V> The type of the values in the map. First we need to restrict that only
 *            {@link Structure} elements with the same result type <code>R</code> are allowed.
 *            Second we need to restrict that only implementations of {@link Mergeable} are allowed.
 *            Because we want to implement the {@link Mergeable} interface in this class but the
 *            generic type <code>V</code> will be of subclasses of {@link AbstractMapStructure} we
 *            need to allow that <code>V</code> is an implementation of any {@link Mergeable} that
 *            is implemented in a superclass. (I tried to describe it as easy as possible and do not
 *            want to go in further details)
 * @param <R> The type of the resulting values This type need to be the same in every nested
 *            structure.
 * 
 */
public abstract class AbstractMapStructure<K, V extends Structure<R> & Mergeable<? super V>, R> extends Structure<R>
        implements Mergeable<AbstractMapStructure<K, V, R>> {

    private final Map<K, V> map;

    /**
     * Creates a new {@link AbstractMapStructure} with the specified map instance.
     * 
     * @param map The map that should be used as underlying data structure.
     */
    protected AbstractMapStructure(Map<K, V> map) {
        this.map = map;
    }

    /**
     * Put a new element in the map. If there is already a value for the given key, the two values
     * will be merged together. Hence you do not have to worry about overwriting existing values.
     * 
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @see Map#put(Object, Object)
     * @see #merge(AbstractMapStructure)
     */
    public void put(K key, V value) {
        if (getMap().containsKey(key)) {
            V myValue = getMap().get(key);
            myValue.merge(value);
        } else {
            getMap().put(key, value);
        }
    }

    public void merge(AbstractMapStructure<K, V, R> otherMap) {
        Map<K, V> otherTreeMap = otherMap.getMap();
        for (Entry<K, V> entry : otherTreeMap.entrySet()) {
            if (getMap().containsKey(entry.getKey())) {
                V myValue = getMap().get(entry.getKey());
                V otherValue = entry.getValue();
                myValue.merge(otherValue);
            } else {
                getMap().put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public Set<R> get() {
        HashSet<R> resultSet = new HashSet<R>();
        for (V value : getMap().values()) {
            Set<R> set = value.get();
            resultSet.addAll(set);
        }
        return resultSet;
    }

    protected Map<K, V> getMap() {
        return map;
    }

}