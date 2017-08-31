/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.util.functional;

/**
 * Represents an operation that accepts a single input argument and returns no result. Unlike most
 * other functional interfaces, {@code Consumer} is expected to operate via side-effects.
 * <p>
 * Will be replaced by java 8 consumer in the future.
 * 
 */
public interface Consumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);

}
