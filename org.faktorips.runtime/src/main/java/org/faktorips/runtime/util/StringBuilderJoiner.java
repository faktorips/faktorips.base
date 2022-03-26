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
import java.util.Iterator;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class similar to {@link StringJoiner} / {@link String#join(CharSequence, Iterable)} /
 * {@link Collectors#joining(CharSequence)} that does not create intermediary Strings but appends
 * all parts to a preexisting {@link StringBuilder}.
 */
public class StringBuilderJoiner {

    public static final String DEFAULT_SEPARATOR = ", ";

    private StringBuilderJoiner() {
        // util
    }

    /**
     * Appends the elements of the provided {@link Iterable} to the given {@link StringBuilder},
     * separated by the given separator. No delimiter is added before or after the list.
     * 
     * @param sb the {@link StringBuilder} to which the values will be appended
     * @param iterable the Collection of values to join together, may be {@code null}
     * @param separator the separator to use, null treated as ""
     */
    public static final void join(StringBuilder sb, Iterable<?> iterable, String separator) {
        join(sb, iterable, separator, t -> sb.append(Objects.toString(t)));
    }

    /**
     * Appends the elements of the provided {@link Iterable} to the given {@link StringBuilder},
     * separated by the {@link #DEFAULT_SEPARATOR} {@value #DEFAULT_SEPARATOR}. No delimiter is
     * added before or after the list.
     * 
     * @param sb the {@link StringBuilder} to which the values will be appended
     * @param iterable the Collection of values to join together, may be {@code null}
     */
    public static final void join(StringBuilder sb, Iterable<?> iterable) {
        join(sb, iterable, t -> sb.append(Objects.toString(t)));
    }

    /**
     * Appends the elements of the provided array to the given {@link StringBuilder}, separated by
     * the {@link #DEFAULT_SEPARATOR} {@value #DEFAULT_SEPARATOR}. No delimiter is added before or
     * after the list.
     * 
     * @param sb the {@link StringBuilder} to which the values will be appended
     * @param objectArray the array of values to join together, may be {@code null}
     */
    public static final void join(StringBuilder sb, Object[] objectArray) {
        join(sb, Arrays.asList(objectArray));
    }

    /**
     * Appends the elements of the provided {@link Iterable}, converted to String with the given
     * {@code toString} {@link Function}, to the given {@link StringBuilder}, separated by the
     * {@link #DEFAULT_SEPARATOR} {@value #DEFAULT_SEPARATOR}. No delimiter is added before or after
     * the list.
     * 
     * @param sb the {@link StringBuilder} to which the values will be appended
     * @param iterable the Collection of values to join together, may be {@code null}
     * @param singleItemAppender a {@link Consumer} that takes a single element to append it with
     *            multiple calls to {@link StringBuilder#append(String)}
     */
    public static final <T> void join(StringBuilder sb,
            Iterable<T> iterable,
            Consumer<? super T> singleItemAppender) {
        join(sb, iterable, DEFAULT_SEPARATOR, singleItemAppender);
    }

    /**
     * Appends the elements of the provided array, converted to String with the given
     * {@code toString} {@link Function}, to the given {@link StringBuilder}, separated by the
     * {@link #DEFAULT_SEPARATOR} {@value #DEFAULT_SEPARATOR}. No delimiter is added before or after
     * the list.
     * 
     * @param sb the {@link StringBuilder} to which the values will be appended
     * @param objectArray the array of values to join together, may be {@code null}
     * @param singleItemAppender a {@link Consumer} that takes a single element to append it with
     *            multiple calls to {@link StringBuilder#append(String)}
     */
    public static final <T> void join(StringBuilder sb,
            T[] objectArray,
            Consumer<? super T> singleItemAppender) {
        join(sb, Arrays.asList(objectArray), singleItemAppender);
    }

    /**
     * Appends the elements of the provided {@link Iterable}, converted to String with the given
     * {@code toString} {@link Function}, to the given {@link StringBuilder}, separated by the given
     * separator. No delimiter is added before or after the list.
     * 
     * @param sb the {@link StringBuilder} to which the values will be appended
     * @param iterable the Collection of values to join together, may be {@code null}
     * @param separator the separator to use, null treated as ""
     * @param singleItemAppender a {@link Consumer} that takes a single element to append it with
     *            multiple calls to {@link StringBuilder#append(String)}
     */
    public static final <T> void join(StringBuilder sb,
            Iterable<T> iterable,
            String separator,
            Consumer<? super T> singleItemAppender) {
        if (iterable != null) {
            Iterator<T> it = iterable.iterator();
            if (it != null) {
                String sep = separator == null ? "" : separator;
                if (it.hasNext()) {
                    singleItemAppender.accept(it.next());
                }
                while (it.hasNext()) {
                    sb.append(sep);
                    singleItemAppender.accept(it.next());
                }
            }
        }
    }
}
