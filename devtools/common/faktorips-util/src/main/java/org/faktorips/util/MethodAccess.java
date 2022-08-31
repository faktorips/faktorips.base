/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.util;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Wraps access to a method. Allows {@link #check(MessageList, String) checks} on the method and its
 * {@link #invoke(String, Object, Object...) invocation}.
 */
public class MethodAccess {

    private final Class<?> clazz;
    private final String methodName;
    private final Class<?>[] parameterTypes;

    private Optional<Method> method;
    private RuntimeException exception;

    private MethodAccess(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        this.clazz = clazz;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        try {
            method = Optional.of(getMethod());
            // CSOFF: Illegal Catch
        } catch (RuntimeException e) {
            exception = e;
            method = Optional.empty();
        }
    }

    /**
     * Creates a {@link MethodAccess} for the given class and method name.
     * <p>
     * If the method can not be found, {@link #check(MessageList, String) checks} and
     * {@link #invoke(String, Object, Object...) invocations} will fail when called.
     *
     * @param clazz a class
     * @param methodName the name of a method of the given class
     * 
     * @throws NullPointerException if no class or method name is given
     * @throws IllegalArgumentException if the method name is empty
     */
    public static final MethodAccess of(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        requireNonNull(clazz, "clazz must not be null");
        if (IpsStringUtils.isBlank(requireNonNull(methodName, "methodName must not be null"))) {
            throw new IllegalArgumentException("methodName must not be empty");
        }
        for (Class<?> parameterType : parameterTypes) {
            requireNonNull(parameterType, "parameterType must not be null");
        }

        return new MethodAccess(clazz, methodName, parameterTypes);
    }

    private Method getMethod() {
        return Arrays.stream(clazz.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> m.getParameterCount() == parameterTypes.length)
                .filter(m -> {
                    var actualParameterTypes = m.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (!parameterTypes[i].isAssignableFrom(actualParameterTypes[i])) {
                            return false;
                        }
                    }
                    return true;
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Unable to find the method " + methodName + "("
                                + Arrays.stream(parameterTypes).map(Object::toString).collect(Collectors.joining())
                                + ") on the adapted class " + clazz));
    }

    /**
     * Invokes the wrapped method on the given object with the given arguments.
     *
     * @param methodDescription a description of what the method does, like "to find a value by
     *            name", that will be included in an exception's message if one is thrown
     * @param object an instance of the wrapped class on which the method should be invoked
     * @param args the arguments (if any) to be passed to the method
     * @param <T> the expected type of the return value
     * @return the method's return value
     * 
     * @throws MethodAccessException if invocation of the method fails
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke(String methodDescription, Object object, Object... args) {
        try {
            return (T)method.orElseThrow(() -> exception).invoke(object, args);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException
                | IllegalStateException | NullPointerException e) {
            throw new MethodAccessException(
                    "Unable to invoke the method " + methodDescription + " " + methodName + " on the class: " + clazz,
                    e);
        }
    }

    /**
     * Invokes the wrapped static method on the wrapped class with the given arguments.
     *
     * @param methodDescription a description of what the method does, like "to find a value by
     *            name", that will be included in an exception's message if one is thrown
     * @param args the arguments (if any) to be passed to the method
     * @param <T> the expected type of the return value
     * @return the method's return value
     * 
     * @throws MethodAccessException if invocation of the method fails
     */
    public <T> T invokeStatic(String methodDescription, Object... args) {
        return invoke(methodDescription, null, args);
    }

    /**
     * Returns whether the wrapped method actually exists.
     */
    public boolean exists() {
        return method.isPresent();
    }

    @Override
    public String toString() {
        return "MethodAccess [" + clazz + "." + methodName
                + (parameterTypes != null ? "(" + Arrays.toString(parameterTypes) + ")" : "()");
    }

    /**
     * Creates a new {@link Check} object for this class and method name.
     * <p>
     * All checks called on the {@link Check} object report problems to the given
     * {@link MessageList} using the given prefix for their message codes.
     * <p>
     * The first check to be called should be {@link Check#exists()}, because other checks won't
     * work if there is no method to check.
     *
     * @param messageList a list of messages to which this check can report problems
     * @param msgCodePrefix a prefix to be used for message codes
     */
    public Check check(MessageList messageList, String msgCodePrefix) {
        requireNonNull(messageList, "messageList must not be null");
        requireNonNull(msgCodePrefix, "msgCodePrefix must not be null");
        return new Check(messageList, msgCodePrefix);
    }

    /**
     * Offers multiple checks on a {@link MethodAccess} in a fluent API.
     */
    public class Check {

        public static final String MSG_CODE_SUFFIX_DOES_NOT_EXIST = "_NOT_FOUND";
        public static final String MSG_CODE_SUFFIX_NOT_STATIC = "_NOT_STATIC";
        public static final String MSG_CODE_SUFFIX_STATIC = "_STATIC";
        public static final String MSG_CODE_SUFFIX_INCOMPATIBLE_RETURN_TYPE = "_INCOMPATIBLE_RETURN_TYPE";

        private final MessageList messageList;
        private final String msgCodePrefix;

        /**
         * Creates a new {@link Check} object. All checks called on this object report problems to
         * the given {@link MessageList} using the given prefix for their message codes.
         * <p>
         * The first check to be called should be {@link #exists()}, because other checks won't work
         * if there is no method to check.
         *
         * @param messageList a list of messages to which this check can report problems
         * @param msgCodePrefix a prefix to be used for message codes
         */
        private Check(MessageList messageList, String msgCodePrefix) {
            this.messageList = messageList;
            this.msgCodePrefix = msgCodePrefix;
        }

        /**
         * Checks whether the method exists. If the method does not exist or cannot be accessed from
         * this class, an error message with a message code ending in
         * {@value #MSG_CODE_SUFFIX_DOES_NOT_EXIST} will be added to the message list.
         *
         * @return this {@link Check} object for further checks.
         */
        public Check exists() {
            if (!MethodAccess.this.exists()) {
                String text = MessageFormat.format("The Java class {0} hasn''t got a method {1}", clazz, methodName);
                if (messageList.getMessagesFor(clazz, methodName)
                        .getMessagesByCode(msgCodePrefix + MSG_CODE_SUFFIX_DOES_NOT_EXIST).isEmpty()) {
                    messageList.add(Message
                            .error(text)
                            .code(msgCodePrefix + MSG_CODE_SUFFIX_DOES_NOT_EXIST)
                            .invalidObjectWithProperties(clazz, methodName).create());
                }
            }
            return this;
        }

        /**
         * If the method {@link #exists()}, checks whether the method is static. If the method is
         * not static, an error message with a message code ending in
         * {@value #MSG_CODE_SUFFIX_NOT_STATIC} will be added to the message list.
         *
         * @return this {@link Check} object for further checks.
         */
        public Check isStatic() {
            method.ifPresentOrElse(m -> {
                if (!Modifier.isStatic(m.getModifiers())) {
                    messageList.add(Message
                            .error("The method " + m + " is not static.")
                            .code(msgCodePrefix + MSG_CODE_SUFFIX_NOT_STATIC)
                            .invalidObjectWithProperties(clazz, methodName).create());
                }
            }, this::exists);
            return this;
        }

        /**
         * If the method {@link #exists()}, checks whether the method is not static. If the method
         * is static, an error message with a message code ending in
         * {@value #MSG_CODE_SUFFIX_STATIC} will be added to the message list.
         *
         * @return this {@link Check} object for further checks.
         */
        public Check isNotStatic() {
            method.ifPresentOrElse(m -> {
                if (Modifier.isStatic(m.getModifiers())) {
                    messageList.add(Message
                            .error("The method " + m + " is static.")
                            .code(msgCodePrefix + MSG_CODE_SUFFIX_STATIC)
                            .invalidObjectWithProperties(clazz, methodName).create());
                }
            }, this::exists);
            return this;
        }

        /**
         * If the method {@link #exists()}, checks whether the method return type is compatible to
         * any one of the given return types. If the method returns something different, an error
         * message with a message code ending in {@value #MSG_CODE_SUFFIX_INCOMPATIBLE_RETURN_TYPE}
         * will be added to the message list.
         * <p>
         * Compatible primitive datatypes and their wrappers will be mapped automatically, so for
         * example {@link Boolean Boolean.class} and {@link Boolean#TYPE} do not need to be both
         * specified.
         *
         * @return this {@link Check} object for further checks.
         */
        public Check returnTypeIsCompatible(Class<?>... expectedReturnTypes) {
            method.ifPresent(m -> {
                if (!isCompatible(m.getReturnType(), expectedReturnTypes)) {
                    messageList.add(Message
                            .error("The method " + m + " does not return a "
                                    + Arrays.stream(expectedReturnTypes).map(Class::getSimpleName)
                                            .collect(Collectors.joining(" or ")))
                            .code(msgCodePrefix + MSG_CODE_SUFFIX_INCOMPATIBLE_RETURN_TYPE)
                            .invalidObjectWithProperties(clazz, methodName).create());
                }
            });
            return this;
        }

        private boolean isCompatible(Class<?> actualType, Class<?>... expectedTypes) {
            Class<?> wrappedActualType = ClassUtils.primitiveToWrapper(actualType);
            return Arrays.stream(expectedTypes)
                    .map(ClassUtils::primitiveToWrapper)
                    .anyMatch(cl -> ((Class<?>)cl).isAssignableFrom(wrappedActualType));
        }

    }

    public static class MethodAccessException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public MethodAccessException(String message) {
            super(message);
        }

        public MethodAccessException(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
