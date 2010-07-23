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

import java.util.List;

import org.faktorips.util.ArgumentCheck;

/**
 * A helper class that moves a given subset of list elements one position up or down in the list.
 */
public class ListElementMover<T> extends ElementMover {

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

    @Override
    protected void swapElements(int index1, int index2) {
        T temp = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, temp);
    }

}
