/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util.collections;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Compares two lists in their current order that means it compares the list one element after the
 * other.
 * 
 * @author dirmeier
 */
public class ListComparator<T> implements Comparator<List<T>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Comparator<T> elementComparator;

    public ListComparator(Comparator<T> elementComparator) {
        this.elementComparator = elementComparator;
    }

    public static <T> ListComparator<T> listComparator(Comparator<T> elementComparator) {
        return new ListComparator<>(elementComparator);
    }

    @Override
    public int compare(List<T> o1, List<T> o2) {
        int compareSize = Integer.compare(o1.size(), o2.size());
        if (compareSize != 0) {
            return compareSize;
        }
        for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
            int compareValueAtIndex = compareValueAtIndex(o1, o2, i);
            if (compareValueAtIndex != 0) {
                return compareValueAtIndex;
            }
        }
        return 0;
    }

    private int compareValueAtIndex(List<T> o1, List<T> o2, int i) {
        T o1Value = o1.get(i);
        T o2Value = o2.get(i);
        return elementComparator.compare(o1Value, o2Value);
    }

}
