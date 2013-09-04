/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
