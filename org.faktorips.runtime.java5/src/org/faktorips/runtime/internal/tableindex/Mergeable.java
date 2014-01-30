/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

/**
 * Defines the ability to merge other objects into this {@link Mergeable mergable}. Used for data
 * structures.
 * <p>
 * An example of the use of this interface are the subclasses of {@link SearchStructure}. They are
 * defined to be mergable with objects of the same class (or at least similar classes). Merging a
 * {@link SearchStructure} deletes no content, instead equal values are being merged recursively.
 * 
 * @param <T> the type of object that can be merged into this object
 * @See {@link AbstractMapStructure}
 */
public interface Mergeable<T> {

    /**
     * Merging adds the contents of the otherMergable to this object. This object then contains the
     * combined content. The other object remains unchanged.
     * 
     * @param otherMergeable The object that should be merged into this object
     */
    public void merge(T otherMergeable);

}