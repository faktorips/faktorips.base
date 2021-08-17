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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Compares two collections by sorting the content using the element comparator and then compare one
 * element after the other. If a collection contains duplicated entries these duplicates will be
 * removed.
 * 
 * If you need to compare two lists with specific order and duplicated values you may better use
 * {@link ListComparator}
 * 
 */
public class DistinctElementComparator<T> implements Comparator<Collection<T>>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Comparator<T> elementComparator;

    public DistinctElementComparator(Comparator<T> elementComparator) {
        this.elementComparator = elementComparator;
    }

    public static <T> DistinctElementComparator<T> createComparator(Comparator<T> elementComparator) {
        return new DistinctElementComparator<>(elementComparator);
    }

    @Override
    public int compare(Collection<T> o1, Collection<T> o2) {
        TreeSet<T> sorted1 = new TreeSet<>(elementComparator);
        sorted1.addAll(o1);
        TreeSet<T> sorted2 = new TreeSet<>(elementComparator);
        sorted2.addAll(o2);
        Iterator<T> iterator2 = sorted2.iterator();
        for (Iterator<T> iterator1 = sorted1.iterator(); iterator1.hasNext() && iterator2.hasNext();) {
            int compareValue = elementComparator.compare(iterator1.next(), iterator2.next());
            if (compareValue != 0) {
                return compareValue;
            }
        }
        return Integer.compare(sorted1.size(), sorted2.size());
    }

}
