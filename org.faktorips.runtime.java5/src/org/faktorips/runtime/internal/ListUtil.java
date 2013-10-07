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

package org.faktorips.runtime.internal;

import java.util.List;

public class ListUtil {

    private ListUtil() {
        // Utility class not to be instantiated.
    }

    public final static <T, R extends T> List<? extends R> convert(List<T> list, Class<R> newType) {
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
