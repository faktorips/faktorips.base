/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.abstracttest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Helper class for replacing singleton instances with a mock implementation.
 * 
 * @author Daniel Schwering, Faktor Zehn AG
 */
public class SingletonMockHelper {

    /**
     * This method should be used to replace a singleton with a mock implementation for testing
     * purposes. It looks for a static field declared in the given class that has the same type as
     * the class, no matter what it's called ("instance", "plugin", ...) and sets it to the given
     * value.
     * 
     * The instance field MUST NOT be final, or this method will fail.
     * 
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
    public static <T> void setSingletonInstance(Class<T> clazz, T instance) throws IllegalArgumentException {
        Field[] fields = clazz.getDeclaredFields();
        Field singletonField = null;
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(clazz) && Modifier.isStatic(field.getModifiers())) {
                if (singletonField != null) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "The class %s has more than one field of it's type, so the field to be set for the singleton pattern could not be determined.",
                                    clazz.getName()));
                } else {
                    singletonField = field;
                }
            }
        }
        if (singletonField == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "The class %s has no field of it's type, so the field to be set for the singleton pattern could not be determined.",
                            clazz.getName()));
        }
        try {
            singletonField.setAccessible(true);
            singletonField.set(null, instance);
            singletonField.setAccessible(false);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Could not set the field %s on class %s.",
                    singletonField.getName(), clazz.getName()), e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(String.format(
                    "Could not change accessibility for the field %s on class %s.", singletonField.getName(),
                    clazz.getName()), e);
        }
    }
}
