/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.caching;

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
    public V compute(K key) throws InterruptedException;

    /**
     * Getting the {@link Class} of the value this computable produces.
     */
    public Class<? super V> getValueClass();

}
