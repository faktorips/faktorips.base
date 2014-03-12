/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model;

/**
 * A version identifies a defined state of a component. The format is arbitrary. Special formats
 * like <em>\d+.\d+.\d+</em> as well as simple strings can be used. Any version can be identified by
 * the unique string returned by the method {@link #asString()}. Thus all {@link IVersion} instances
 * created using the same unique identifier are equal.
 * <p>
 * <h2>Comparable</h2>
 * Version objects are comparable. To make clear that only versions of the same implementation are
 * comparable to each other, every implementation must provide the generic type <code>K</code>. The
 * equals() method must be implemented according to the recommendation in {@link Comparable} so that
 * equals returns <code>true</code> if and only if {@link Comparable#compareTo(Object)} returns 0.
 * 
 */
public interface IVersion<K extends IVersion<K>> extends Comparable<K> {

    /**
     * Returns the textual representation of this version. {@link IVersion} instances created using
     * the same unique identifier are equal.
     * 
     * @return String represents the specified Version
     */
    String asString();

}
