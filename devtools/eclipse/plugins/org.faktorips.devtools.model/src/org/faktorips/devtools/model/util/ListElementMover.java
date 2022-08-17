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

import java.util.ArrayList;
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
     * Returns <code>true</code> if the given indices array contains the given index, otherwise
     * <code>false</code>.
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

    public void moveToIndex(int[] selectedindices, int targetIndex, boolean insertBelow) {
        if (targetIndex < 0 || targetIndex > list.size()) {
            return;
        }
        List<T> buffer = new ArrayList<>();
        int delta = insertBelow ? 1 : 0;
        for (int selectedInd : selectedindices) {
            buffer.add(list.get(selectedInd));
            if (selectedInd < targetIndex) {
                delta--;
            }
        }
        list.removeAll(buffer);
        list.addAll(targetIndex + delta, buffer);
    }

}
