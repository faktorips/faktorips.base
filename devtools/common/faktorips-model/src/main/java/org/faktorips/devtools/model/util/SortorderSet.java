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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SortorderSet<T> {

    private final Map<T, Integer> valuesToPosition = new HashMap<>();

    public void add(T value, Integer sortorder) {
        valuesToPosition.put(value, sortorder);
    }

    public Set<T> getSortedValues() {
        TreeSet<T> sortedValues = new TreeSet<>((o1, o2) -> {
            Integer posO1 = valuesToPosition.get(o1);
            if (posO1 == null) {
                return 1;
            }
            Integer posO2 = valuesToPosition.get(o2);
            if (posO2 == null) {
                return -1;
            }
            return posO1.compareTo(posO2);
        });
        sortedValues.addAll(valuesToPosition.keySet());
        return sortedValues;
    }
}
