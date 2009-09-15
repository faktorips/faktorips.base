/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.util;

import java.util.List;

import org.faktorips.util.ArgumentCheck;

/**
 * A helper class that moves a given subset of an list elements one position up or down.
 */
public class ListElementMover {

    private List list;

    public ListElementMover(List list) {
        ArgumentCheck.notNull(list);
        this.list = list;
    }

    /**
     * Moves the elements identifies by the given indices one position up or down.
     * 
     * @param indexes The indices array identifying the elemtents to move.
     * @param up True to move up, false to move down.
     * 
     * @return The new indexes of the moves elements.
     */
    public int[] move(int[] indexes, boolean up) {
        if (up) {
            return moveUp(indexes);
        } else {
            return moveDown(indexes);
        }
    }

    /**
     * Moves the elements identifies by the given indices one position up in the list this mover is
     * constructed for. Does not nothing if one of the indexes is 0.
     * 
     * @return the new indexes of the elements that are identified by the given indexes array.
     */
    public int[] moveUp(int[] indexes) {
        if (contains(indexes, 0)) {
            return indexes;
        }
        int[] newSelection = new int[indexes.length];
        int j = 0;
        for (int i = 1; i < list.size(); i++) {
            if (contains(indexes, i)) {
                swapElements(i - 1, i);
                newSelection[j] = i - 1;
                j++;
            }
        }
        return newSelection;
    }

    /**
     * Moves the elements identifies by the given indices one position down in the array this mover
     * is constructed for. Does not nothing if one of the indexes is the last index in the array
     * (length-1).
     * 
     * @return the new indexes of the elements that are identified by the given indexes array.
     */
    public int[] moveDown(int[] indexes) {
        if (contains(indexes, list.size() - 1)) {
            return indexes;
        }
        int[] newSelection = new int[indexes.length];
        int j = 0;
        for (int i = list.size() - 2; i >= 0; i--) {
            if (contains(indexes, i)) {
                swapElements(i, i + 1);
                newSelection[j++] = i + 1;
            }
        }
        return newSelection;
    }

    /*
     * Returns true if the indices array contains the index, otherwise false.
     */
    private boolean contains(int[] indices, int index) {
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] == index) {
                return true;
            }
        }
        return false;
    }

    /**
     * Swaps the elements at the given indexes. Can be overridden in subclasses if additional logic
     * is needed.
     */
    protected void swapElements(int index1, int index2) {
        Object temp = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, temp);
    }

}
