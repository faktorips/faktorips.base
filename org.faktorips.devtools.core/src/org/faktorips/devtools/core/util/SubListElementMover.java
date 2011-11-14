/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.Arrays;
import java.util.List;

/**
 * Provides functionality to move elements within a sub list of a larger list up or down.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public class SubListElementMover<T> implements IElementMover {

    private final List<T> list;

    private final List<T> subList;

    /**
     * @param list the {@link List} containing all elements
     * @param subList the {@link List} containing the elements that constitute the sub list
     */
    public SubListElementMover(List<T> list, List<T> subList) {
        this.list = list;
        this.subList = subList;
    }

    @Override
    public int[] move(int[] indices, boolean up) {
        // The result indices are set to the provided indices and will be modified accordingly
        int[] newIndices = Arrays.copyOf(indices, indices.length);

        // Do nothing if the provided index array is empty
        if (indices.length == 0) {
            return newIndices;
        }

        // Compute the indices in the larger list for the provided inner list indices
        int[] outerIndices = getOuterIndicesForSubListIndices();

        // Sort the indices to be moved in ascending order to be independent of the provided order
        int[] sortedIndices = Arrays.copyOf(indices, indices.length);
        Arrays.sort(sortedIndices);

        /*
         * Do nothing if the first element of the sub list is part of an up-move or the last element
         * of the sub list is part of a down-move.
         */
        if ((up && sortedIndices[0] == 0) || (!up && sortedIndices[sortedIndices.length - 1] == subList.size() - 1)) {
            return newIndices;
        }

        if (up) {
            for (int i = 0; i < sortedIndices.length; i++) {
                int sourceIndex = outerIndices[sortedIndices[i]];
                int targetIndex = outerIndices[sortedIndices[i] - 1];
                newIndices[i]--;
                swapElements(sourceIndex, targetIndex);
            }
        } else {
            for (int i = sortedIndices.length - 1; i >= 0; i--) {
                int sourceIndex = outerIndices[sortedIndices[i]];
                int targetIndex = outerIndices[sortedIndices[i] + 1];
                newIndices[i]++;
                swapElements(sourceIndex, targetIndex);
            }
        }

        return newIndices;
    }

    private int[] getOuterIndicesForSubListIndices() {
        int[] outerIndices = new int[subList.size()];
        for (int i = 0; i < outerIndices.length; i++) {
            outerIndices[i] = list.indexOf(subList.get(i));
        }
        return outerIndices;
    }

    @Override
    public int[] moveUp(int[] indices) {
        return move(indices, true);
    }

    @Override
    public int[] moveDown(int[] indices) {
        return move(indices, false);
    }

    private void swapElements(int sourceIndex, int targetIndex) {
        T sourceElement = list.get(sourceIndex);
        T targetElement = list.get(targetIndex);
        list.set(sourceIndex, targetElement);
        list.set(targetIndex, sourceElement);
    }

}
