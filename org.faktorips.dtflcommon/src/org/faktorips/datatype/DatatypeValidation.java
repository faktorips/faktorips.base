/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.datatype;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang.ClassUtils;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

public class DatatypeValidation {

    private static final String MSGCODE_PREFIX = "GENERIC DATATYPE-"; //$NON-NLS-1$
    public static final String MSGCODE_METHOD_NOT_FOUND = MSGCODE_PREFIX + "Method not found"; //$NON-NLS-1$
    public static final String MSGCODE_METHOD_NOT_STATIC = MSGCODE_PREFIX + "Method not static"; //$NON-NLS-1$

    private DatatypeValidation() {
        // util class
    }

    /**
     * Checks if the method returns a specific type. If {@code checkStatic} is {@code true} the
     * method will be checked accordingly. The varargs parameter {@code returnType} is comprehended
     * as OR. e.g. String.class, Integer.class is comprehended as
     * {@code String.class.isAssignableFrom(method.getReturnType()) || Integer.class.isAssignableFrom(method.getReturnType())}
     * Primitive datatypes will always be auto-boxed, so there is no need to check for
     * {@code Boolean.class} or {@code boolean.class}.
     * 
     * @param ml A {@code MessageList} with the results of the checks.
     * @param method The method to check.
     * @param checkStatic If the method should be static.
     * @param returnType The expected return types of the method
     */
    public static void checkMethod(MessageList ml, Method method, boolean checkStatic, Class<?>... returnType) {
        if (method != null) {

            if (!isCompatible(method.getReturnType(), returnType)) {
                ml.add(Message.newError(MSGCODE_METHOD_NOT_FOUND,
                        "The method " + method + " does not return a " //$NON-NLS-1$//$NON-NLS-2$
                                + Arrays.stream(returnType).map(Class::getSimpleName)
                                        .collect(Collectors.joining(" or ")))); //$NON-NLS-1$
            }

            if (checkStatic && !Modifier.isStatic(method.getModifiers())) {
                ml.add(Message.newError(MSGCODE_METHOD_NOT_STATIC, "The method " + method + " is not static.")); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
    }

    private static boolean isCompatible(Class<?> actual, Class<?>... expected) {
        Class<?> wrappedActual = ClassUtils.primitiveToWrapper(actual);
        return Arrays.stream(expected)
                .map(ClassUtils::primitiveToWrapper)
                .anyMatch(cl -> ((Class<?>)cl).isAssignableFrom(wrappedActual));
    }

}
