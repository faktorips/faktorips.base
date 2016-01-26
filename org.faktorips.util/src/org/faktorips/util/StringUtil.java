/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * A collection of utility methods for Strings.
 * 
 * @author Jan Ortmann
 */
public class StringUtil {

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
     * {@link java.nio.charset.Charset charset}. The method does not close the stream.
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

        StringBuffer buf = new StringBuffer(is.available());
        BufferedReader in = new BufferedReader(new InputStreamReader(is, charsetName));

        try {
            int charValue = 0;
            while (true) {
                charValue = in.read();
                if (charValue == -1) {
                    break;
                }
                buf.append((char)charValue);
            }

            return buf.toString();
        } finally {
            in.close();
        }
    }

    /**
     * Returns the passed String enclosed in double quotes.
     */
    public static String quote(String s) {
        if (s == null) {
            return null;
        }
        return "\"" + s + "\"";
    }

    /**
     * Returns the passed List of String enclosing every entry in double quotes
     */
    public static List<String> quoteAll(List<String> strings) {
        List<String> result = new ArrayList<String>(strings.size());
        for (String aString : strings) {
            result.add(quote(aString));
        }
        return result;
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
     * Takes a name like a class name and removes the package information from the beginning,
     * leaving the last two parts and the '.' between.
     */
    public static final String unqualifiedRelationName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(qualifiedName.lastIndexOf('.', qualifiedName.lastIndexOf('.') - 1) + 1);
    }

    /**
     * Returns the qualified name for the given package name and unqualified name. If packageName is
     * <code>null</code> or the empty String the unqualified name is returned.
     * 
     * @throws NullPointerException if unqualifiedName is <code>null</code>.
     */
    public static final String qualifiedName(String packageName, String unqualifiedName) {
        ArgumentCheck.notNull(unqualifiedName);
        if (packageName == null || StringUtils.isEmpty(packageName)) {
            return unqualifiedName;
        }
        return packageName + '.' + unqualifiedName;
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
     * Returns the lines of the given text as array. Each array item represents one line. The lines
     * don't contains the line separator.
     */
    public static final String[] getLines(String text, String lineSeparator) {
        List<String> lines = new ArrayList<String>();
        int start = 0;
        int end = text.indexOf(lineSeparator);
        while (end > 0) {
            lines.add(text.substring(start, end));
            start = end + lineSeparator.length();
            end = text.indexOf(lineSeparator, start);
        }
        lines.add(text.substring(start));
        return lines.toArray(new String[lines.size()]);
    }

    /**
     * Returns the line in the text that starts at the given position and ends at the next line
     * separator. The line separator itself is not returned as part of the line.
     */
    public static final String getLine(String text, int startPos, String lineSeparator) {
        int pos = text.indexOf(lineSeparator, startPos);
        if (pos == -1) {
            return text.substring(startPos);
        }
        return text.substring(startPos, pos);
    }

    /**
     * Returns the line separator provided by System.getProperty("line.separator").
     */
    public static String getSystemLineSeparator() {
        return System.getProperty("line.separator");
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
        if (text == null || text.equals("")) {
            return "";
        }
        String[] words = text.split(CAMEL_CASE_SEPERATORS);
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            char firstChar = word.charAt(0);
            if (firstUp) {
                firstChar = Character.toUpperCase(firstChar);
            } else {
                firstChar = Character.toLowerCase(firstChar);
            }
            result.append(firstChar).append(word.substring(1).toLowerCase());
            firstUp = true;
        }
        return result.toString();
    }

    /**
     * Returns a String where an occurrence of a character followed by an upper case character is
     * replaced by these characters divided by an underscore. Consecutive sequences of comma, dot,
     * hyphen and whitespace is also replaced by one single underscore.
     * <p/>
     * For example:
     * 
     * <pre>
     *  CamelCase      -&gt;  Camel_Case
     *  a.,- b-cdEF    -&gt;  a_b_cd_EF
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
     * <p/>
     * For example:
     * 
     * <pre>
     *  CamelCase      -&gt;  Camel_Case
     *  a.,- b-cdEF    -&gt;  a_b_cd_E_F       (splitUppercaseSequences = true)
     *  a.,- b-cdEF    -&gt;  a_b_cd_EF       (splitUppercaseSequences = false)
     * </pre>
     * 
     */
    public static String camelCaseToUnderscore(String text, boolean splitUppercaseSequences) {
        if (text == null || text.equals("")) {
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

        return result.toString();
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

}
