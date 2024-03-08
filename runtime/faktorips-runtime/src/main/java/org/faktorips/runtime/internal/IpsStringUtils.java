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
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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
        return s == null || s.isBlank();
    }

    /**
     * Returns {@code true} if {@code s} is neither {@code null}, the empty string nor a string that
     * only contains whitespace, otherwise {@code false}.
     */
    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /**
     * Trims the parameters, if the are not {@code null} and checks for equality using
     * {@link Objects#equals(Object, Object)}.
     *
     * @param s1 a string
     * @param s2 another string to be compared with a for equality
     * @return {@code true} if the arguments are equal to each other and {@code false} otherwise
     */
    public static boolean trimEquals(String s1, String s2) {
        return Objects.equals(
                s1 != null ? s1.trim() : s1,
                s2 != null ? s2.trim() : s2);
    }

    /**
     * Compares two strings, treating {@code null} as {@link #EMPTY}.
     *
     * @param s1 a string
     * @param s2 another string
     * @return whether both strings are equal
     */
    public static boolean equalsNullAsEmpty(String s1, String s2) {
        return Objects.equals(s1 == null ? EMPTY : s1, s2 == null ? EMPTY : s2);
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
        return changeFirstChar(string, Character::isLowerCase, Character::toLowerCase);
    }

    /** @since 24.7 */
    public static String toUpperFirstChar(String string) {
        return changeFirstChar(string, Character::isUpperCase, Character::toUpperCase);
    }

    private static String changeFirstChar(String string,
            Predicate<Character> isAlreadyChanged,
            UnaryOperator<Character> change) {
        if (isEmpty(string)) {
            return string;
        }
        char firstChar = string.charAt(0);
        if (isAlreadyChanged.test(firstChar)) {
            return string;
        }
        return change.apply(firstChar) + string.substring(1);
    }

    /**
     * <p>
     * Replaces all occurrences of Strings within another String.
     * </p>
     *
     * <p>
     * A {@code null} reference passed to this method is a no-op, or if any "search string" or
     * "string to replace" is {@code null}, that replace will be ignored.
     * </p>
     *
     * <pre>
     *  IpsStringUtils.replaceEach(null, *, *)        = null
     *  IpsStringUtils.replaceEach("", *, *)          = ""
     *  IpsStringUtils.replaceEach("aba", null, null) = "aba"
     *  IpsStringUtils.replaceEach("aba", new String[0], null) = "aba"
     *  IpsStringUtils.replaceEach("aba", null, new String[0]) = "aba"
     *  IpsStringUtils.replaceEach("aba", new String[]{"a"}, null)  = "aba"
     *  IpsStringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""})  = "b"
     *  IpsStringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"})  = "aba"
     *  IpsStringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"})  = "wcte"
     *  (example of how it does not repeat)
     *  IpsStringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"})  = "dcte"
     * </pre>
     *
     * @param text text to search and replace in, no-op if null
     * @param searchList the Strings to search for, no-op if null
     * @param replacementList the Strings to replace them with, no-op if null
     * @return the text with any replacements processed, {@code null} if null String input
     * @throws IllegalArgumentException if the lengths of the arrays are not the same (null is ok,
     *             and/or size 0)
     */
    // adapted from Apache StringUtils, to avoid external dependencies.
    // CSOFF: CyclomaticComplexity
    // CSOFF: BooleanExpression
    public static String replaceEach(final String text,
            final String[] searchList,
            final String[] replacementList) {

        if (isEmpty(text) || isEmpty(searchList) || isEmpty(replacementList)) {
            return text;
        }

        final int searchLength = searchList.length;
        final int replacementLength = replacementList.length;

        // make sure lengths are ok, these need to be equal
        if (searchLength != replacementLength) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: "
                    + searchLength
                    + " vs "
                    + replacementLength);
        }

        // keep track of which still have matches
        final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        // index on index that the match was found
        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1;

        // index of replace array that will replace the search string found
        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || isEmpty(searchList[i]) || replacementList[i] == null) {
                continue;
            }
            tempIndex = text.indexOf(searchList[i]);

            // see if we need to keep searching for this
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else if (textIndex == -1 || tempIndex < textIndex) {
                textIndex = tempIndex;
                replaceIndex = i;
            }
        }

        // no search strings found, we are done
        if (textIndex == -1) {
            return text;
        }

        int start = 0;

        // get a good guess on the size of the result buffer so it doesn't have to double if it goes
        // over a bit
        int increase = 0;

        // count the replacement text elements that are larger than their corresponding text being
        // replaced
        for (int i = 0; i < searchList.length; i++) {
            if (searchList[i] == null || replacementList[i] == null) {
                continue;
            }
            final int greater = replacementList[i].length() - searchList[i].length();
            if (greater > 0) {
                // assume 3 matches
                increase += 3 * greater;
            }
        }
        // have upper-bound at 20% increase, then let Java take over
        increase = Math.min(increase, text.length() / 5);

        final StringBuilder buf = new StringBuilder(text.length() + increase);

        while (textIndex != -1) {

            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacementList[replaceIndex]);

            start = textIndex + searchList[replaceIndex].length();

            textIndex = -1;
            replaceIndex = -1;
            // find the next earliest match
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || searchList[i] == null
                        || searchList[i].isEmpty() || replacementList[i] == null) {
                    continue;
                }
                tempIndex = text.indexOf(searchList[i], start);

                // see if we need to keep searching for this
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }

        }
        final int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        return buf.toString();
    }
    // CSON: BooleanExpression
    // CSON: CyclomaticComplexity

    private static boolean isEmpty(String[] stringArray) {
        if (stringArray == null) {
            return true;
        }
        return stringArray.length == 0;
    }
}
