/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.util;

/**
 * Common base class for utility classes providing functionality to move elements inside an array or
 * inside a collection.
 * 
 * @author Alexander Weickmann
 */
abstract class ElementMover {

    /**
     * Moves the elements identified by the given indices one position up or down and returns the
     * new indices of the moved elements.
     * 
     * @param indices The indices array identifying the elements to move.
     * @param up <tt>true</tt> to move up, <tt>false</tt> to move down.
     */
    public abstract int[] move(int[] indices, boolean up);

    /**
     * Moves the elements identified by the given indices one position up and returns the new
     * indices of the elements that are identified by the given indices array.
     * <p>
     * Does not nothing if one of the indices is 0.
     */
    public abstract int[] moveUp(int[] indices);

    /**
     * Moves the elements identified by the given indices one position down and returns the new
     * indices of the elements that are identified by the given indices array.
     * <p>
     * Does not nothing if one of the indices is the last index in the array (length - 1).
     */
    public abstract int[] moveDown(int[] indices);

    /**
     * Swaps the elements identified by the given indices.
     */
    protected abstract void swapElements(int index1, int index2);

}
