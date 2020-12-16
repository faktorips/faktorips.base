/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.util.function;

/**
 * Represents a predicate (boolean-valued function) of one argument.
 *
 * <p>
 * This is a functional interface whose functional method is {@link #test(Object)}.
 * 
 * <p>
 * <em>Note that this is a simple copy of Java 8's java.util.function.Predicate to start implementing a more functional style for some of Faktor-IPS' API and will probably be removed once we move the code base to Java 8.</em>
 *
 * @param <T> the type of the input to the predicate
 */
public interface IPredicate<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean test(T t);

}
