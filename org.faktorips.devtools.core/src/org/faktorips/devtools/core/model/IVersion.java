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
 * A version identifies a defined state of a component. It could have a special format like
 * <em>\d+.\d+.\d+</em> but does not necessary need to. A version have to be represented identically
 * by the string returned by the method {@link #asString()}. Using this String it must be possible
 * to create a new Version of same type that is equal to this version.
 * <p>
 * <h2>Comparable</h2>
 * Two versions need to be comparable to each other. To make clear that only versions of the same
 * implementation are comparable to each other, you need to provide your implementation as generic
 * type <code>K</code> when implementing this interface. It is also necessary that equals is
 * implemented according to the recommendation in {@link Comparable} so that equals return true if
 * and only if {@link Comparable#compareTo(Object)} returns 0.
 * 
 */
public interface IVersion<K extends IVersion<K>> extends Comparable<K> {

    /**
     * Returns the version in a textually representation by a String. This string identifies the
     * version. Using this string you could create a new version that is equal to this one.
     * 
     * @return String represents the specified Version
     */
    String asString();

}
