/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

/**
 * Defines the ability to merge or copy other objects into this {@link MergeAndCopyStructure}. Used
 * for data structures.
 * <p>
 * An example of the use of this interface are the subclasses of {@link SearchStructure}. They are
 * defined to be mergeable with objects of the same class (or at least similar classes).
 * 
 * @param <T> the type of object that can be copied or merged into this object
 */
public interface MergeAndCopyStructure<T extends MergeAndCopyStructure<T>> {

    /**
     * Merging adds the contents of the otherMergable to this object. This object then contains the
     * combined content. The other object remains unchanged.Merging a {@link SearchStructure}
     * deletes no content, instead equal values are being merged recursively.
     * 
     * @param otherMergeable The object that should be merged into this object
     */
    public void merge(T otherMergeable);

    /**
     * This method copies the object which calls it. The newly created object has the same
     * properties like the object calling {@link #copy()}.
     * 
     * @return T that is a copy of the calling object
     */
    public T copy();

}