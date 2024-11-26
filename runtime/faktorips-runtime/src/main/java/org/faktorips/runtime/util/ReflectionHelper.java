/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.util;

import java.util.Arrays;
import java.util.Optional;

import org.faktorips.annotation.UtilityClass;

/**
 * A helper to make dealing with Java's reflection API a little easier.
 */
@UtilityClass
public class ReflectionHelper {

    private ReflectionHelper() {
        // util
    }

    /**
     * Finds a static field of the given name in the given class and returns its value, if such a
     * field is present, otherwise an {@link Optional#empty() empty Optional}.
     *
     * @param <R> the return value's type
     * @param clazz a class
     * @param fieldName the name of a field of the class
     * @return the field's value if present, otherwise an {@link Optional#empty() empty Optional}.
     */
    public static <R> Optional<R> findStaticFieldValue(Class<?> clazz, String fieldName) {
        return findFieldValue(clazz, fieldName, null);
    }

    /**
     * Finds a field of the given name in the given class and returns its value from the given
     * object, if such a field is present, otherwise an {@link Optional#empty() empty Optional}.
     *
     * @param <T> the type of the class and object
     * @param <R> the return value's type
     * @param clazz a class
     * @param fieldName the name of a field of the class
     * @param object an object of the type of the {@code clazz}; may be {@code null} for static
     *            fields
     * @return the field's value if present, otherwise an {@link Optional#empty() empty Optional}.
     */
    @SuppressWarnings("unchecked")
    public static <T, R> Optional<R> findFieldValue(Class<T> clazz, String fieldName, T object) {
        return Arrays.stream(clazz.getDeclaredFields()).filter(f -> fieldName.equals(f.getName()))
                .findFirst()
                .map(f -> {
                    try {
                        f.setAccessible(true);
                        return (R)f.get(object);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
