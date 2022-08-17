/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
     * Returns <code>true</code> if this object is either <code>null</code> or a {@link NullObject}.
     * 
     * @see NullObject
     */
    public static final boolean isNull(Object o) {
        return o == null || isNullObject(o);
    }

    /**
     * Returns whether the given object implements {@link NullObjectSupport} and
     * {@link NullObjectSupport#isNull() is null}.
     */
    public static boolean isNullObject(Object o) {
        return o instanceof NullObject || (o instanceof NullObjectSupport && ((NullObjectSupport)o).isNull());
    }

    /**
     * Compares two objects for equality, where either one or both objects may be <code>null</code>.
     * 
     * @param object1 the first object
     * @param object2 the second object
     * @return <code>true</code> if the values of both objects are the same
     * 
     * @deprecated since 21.6. Use {@link java.util.Objects#equals} instead
     */
    @Deprecated
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
     * This method checks whether the given object is an instance of the specified class. A
     * {@link ClassCastException} is thrown if the object is not an instance of the given class.
     * This method does nothing otherwise, especially if the given object is <code>null</code> .
     * 
     * @param object The object that should be tested
     * @param expectedClass The class against which the given object is tested
     */
    public static final void checkInstanceOf(Object object, Class<?> expectedClass) {
        if (object == null) {
            return;
        }
        strictCheckInstanceOf(object, expectedClass);
    }

    private static final void strictCheckInstanceOf(Object object, Class<?> expectedClass) {
        if (!(expectedClass.isInstance(object))) {
            throw new ClassCastException("The object " + object + " is not an instance of " + expectedClass);
        }
    }

    /**
     * Returns the given object if it is not {@code null} or the given default if it is.
     * 
     * @param maybeNull the object that is returned if it is not {@code null}
     * @param defaultIfNull the default value returned if the first parameter is {@code null}
     * @return given object if it is not {@code null} or the given default if it is
     */
    public static <T> T defaultIfNull(T maybeNull, T defaultIfNull) {
        return maybeNull == null ? defaultIfNull : maybeNull;
    }

}
