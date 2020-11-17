package org.faktorips.devtools.model.util;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang.ObjectUtils;

public class NullSafeComparableComparator<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings("unchecked")
    public int compare(T o1, T o2) {
        Comparable<T> c1 = (Comparable<T>)o1;
        Comparable<T> c2 = (Comparable<T>)o2;
        return ObjectUtils.compare(c1, c2);
    }

}