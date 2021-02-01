/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import org.faktorips.util.ArgumentCheck;

/**
 * A helper class that moves a given subset of array elements one position up or down inside the
 * array.
 */
public class ArrayElementMover implements IElementMover {

    private Object[] array;

    public ArrayElementMover(Object[] array) {
        ArgumentCheck.notNull(array);
        this.array = array;
    }

    @Override
    public int[] move(int[] indices, boolean up) {
        if (up) {
            return moveUp(indices);
        } else {
            return moveDown(indices);
        }
    }

    @Override
    public int[] moveUp(int[] indices) {
        if (contains(indices, 0)) {
            return indices;
        }
        int[] newSelection = new int[indices.length];
        int j = 0;
        for (int i = 1; i < array.length; i++) {
            if (contains(indices, i)) {
                swapElements(i - 1, i);
                newSelection[j] = i - 1;
                j++;
            }
        }
        return newSelection;
    }

    @Override
    public int[] moveDown(int[] indices) {
        if (contains(indices, array.length - 1)) {
            return indices;
        }
        int[] newSelection = new int[indices.length];
        int j = 0;
        for (int i = array.length - 2; i >= 0; i--) {
            if (contains(indices, i)) {
                swapElements(i, i + 1);
                newSelection[j++] = i + 1;
            }
        }
        return newSelection;
    }

    /**
     * Returns <code>true</code> if the given indices array contains the given index, otherwise
     * <code>false</code>.
     */
    private boolean contains(int[] indices, int index) {
        for (int indice : indices) {
            if (indice == index) {
                return true;
            }
        }
        return false;
    }

    private void swapElements(int index1, int index2) {
        Object temp = array[index1];
        array[index1] = array[index2];
        array[index2] = temp;
    }

}
