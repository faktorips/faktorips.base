/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

/**
 * A collection of general utility methods for objects.
 * 
 * @author Jan Ortmann
 */
public class ObjectUtil {

    private ObjectUtil() {
        super();
    }

    /**
     * Returns <code>true</code> if this object is either <code>null</code> or a NullObject.
     * 
     * @see NullObject
     */
    public static final boolean isNull(Object o) {
        return o == null || (o instanceof NullObject);
    }

    /**
     * Compares two objects for equality, where either one or both objects may be <code>null</code>.
     * 
     * @param object1 the first object
     * @param object2 the second object
     * @return <code>true</code> if the values of both objects are the same
     */
    public static final boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if ((object1 == null) || (object2 == null)) {
            return false;
        }
        return object1.equals(object2);
    }

    /**
     * This method checks whether the given object is an instance of the specified class. If the
     * object is not an instance of the given class, this method throws an
     * {@link ClassCastException}, otherwise it returns.
     * 
     * @param object The object that should be tested
     * @param expectedClass The class of which type the object have to be an instance of
     */
    public static final void checkInstanceOf(Object object, Class<?> expectedClass) {
        if (!(expectedClass.isInstance(object))) {
            throw new ClassCastException("The object " + object + "is not an instance of " + expectedClass);
        }
    }

}
