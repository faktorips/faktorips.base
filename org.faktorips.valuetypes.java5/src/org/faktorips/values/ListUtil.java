/*******************************************************************************
 * Copyright (c) 2005-2013 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.values;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of general utility methods for lists.
 * 
 * @author Jan Ortmann
 */
public class ListUtil {

    private ListUtil() {
        // Prevent initialization.
    }

    /**
     * Initializes a list with a given value.
     */
    public static final <T> List<T> newList(T defaultValue) {
        List<T> newList = new ArrayList<T>();
        newList.add(defaultValue);
        return newList;
    }

    public static final <T, R extends T> List<? extends R> convert(List<? extends T> list, Class<R> newType) {
        for (T member : list) {
            if (!(member.getClass().isInstance(newType))) {
                throw new ClassCastException(member + " not instance of " + newType);
            }
        }
        @SuppressWarnings("unchecked")
        List<? extends R> convertList = (List<? extends R>)list;
        return convertList;
    }

}
