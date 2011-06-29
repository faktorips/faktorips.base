/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private final Map<Class<? extends T>, List<? extends T>> internalMap = new ConcurrentHashMap<Class<? extends T>, List<? extends T>>();

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
     * Putting the value to the classes of key. Calling this method multiple times with the same key
     * value pair would add a new value every time. That means you could have the same instance
     * multiple time.
     * 
     * @param key The key specifying the class of the value
     * @param value the value you want to add
     * @return the list of all values already added to the map for the given key including the new
     *         one.
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
        return valuesInternal().size();
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
        return valuesInternal().isEmpty();
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
        List<K> list = getInstanceList(key);
        List<K> result = new ArrayList<K>(list);
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
     * This method returns all values in one collection but does not guaranty any order of the
     * values.
     * 
     * @return All values stored in this map in one collection.
     */
    private List<T> valuesInternal() {
        ArrayList<T> result = new ArrayList<T>();
        for (List<? extends T> list : internalMap.values()) {
            result.addAll(list);
        }
        return result;
    }

    /**
     * This method returns all values in one collection. The order of the lists of different classes
     * is always the same.
     * 
     * @return All values stored in this map in one collection.
     */
    public List<T> values() {
        ArrayList<T> result = new ArrayList<T>();
        Set<Class<? extends T>> keySet = internalMap.keySet();
        ArrayList<Class<? extends T>> sortedKeySet = new ArrayList<Class<? extends T>>(keySet);
        Collections.sort(sortedKeySet, new Comparator<Class<? extends T>>() {

            public int compare(Class<? extends T> o1, Class<? extends T> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (List<? extends T> list : internalMap.values()) {
            result.addAll(list);
        }
        return result;
    }

    /**
     * Getting the list that is stored for the given key. This method does create the list if it is
     * absent. Before creating the list, the double checking ideom is implemented to avoid
     * concurrency problems. The double checking ideom works because we use a
     * {@link ConcurrentHashMap}.
     */
    private <K extends T> List<K> getInstanceList(Class<K> key) {
        List<? extends T> list = internalMap.get(key);
        if (list == null) {
            synchronized (internalMap) {
                list = internalMap.get(key);
                if (list == null) {
                    list = new ArrayList<T>();
                    internalMap.put(key, list);
                }
            }
        }
        @SuppressWarnings("unchecked")
        List<K> castedList = (List<K>)list;
        return castedList;
    }

}
