/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a map with a class as key and a collection of instances of this class as value. The
 * class does NOT implements the {@link Map} interface because this interface does not set the
 * correct generics. But this class does implements the same methods like {@link Map} except of the
 * generics.
 * <p>
 * The generic Type T represents the base class used in this map. All keys have to be a subclass or
 * the same class.
 * <p>
 * Remember that this map does only handles a bunch of list with the same superclass. Changing any
 * of these lists does also change the list in the map. Only the method {@link #values()} does
 * create a new List containing every object of the values. All other methods never create list
 * copies.
 * 
 * @author dirmeier
 */
public class ClassToInstancesMap<T> {

    private final ConcurrentHashMap<Class<? extends T>, List<? extends T>> internalMap = new ConcurrentHashMap<>();

    /**
     * Getting the list of instances stored of the type given by the key. If there is no element for
     * this key this method would return an empty list. Never return null.
     * 
     * @param key The class of the instances you want to get
     * @return A list of instances of the given class
     */
    public <K extends T> List<K> get(Class<K> key) {
        return getInstanceList(key);
    }

    /**
     * Putting the value to the classes of its type. Use this method only if you want to put the
     * instance to exactly its implementation class used as key. Use {@link #put(Class, Object)} if
     * you want to store the instance for example with its interface used as the key. Calling this
     * method multiple times with the same key value pair would add a new value every time. That
     * means you could have the same instance multiple time.
     * 
     * @param value the value you want to add
     * @return the list of all values already added to the map for the given key including the new
     *             one.
     */
    public <K extends T> List<K> put(K value) {
        @SuppressWarnings("unchecked")
        // this is exactly this class
        Class<K> implClass = (Class<K>)value.getClass();
        return put(implClass, value);
    }

    /**
     * Putting the value to the classes of key. Calling this method multiple times with the same key
     * value pair would add a new value every time. That means you could have the same instance
     * multiple time.
     * 
     * @param key The key specifying the class of the value
     * @param value the value you want to add
     * @return the list of all values already added to the map for the given key including the new
     *             one.
     */
    public <K extends T> List<K> put(Class<K> key, K value) {
        List<K> list = getInstanceList(key);
        list.add(value);
        return list;
    }

    /**
     * This method puts the value into the list specified by the key class. Use this method only if
     * you do not know the concrete class at compile time, use {@link #put(Class, Object)} instead.
     * If you do not have the information about key and value you could use this put method. The
     * type check is done at runtime, this method would throw a {@link RuntimeException} in case of
     * type mismatch.
     * 
     * @param key The class that identifies the list of values
     * @param value the value you want to add to this map
     * @return the list of values of type key after adding the value
     */
    public List<T> putWithRuntimeCheck(Class<? extends T> key, T value) {
        if (key.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            List<T> list = (List<T>)getInstanceList(key);
            list.add(value);
            return list;
        } else {
            throw new RuntimeException("The value " + value + " is not of type " + key);
        }
    }

    /**
     * The whole size of the map that means how many instances are stored in this map. If you want
     * to know how many objects are stored of one type use {@link #size(Class)}.
     * 
     * @return The number of discrete values stored in this map.
     */
    public int size() {
        return internalMap.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Getting the number of objects stored for the specified key.
     * 
     * @param key The class you want to get the number of instances for
     * @return the number of values stored for the specified class.
     */
    public <K extends T> int size(Class<K> key) {
        return getInstanceList(key).size();
    }

    /**
     * Return true if this map is empty
     * 
     * @return true if the map is empty or false if there is at least one element.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Check if the map contains the value. This method running over all value lists searching for
     * the specified value.
     * 
     * @param value The value you want to know if it is contained
     * @return true if the value is in the list or false if not
     */
    public boolean containsValue(Object value) {
        for (List<? extends T> list : internalMap.values()) {
            if (list.contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the map contains a list of values for the specified type.
     * 
     * @param type The type for which we check if there is a list of values for
     * @return true if there is a list of values of the specified type
     */
    public boolean containsValuesOf(Class<? extends T> type) {
        return internalMap.get(type) != null;
    }

    /**
     * Remove a single element from the list and returns true if it was removed successfully. If the
     * list containing the object multiple times only the fist one will be removed!
     * 
     * @param key The key of the list that should contain the object
     * @param object the object you want to remove
     * @return true if the object was found in the list.
     */
    public boolean remove(Class<? extends T> key, T object) {
        List<? extends T> instanceList = getInstanceList(key);
        return instanceList.remove(object);
    }

    /**
     * Remove all objects for a given key and returns the list of removed objects. The returned list
     * is a copy of the original included list, changing this list does not matter fot this map.
     * 
     * @param key The class of the objects you want to remove
     * @return the list of removed objects
     */
    public synchronized <K extends T> List<K> removeAll(Class<K> key) {
        var list = getInstanceList(key);
        var result = new ArrayList<>(list);
        list.clear();
        return result;
    }

    /**
     * Remove every object in the map.
     */
    public void clear() {
        internalMap.clear();
    }

    /**
     * This method returns all values in one collection. The order of the lists of different classes
     * is always the same.
     * 
     * @return All values stored in this map in one collection.
     */
    public List<T> values() {
        return internalMap.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getName()))
                .map(Entry::getValue)
                .flatMap(List::stream)
                .map(t -> (T)t)
                .toList();
    }

    /**
     * Gets the set of list of instances stored for the given key. Creates a new list if absent.
     * <p>
     * Returns the very list instance that is stored in the map for the given key (not a defensive
     * copy). Thus adding values to that list changes this map.
     * <p>
     * This method is thread safe. Ensures that only a single list instance is created for each key,
     * and that multiple threads will share these instances (and not overwrite each other's values).
     * 
     * @param key The class to return a list of instances for.
     * 
     * @type K a sub class of the key
     */
    @SuppressWarnings("unchecked")
    private <K extends T> List<K> getInstanceList(Class<K> key) {
        return (List<K>)internalMap.computeIfAbsent(key, $ -> new ArrayList<K>());
    }

}
