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

package org.faktorips.runtime.internal.tableindex;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract implementation for all {@link SearchStructure structures} that map keys to nested
 * {@link SearchStructure structures} much like a map.
 * <p>
 * This class provides a {@link #put(Object, SearchStructure)} method to add nested structures.
 * Adding multiple structures with the same key will result in those structures being merged. This
 * makes setting up nested map structures easy.
 * <p>
 * This abstract implementation makes no assumption about the underlying map implementation. The map
 * instance is provided by subclasses via the constructor. Thus the {@link #get(Object)} method
 * implementation must also be provided by subclasses using the chosen data structure.
 * 
 * @param <K> The type of key in the underlying map - can be restricted in subclasses
 * @param <V> The type of values in the map. Values are nested {@link SearchStructure structures}
 *            which must:
 *            <ul>
 *            <li> have the same result type <code>R</code> as this {@link AbstractMapStructure}
 *            </li> <li> implement the {@link Mergeable} interface. The class definition enforces
 *            the implementation of {@link Mergeable} without restricting the type V to a specific
 *            {@link SearchStructure}. Therefore the mergable type is bound to <code>? super V
 *            </code>. </li>
 *            </ul>
 * 
 * @param <R> The type of the result values. The result type must be the same in every nested
 *            structure.
 * 
 */
public abstract class AbstractMapStructure<K, V extends SearchStructure<R> & Mergeable<? super V>, R> extends
        SearchStructure<R> implements Mergeable<AbstractMapStructure<K, V, R>> {

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
     * Puts a new element in the map. If there is already a value for the given key, the two values
     * will be merged together. Thus existing values will never be overwritten.
     * 
     * @param key key that maps to the specified value
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

    @Override
    public void merge(AbstractMapStructure<K, V, R> otherMap) {
        Map<K, V> otherUnderlyingMap = otherMap.getMap();
        for (Entry<K, V> entry : otherUnderlyingMap.entrySet()) {
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

    /**
     * Checks whether the given result is <code>null</code> and returns a fall-back result (an empty
     * {@link ResultStructure}) in that case.
     * 
     * @param result a valid result or <code>null</code>.
     * @return the given result or an empty {@link ResultStructure} if the given result is
     *         <code>null</code>. Never returns <code>null</code>.
     */
    protected SearchStructure<R> getValidResult(V result) {
        if (result == null) {
            return createEmptyResult();
        } else {
            return result;
        }
    }

    /**
     * Simply creates an empty result structure.
     */
    protected ResultStructure<R> createEmptyResult() {
        return ResultStructure.create();
    }

}