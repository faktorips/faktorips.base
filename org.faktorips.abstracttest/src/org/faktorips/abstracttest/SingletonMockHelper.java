/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper class for replacing singleton instances with a mock implementation.
 * <p>
 * Remember that it is bad style to use singletons at all. However we have this class to replace
 * singletons that we have to use from any framework we cannot change. Always try to prevent using
 * the singleton first before you use this helper in your test.
 * <p>
 * Consider that many tests run in the same static content. To avoid side effects you have to keep
 * the instance of this helper and call the {@link #reset()} method in your test tear down. If you
 * want to use this helper only inside one test case, call the {@link #reset()} method in a
 * {@code finally} block. Also consider that it is not possible to run tests parallel when using
 * this helper!
 */
public class SingletonMockHelper {

    private final Map<Field, Object> singletonMap;

    /**
     * Instantiate the singleton mock helper.
     * <p>
     * Keep this instance in your test case to call {@link #reset()} in your test's tear down
     * method.
     */
    public SingletonMockHelper() {
        singletonMap = new HashMap<Field, Object>();
    }

    /**
     * This method should be used to replace a singleton with a mock implementation for testing
     * purposes. It looks for a static field declared in the given class that has the same type as
     * the class, no matter what it's called ("instance", "plugin", ...) and sets it to the given
     * value.
     * <p>
     * The instance field MUST NOT be final, or this method will fail.
     * <p>
     * If you want to mock something which usually has heavyweight initialization, you should do
     * that initialization in it's constructor and only instantiate the instance on the first call
     * to getInstance() and then set the instance via this helper before calling getInstance().
     * 
     * @param <T> the type of the singleton class
     * @param clazz the singleton class
     * @param instance the new instance
     * @throws IllegalArgumentException if the class does not have exactly one field of it's type or
     *             setting the field's value is not possible.
     */
    public <T> void setSingletonInstance(Class<T> clazz, T instance) throws IllegalArgumentException {
        Field[] fields = clazz.getDeclaredFields();
        Field singletonField = null;
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(clazz) && Modifier.isStatic(field.getModifiers())) {
                if (singletonField != null) {
                    throw new IllegalArgumentException(String.format(
                            "The class %s has more than one field of it's type, so the field to be set for the singleton pattern could not be determined.",
                            clazz.getName()));
                } else {
                    singletonField = field;
                }
            }
        }
        if (singletonField == null) {
            throw new IllegalArgumentException(String.format(
                    "The class %s has no field of it's type, so the field to be set for the singleton pattern could not be determined.",
                    clazz.getName()));
        }
        try {
            singletonField.setAccessible(true);
            Object oldValue = singletonField.get(null);
            singletonField.set(null, instance);
            if (!singletonMap.containsKey(singletonField)) {
                singletonMap.put(singletonField, oldValue);
            }
            singletonField.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                    String.format("Could not set the field %s on class %s.", singletonField.getName(), clazz.getName()),
                    e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(
                    String.format("Could not change accessibility for the field %s on class %s.",
                            singletonField.getName(), clazz.getName()),
                    e);
        }
    }

    /**
     * Restores mocked singletons to their original values.
     */
    public void reset() {
        for (Entry<Field, Object> entry : singletonMap.entrySet()) {
            Field singletonField = entry.getKey();
            try {
                singletonField.setAccessible(true);
                singletonField.set(null, entry.getValue());
                singletonField.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(String.format("Could not set the field %s on class %s.",
                        singletonField.getName(), singletonField.getClass().getName()), e);
            } catch (SecurityException e) {
                throw new IllegalArgumentException(
                        String.format("Could not change accessibility for the field %s on class %s.",
                                singletonField.getName(), singletonField.getClass().getName()),
                        e);
            }
        }
        singletonMap.clear();
    }
}
