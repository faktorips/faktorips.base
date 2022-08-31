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

import java.io.Serializable;
import java.util.Comparator;

public class NullSafeComparableComparator<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compare(T o1, T o2) {
        Comparable<T> c1 = (Comparable<T>)o1;
        Comparable<T> c2 = (Comparable<T>)o2;
        return Comparator.nullsFirst(Comparator.<Comparable> naturalOrder()).compare(c1, c2);
    }

}
