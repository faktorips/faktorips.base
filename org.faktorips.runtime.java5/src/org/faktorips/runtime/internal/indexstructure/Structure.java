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

import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A {@link Structure} a data structure that is used by tables to optimize index key access on their
 * data. The fundamental idea is that a {@link Structure} is a self containing composite, that means
 * every structure contains another structure. Every kind of {@link Structure} is optimized to get
 * the nested structure by any key. Depending on the kind this may be for example a map with direct
 * key access or a tree to access keys in ranges. Finally the last nested element is a
 * {@link ResultStructure} which contains the resulting value.
 * <p>
 * To prevent {@link NullPointerException}s without checking for <code>null</code>, every
 * {@link #get(Object)} with any key will return a {@link Structure}. If there is no nested
 * structure for the given key, an empty {@link ResultStructure} is returned as a fall-back. Calling
 * {@link #get(Object)} on it (with any key) simply returns the empty {@link ResultStructure}
 * itself. IOW an empty {@link ResultStructure} is a kind of null-Object. For example, you have a
 * nested structure Map -> Tree -> Tree and you call <code>get(x).get(y).get(z)</code>. If the first
 * <code>get(x)</code> on the map fails you just get an empty {@link ResultStructure}. Nevertheless
 * the following <code>get(y).get(z)</code> can be called without respecting whether there is a
 * value for <code>x</code> you simply get the empty result.
 * <p>
 * To create a new nested structure, every {@link Structure} that can be nested in other structures
 * have to implement the {@link Mergeable} interface. The aim is that the user could simply create
 * new structures and put them in other existing structures without thinking about duplicated values
 * or keys. The structure just uses {@link Mergeable#merge(Object)} to merge two structures together
 * if necessary.
 * 
 * 
 * @param <R> The type of the resulting values.
 * @see ResultStructure
 * 
 * 
 */
public abstract class Structure<R> {

    /**
     * Getting the nested {@link Structure} for the given key. This method never returns
     * <code>null</code> never mind whether the key exists or not. For non existing keys it simply
     * returns an empty {@link ResultStructure}.
     * 
     * @param key The key for the requested nested {@link Structure}
     * @return The nested {@link Structure} or an empty {@link ResultStructure} if the key does not
     *         exists.
     */
    public abstract Structure<R> get(Object key);

    /**
     * Getting the set of resulting values. If this {@link Structure} is no {@link ResultStructure}
     * this method simply aggregates every nested {@link Structure structures} results.
     * 
     * @return The set of resulting values that are reachable by this {@link Structure}
     */
    public abstract Set<R> get();

    /**
     * Returns the value if there is exactly one value.
     * <p>
     * Use this method if you know there should be exactly one result value. This method throws an
     * {@link AssertionError} if there is no element or more than one element.
     * 
     * @return The one and only result hold by this {@link Structure}.
     * @throws AssertionError if your assertion that there is at most one element is wrong and hence
     *             there are are more than one values.
     * @throws NoSuchElementException If there is no element at all.
     */
    public R getUnique() {
        Set<R> set = get();
        if (set.size() > 1) {
            throw new AssertionError("There are multiple values for a unique key.");
        } else {
            return set.iterator().next();
        }
    }

    /**
     * Returns the value if there is exactly one value or the given defaultValue if there is this
     * structure is empty.
     * <p>
     * Use this method if you know there should be at most one result value. This method throws an
     * {@link AssertionError} if there is more than one element.
     * 
     * @param defaultValue The defaultValue which is returned if this {@link Structure} is empty.
     * @return The result hold by this {@link Structure} or the defaultValue if the structure is
     *         empty.
     * @throws AssertionError if your assertion that there is at most one element is wrong and hence
     *             there are more than one result values.
     */
    public R getUnique(R defaultValue) {
        Set<R> set = get();
        if (set.size() > 1) {
            throw new AssertionError("There are multiple values for a unique key.");
        } else if (set.isEmpty()) {
            return defaultValue;
        } else {
            return set.iterator().next();
        }
    }
}