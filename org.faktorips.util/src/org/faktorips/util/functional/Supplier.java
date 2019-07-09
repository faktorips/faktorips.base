/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.util.functional;

/**
 * Represents a supplier of results.
 *
 * <p>
 * There is no requirement that a new or distinct result be returned each time the supplier is
 * invoked.
 * <p>
 * Will be replaced by java 8 supplier in the future.
 *
 * @param <T> the type of results supplied by this supplier
 *
 * @since 19.7
 */
public interface Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}
