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

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of utility methods for the Java collection classes.
 */
public class CollectionUtil {

    /**
     * Adds the objects in the given array to the given list.
     */
    public final static <T> void add(List<T> list, T[] array) {
        for (T element : array) {
            list.add(element);
        }
    }

    /**
     * Creates a new <tt>ArrayList</tt> that contains the object references in the given array in
     * the same order. The list's capacity is equal to the array's length.
     * <p>
     * Note that the list returned by this method can be modified (without modifying the array) -
     * which is not possible if one uses <tt>java.util.Arrays.asList()</tt>.
     * 
     * @throws NullPointerException if array is <code>null</code>.
     */
    public final static <T> ArrayList<T> toArrayList(T[] array) {
        ArrayList<T> list = new ArrayList<T>(array.length);
        add(list, array);
        return list;
    }

    private CollectionUtil() {
        // Utility class not to be instantiated.
    }

}
