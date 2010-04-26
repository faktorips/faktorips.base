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

package org.faktorips.values;

/**
 * A collection of general utility methods for objects.
 * 
 * @author Jan Ortmann
 */
public class ObjectUtil {

    /**
     * Returns <code>true</code> if this object is either <code>null</code> or a NullObject.
     * 
     * @see NullObject
     */
    public final static boolean isNull(Object o) {
        return o == null || (o instanceof NullObject);
    }

    /**
     * Compares two objects for equality, where either one or both objects may be <code>null</code>.
     * 
     * @param object1 the first object
     * @param object2 the second object
     * @return <code>true</code> if the values of both objects are the same
     */
    public static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if ((object1 == null) || (object2 == null)) {
            return false;
        }
        return object1.equals(object2);
    }

    private ObjectUtil() {
        super();
    }

}
