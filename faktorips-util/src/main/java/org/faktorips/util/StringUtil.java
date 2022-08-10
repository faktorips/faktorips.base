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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.faktorips.annotation.UtilityClass;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A collection of utility methods for Strings.
 */
@UtilityClass
public class StringUtil {

    public static final String BLANK = " ";

    public static final String CAMEL_CASE_SEPERATORS = "[-_., ]";

    public static final String CHARSET_UTF8 = "UTF-8";

    public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    private static final String FORMAT_CARDINALITY = "[%d..%s]"; //$NON-NLS-1$

    private static final String FORMAT_CARDINALITY_WITH_DEFAULT = "[%d..%s, %d]"; //$NON-NLS-1$

    private StringUtil() {
        // do not instantiate
    }

    /**
     * Reads the available bytes from the input stream and returns them as a string using the given
     * {@link java.nio.charset.Charset charset}.
     * <p>
     * This method closes the input stream before returning!
     */
    public static final String readFromInputStream(InputStream is, Charset charset) throws IOException {
        return readFromInputStream(is, charset.name());
    }

    /**
     * Reads the available bytes from the input stream and returns them as a string using the given
     * {@link java.nio.charset.Charset charset}.
     * <p>
     * This method closes the input stream before returning!
     */
    public static final String readFromInputStream(InputStream is, String charsetName) throws IOException {
        ByteArrayOutputStream buffer = StreamUtil.toByteArrayOutputStream(is);
        try (buffer) {
            return buffer.toString(charsetName);
        }
    }

    /**
     * Takes a name like a class name and removes the package information from the beginning.
     */
    public static final String unqualifiedName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(index + 1);
    }

    /**
     * Returns the package name for a given class name. Returns an empty String if the class name
     * does not contain a package name.
     * 
     * @throws NullPointerException if the qualifiedClassName is null.
     */
    public static final String getPackageName(String qualifiedClassName) {
        int index = qualifiedClassName.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return qualifiedClassName.substring(0, index);
    }

    /**
     * Returns the filename without extension.
     */
    public static String getFilenameWithoutExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index == -1) {
            return filename;
        }
        return filename.substring(0, index);
    }

    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return null;
        }
        return fileName.substring(index + 1, fileName.length());

    }

    /**
     * Returns the input stream to read the given data.
     * 
     * @param data The data to read.
     * @param charset The char set to be used to convert the string data to bytes.
     * @throws UnsupportedEncodingException if the given char set is unsupported.
     */
    public static InputStream getInputStreamForString(String data, String charset) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(data.getBytes(charset));
    }

    public static String toCamelCase(String text, boolean firstUp) {
        if (IpsStringUtils.isBlank(text)) {
            return "";
        }
        String[] words = text.split(CAMEL_CASE_SEPERATORS);
        StringBuilder result = new StringBuilder();
        boolean firstUp2 = firstUp;
        for (String word : words) {
            char firstChar = word.charAt(0);
            if (firstUp2) {
                firstChar = Character.toUpperCase(firstChar);
            } else {
                firstChar = Character.toLowerCase(firstChar);
            }
            result.append(firstChar).append(word.substring(1).toLowerCase());
            firstUp2 = true;
        }
        return result.toString();
    }

    /**
     * Returns a String where an occurrence of a character followed by an upper case character is
     * replaced by these characters divided by an underscore. Consecutive sequences of comma, dot,
     * hyphen and whitespace is also replaced by one single underscore.
     * <p>
     * For example:
     * 
     * <pre>
     *  CamelCase      &rarr;  Camel_Case
     *  a.,- b-cdEF    &rarr;  a_b_cd_EF
     * </pre>
     * 
     */
    public static String camelCaseToUnderscore(String text) {
        return camelCaseToUnderscore(text, false);
    }

    /**
     * Returns a String where an occurrence of a character followed by an upper case character is
     * replaced by these characters divided by an underscore. Consecutive sequences of comma, dot,
     * hyphen and whitespace is also replaced by one single underscore.
     * <p>
     * For example:
     * 
     * <pre>
     *  CamelCase      &rarr;  Camel_Case
     *  a.,- b-cdEF    &rarr;  a_b_cd_E_F       (splitUppercaseSequences = true)
     *  a.,- b-cdEF    &rarr;  a_b_cd_EF       (splitUppercaseSequences = false)
     * </pre>
     * 
     */
    public static String camelCaseToUnderscore(String text, boolean splitUppercaseSequences) {
        if (text == null || "".equals(text)) {
            return "";
        }
        String regex;
        if (splitUppercaseSequences) {
            regex = "([A-Z])";
        } else {
            regex = "([^A-Z])([A-Z])";
        }

        String result = text.replaceAll(regex, splitUppercaseSequences ? "_$1" : "$1_$2");

        // compress sequences of [,.-_ ] to a single underscore
        result = result.replaceAll(CAMEL_CASE_SEPERATORS + CAMEL_CASE_SEPERATORS + "*", "_");

        // cut off leading and trailing underscores
        if (result.charAt(0) == '_') {
            result = result.substring(1, result.length());
        }
        if (result.charAt(result.length() - 1) == '_') {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Returns a String of the form ' [x..y]', where x and y are the minimum and maximum values of a
     * range.
     * 
     * Note that maxValue will be represented as an asterisk (*) if its value is Integer.MAX_VALUE.
     * 
     * @param minValue minimum value of the range.
     * @param maxValue maximum value of the range or Integer.MAX_VALUE to indicate an unbound range.
     * @return a String representation of the range in the form ' [min..max]'.
     */
    public static String getRangeString(int minValue, int maxValue) {
        return String.format(FORMAT_CARDINALITY, minValue, formatMax(maxValue));
    }

    /**
     * Returns a String of the form ' [x..y, z]', where x and y are the minimum and maximum values
     * of a range, and z is the default value of that range.
     * 
     * Note that maxValue will be represented as an asterisk (*) if its value is Integer.MAX_VALUE.
     * 
     * @param minValue minimum value of the range.
     * @param maxValue maximum value of the range or Integer.MAX_VALUE to indicate an unbound range.
     * @param defaultValue default value of the range (if showDefault is false, this value will be
     *            ignored).
     * @return a String representation of the range in the form ' [min..max, default]'.
     */
    public static String getRangeString(int minValue, int maxValue, int defaultValue) {
        return String.format(FORMAT_CARDINALITY_WITH_DEFAULT, minValue, formatMax(maxValue), defaultValue);
    }

    private static String formatMax(int maxValue) {
        return maxValue == Integer.MAX_VALUE ? "*" : String.valueOf(maxValue);
    }

    /**
     * Checks whether the {@link String} contains whitespace.
     * 
     * @param string {@link String} to be checked
     * @return {@code true} if the {@code string} contains {@link Character#isWhitespace(char)
     *         whitespace}, {@code false} if not
     */
    public static boolean containsWhitespace(String string) {
        return string != null && string.codePoints().anyMatch(Character::isWhitespace);
    }

}
