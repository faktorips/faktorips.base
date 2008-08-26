/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.valueset;


/**
 * Collection of utility methods for this package.
 * 
 * @author Peter Erzberger
 */
public class Util{

    /**
     * Compares the two objects for equality considering the case that the parameters can be null.
     * If both parameters are null this method returns true. 
     */
    public static boolean equals(Object first, Object second) {
        if (first == second) {
            return true;
        }
        if ((first == null) || (second == null)) {
            return false;
        }
        return first.equals(second);
    }

}
