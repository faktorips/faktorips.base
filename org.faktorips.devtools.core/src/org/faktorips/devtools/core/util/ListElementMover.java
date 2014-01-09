/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.util.List;

import org.faktorips.util.ArgumentCheck;

/**
 * A helper class that moves a given subset of list elements one position up or down in the list.
 */
public class ListElementMover<T> implements IElementMover {

    private final List<T> list;

    public ListElementMover(List<T> list) {
        ArgumentCheck.notNull(list);
        this.list = list;
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
        for (int i = 1; i < list.size(); i++) {
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
        if (contains(indices, list.size() - 1)) {
            return indices;
        }
        int[] newSelection = new int[indices.length];
        int j = 0;
        for (int i = list.size() - 2; i >= 0; i--) {
            if (contains(indices, i)) {
                swapElements(i, i + 1);
                newSelection[j++] = i + 1;
            }
        }
        return newSelection;
    }

    /**
     * Returns <tt>true</tt> if the given indices array contains the given index, otherwise
     * <tt>false</tt>.
     */
    private boolean contains(int[] indices, int index) {
        for (int currentIndex : indices) {
            if (currentIndex == index) {
                return true;
            }
        }
        return false;
    }

    private void swapElements(int index1, int index2) {
        T temp = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, temp);
    }

}
