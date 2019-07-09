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
 * Represents a supplier of {@code boolean}-valued results. This is the {@code boolean}-producing
 * primitive specialisation of {@link Supplier}.
 *
 * <p>
 * There is no requirement that a new or distinct result be returned each time the supplier is
 * invoked.
 * <p>
 * Will be replaced by java 8 supplier in the future.
 *
 * @see Supplier
 * @since 19.7
 */
public interface BooleanSupplier {

    /**
     * Gets a result.
     *
     * @return a result
     */
    boolean getAsBoolean();
}
