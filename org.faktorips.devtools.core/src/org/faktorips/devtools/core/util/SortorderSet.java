/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SortorderSet<T> {

    private final Map<T, Integer> valuesToPosition = new HashMap<T, Integer>();

    public void add(T value, Integer sortorder) {
        valuesToPosition.put(value, sortorder);
    }

    public Set<T> getSortedValues() {
        TreeSet<T> sortedValues = new TreeSet<T>(new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                Integer posO1 = valuesToPosition.get(o1);
                if (posO1 == null) {
                    return 1;
                }
                Integer posO2 = valuesToPosition.get(o2);
                if (posO2 == null) {
                    return -1;
                }
                return posO1.compareTo(posO2);
            }

        });
        sortedValues.addAll(valuesToPosition.keySet());
        return sortedValues;
    }
}
