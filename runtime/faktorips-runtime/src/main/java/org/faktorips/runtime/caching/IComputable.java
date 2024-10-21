/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.caching;

import java.util.function.Function;

/**
 * Interface to compute objects of type V identified by a key of type K
 * 
 * @author dirmeier
 */
public interface IComputable<K, V> {

    /**
     * Compute an object of type V identified by the key of type K
     * 
     * @param key the key to identify the object
     * @return the computed Object of type V
     * @throws InterruptedException When computation was interrupted
     */
    V compute(K key) throws InterruptedException;

    /**
     * Getting the {@link Class} of the value this computable produces.
     */
    Class<? super V> getValueClass();

    /**
     * Creates a new {@link IComputable} for the given value class using the given {@link Function}
     * to compute the values from keys.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param valueClass the class of the values
     * @param function the function to compute a value from a key
     * @return a new {@link IComputable}
     */
    static <K, V> IComputable<K, V> of(Class<? super V> valueClass, Function<K, V> function) {
        return new AbstractComputable<>(valueClass) {

            @Override
            public V compute(K key) throws InterruptedException {
                return function.apply(key);
            }
        };
    }

}
