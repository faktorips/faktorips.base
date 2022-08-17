/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.faktorips.runtime.util.StringBuilderJoiner.DEFAULT_SEPARATOR;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import org.faktorips.runtime.util.StringBuilderJoiner;

/**
 * A collection of utility methods for Strings. We don't use a class library like apache-commons
 * here to minimize the dependencies for the generated code.
 */
public final class IpsStringUtils {

    public static final String EMPTY = "";

    private IpsStringUtils() {
        /* no instances */
    }

    /**
     * Returns {@code true} if {@code s} is either {@code null} or the empty string, otherwise
     * {@code false}.
     */
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Returns {@code true} if {@code s} is neither {@code null} nor the empty string, otherwise
     * {@code false}.
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    /**
     * Returns {@code true} if {@code s} is either {@code null}, the empty string or a string that
     * only contains whitespace, otherwise {@code false}.
     */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Returns {@code true} if {@code s} is neither {@code null}, the empty string nor a string that
     * only contains whitespace, otherwise {@code false}.
     */
    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /**
     * Joins the elements of the provided {@link Collection} into a single String containing the
     * provided elements with the given separator. No delimiter is added before or after the list.
     * 
     * @param collection the Collection of values to join together, may be {@code null}
     * @param separator the separator to use, {@code null} treated as ""
     * @return the joined String, empty if the collection is {@code null}
     */
    public static String join(Collection<?> collection, String separator) {
        return join((Iterable<?>)collection, separator);
    }

    /**
     * Joins the elements of the provided {@link Iterable} into a single String containing the
     * provided elements with the given separator. No delimiter is added before or after the list.
     * 
     * @param iterable the Collection of values to join together, may be {@code null}
     * @param separator the separator to use, {@code null} treated as ""
     * @return the joined String, empty if the collection is {@code null}
     */
    public static String join(Iterable<?> iterable, String separator) {
        // 16 (default) may be too small
        StringBuilder stringBuilder = new StringBuilder(256);
        StringBuilderJoiner.join(stringBuilder, iterable, separator, t -> stringBuilder.append(Objects.toString(t)));
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided {@link Iterable} into a single String containing the
     * provided elements with the default separator {@value StringBuilderJoiner#DEFAULT_SEPARATOR}.
     * No delimiter is added before or after the list.
     * 
     * @param iterable the Collection of values to join together, may be {@code null}
     * @return the joined String, empty if the collection is {@code null}
     */
    public static String join(Iterable<?> iterable) {
        return join(iterable, DEFAULT_SEPARATOR);
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided
     * elements with the default separator {@value StringBuilderJoiner#DEFAULT_SEPARATOR}. No
     * delimiter is added before or after the list.
     * 
     * @param objectArray the array of values to join together, may be {@code null}
     * @return the joined String, empty if the collection is {@code null}
     */
    public static String join(Object[] objectArray) {
        return join(Arrays.asList(objectArray));
    }

    /**
     * Joins the elements of the provided array into a single String containing the provided
     * elements with the given separator. No delimiter is added before or after the list.
     * 
     * @param objectArray the array of values to join together, may be {@code null}
     * @param separator the separator to use, {@code null} treated as ""
     * @return the joined String, empty if the collection is {@code null}
     */
    public static String join(Object[] objectArray, String separator) {
        return join(Arrays.asList(objectArray), separator);
    }

    /**
     * Joins the elements of the provided {@link Iterable} into a single String containing the
     * provided elements, converted to String with the given {@code toString} {@link Function}, with
     * the default separator {@value StringBuilderJoiner#DEFAULT_SEPARATOR}. No delimiter is added
     * before or after the list.
     * 
     * @param iterable the Collection of values to join together, may be {@code null}
     * @param toString the {@link Function} to convert an element from the {@link Iterable} to a
     *            String
     * @return the joined String, {@code null} if the collection is {@code null}
     */
    public static <T> String join(Iterable<T> iterable, Function<? super T, String> toString) {
        // 16 (default) may be too small
        StringBuilder stringBuilder = new StringBuilder(256);
        StringBuilderJoiner.join(stringBuilder, iterable, t -> stringBuilder.append(toString.apply(t)));
        return stringBuilder.toString();
    }

    /**
     * Joins the elements of the provided {@link Iterable} into a single String containing the
     * provided elements, converted to String with the given {@code toString} {@link Function}, with
     * the given separator. No delimiter is added before or after the list.
     * 
     * @param iterable the Collection of values to join together, may be {@code null}
     * @param separator the separator to use, {@code null} treated as ""
     * @param toString the {@link Function} to convert an element from the {@link Iterable} to a
     *            String
     * @return the joined String, {@code null} if the collection is {@code null}
     */
    public static <T> String join(Iterable<T> iterable, Function<? super T, String> toString, String separator) {
        // 16 (default) may be too small
        StringBuilder stringBuilder = new StringBuilder(256);
        StringBuilderJoiner.join(stringBuilder, iterable, separator, t -> stringBuilder.append(toString.apply(t)));
        return stringBuilder.toString();
    }

    public static String toLowerFirstChar(String string) {
        if (isEmpty(string)) {
            return string;
        }
        char firstChar = string.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            return string;
        }
        return Character.toLowerCase(firstChar) + string.substring(1);
    }
}
