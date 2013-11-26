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

package org.faktorips.runtime.internal.indexstructure;

/**
 * This interface imposes the ability to merge another object into this object. It is used by data
 * structures which have any kind of content. After the other object is merged into this object, the
 * whole content of the the other object have to be accessible by this object.
 * 
 * @param <T> the type of objects that could be merged in this object
 */
public interface Mergeable<T> {

    /**
     * Merges the other object into this object. After this method the whole content of the other
     * object is accessible by this object and no content of this object is deleted.
     * <p>
     * For example if this is a map data structure and you want to merge another map into this one.
     * For every key you have to look in this map if it already exists. If not, just add the new key
     * value pair. If it already exists you need to merge the values, that means for example if the
     * value type is a set you add all values from the other map to the values of this map.
     * 
     * @param otherMergeable The object that should be merged into this object
     */
    public void merge(T otherMergeable);

}