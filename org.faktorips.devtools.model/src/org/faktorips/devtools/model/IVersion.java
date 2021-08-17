/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

/**
 * A version identifies a defined state of a component. The format is arbitrary. Special formats
 * like <em>\d+.\d+.\d+</em> as well as simple strings can be used. Any version can be identified by
 * the unique string returned by the method {@link #asString()}. Thus all {@link IVersion} instances
 * created using the same unique identifier are equal.
 * <h2>Comparable</h2> Version objects are comparable. To make clear that only versions of the same
 * implementation are comparable to each other, every implementation must provide the generic type
 * <code>K</code>. The equals() method must be implemented according to the recommendation in
 * {@link Comparable} so that equals returns <code>true</code> if and only if
 * {@link Comparable#compareTo(Object)} returns 0.
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

    /**
     * Returns textual representation of this version without any optional qualifier. The qualifier
     * normally gives a hint for a special sub-version like the current snapshot or CI build. The
     * unqualified version is used for example in documentation that is created during
     * implementation like the since-Tag in java doc.
     * 
     * @return The textual representation of this version without any qualifier
     */
    String getUnqualifiedVersion();

    /**
     * Returns true if this version represents an empty Version (for example 0), false otherwise.
     * 
     * @return true if this version is an empty version
     */
    public boolean isEmptyVersion();

    /**
     * Returns true if this version is not an empty version, false otherwise. This method always
     * returns the negation of {@link #isEmptyVersion()}
     * 
     * @return true if this version is not an empty version, false if this version is an empty
     *         version
     */
    public boolean isNotEmptyVersion();

}
