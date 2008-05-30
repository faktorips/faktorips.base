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

package org.faktorips.runtime.internal;

/**
 * A collection of utility methods for Object. We don't use a class library like apache-commons
 * here to minimize the dependencies for the generated code.
 * 
 * @author Jan Ortmann
 */

public class ObjectUtil {

    /**
     * Returns <code>true</code> if either both objects are <code>null</code> or
     */
    public final static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1!=null) {
            return object1.equals(object2);
        }
        return false;
    }
    
}
