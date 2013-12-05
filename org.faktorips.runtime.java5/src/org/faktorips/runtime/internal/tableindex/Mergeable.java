/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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