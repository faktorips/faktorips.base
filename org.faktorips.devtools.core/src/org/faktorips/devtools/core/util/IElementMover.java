/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

/**
 * Provides functionality to move elements inside an array or a collection up or down.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IElementMover {

    /**
     * Moves the elements identified by the given indices one position up or down and returns the
     * new indices of the moved elements.
     * 
     * @param indices The indices array identifying the elements to move.
     * @param up <tt>true</tt> to move up, <tt>false</tt> to move down.
     */
    public int[] move(int[] indices, boolean up);

    /**
     * Moves the elements identified by the given indices one position up and returns the new
     * indices of the elements that are identified by the given indices array.
     * <p>
     * Does not nothing if one of the indices is 0.
     */
    public int[] moveUp(int[] indices);

    /**
     * Moves the elements identified by the given indices one position down and returns the new
     * indices of the elements that are identified by the given indices array.
     * <p>
     * Does not nothing if one of the indices is the last index in the array (length - 1).
     */
    public int[] moveDown(int[] indices);

}
