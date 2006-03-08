/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of utilitiy methods for the Java collection classes.
 */
public class CollectionUtil {
    
    /**
     * Clears the list and adds the objects from the array. 
     */
    public final static void copy(List list, Object[] array) {
        list.clear();
        list.add(array);
    }
    
    
    /**
     * Adds the objects in the array to the list.
     */
    public final static void add(List list, Object[] array) {
        for (int i=0; i<array.length; i++) {
            list.add(array[i]);
        }
    }
    
    /**
     * Creates a new arraylist that contains the object references in the 
     * array in the same order. The list's capacity is equal to the array's length. 
     * 
     * @throws NullPointerException if array is <code>null</code>.
     */
    public final static ArrayList toArrayList(Object[] array) {
        ArrayList list = new ArrayList(array.length);
        add(list, array);
        return list;
    }
    
    private CollectionUtil() {
    }

}
