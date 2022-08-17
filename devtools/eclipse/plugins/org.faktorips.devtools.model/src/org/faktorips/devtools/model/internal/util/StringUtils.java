/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.util;

/**
 * A utility class for strings.
 * 
 * @author Peter Erzberger
 */
public class StringUtils {

    public static final String QUOTE = "\""; //$NON-NLS-1$

    private StringUtils() {
        // Utility class not to be instantiated.
    }

    /**
     * Wraps and returns the provide text according to the split length and tab size.
     * 
     * @param text the text that needs to be wrapped up
     * @param length the length after which a line break should occur
     * @param lineSeparator the line separator that is used for the wrapped text
     */
    public static final String wrapText(String text, int length, String lineSeparator) {
        if (text == null || "".equals(text)) { //$NON-NLS-1$
            return text;
        }
        String[] lines = text.split(lineSeparator);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String lineText = lines[i];
            while (lineText.length() > length) {
                int index = lineText.indexOf(' ', length);
                if (index != -1) {
                    sb.append(lineText.substring(0, index));
                    if (lineText.length() > index + 1) {
                        sb.append(lineSeparator);
                        lineText = lineText.substring(index + 1, lineText.length());
                    } else {
                        lineText = ""; //$NON-NLS-1$
                        break;
                    }
                } else {
                    break;
                }
            }
            sb.append(lineText);
            if (i + 1 < lines.length && !"".equals(lines[i + 1])) { //$NON-NLS-1$
                sb.append(lineSeparator);
            }
        }
        return sb.toString();
    }

    /**
     * Returns the given name with a "copyOf_" and counter prefix that can be used to make the name
     * unique.
     * <p>
     * Example:
     * <table border="1">
     * <caption>Example:</caption>
     * <tr>
     * <th>uniqueCopyOfCounter</th>
     * <th>nameCandidate</th>
     * <th>result</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>Test</td>
     * <td>CopyOf_Test</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>Test</td>
     * <td>CopyOf_(2)_Test</td>
     * </tr>
     * </table>
     */
    public static String computeCopyOfName(int uniqueCopyOfCounter, String nameCandidate) {
        String uniqueCopyOfCounterText = uniqueCopyOfCounter == 0 ? "" : "(" + (uniqueCopyOfCounter + 1) + ")_"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String nameWithoutCopyOfPrefix;
        if (nameCandidate.startsWith(Messages.StringUtils_copyOfNamePrefix)) {
            // Remove copyOf from the name.
            nameWithoutCopyOfPrefix = org.apache.commons.lang.StringUtils.substringAfter(nameCandidate,
                    Messages.StringUtils_copyOfNamePrefix);
            // Remove copyOf counter prefix e.g. "(2)_" if it exists.
            nameWithoutCopyOfPrefix = nameWithoutCopyOfPrefix.replaceAll("^\\([0-9]*\\)_", ""); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            nameWithoutCopyOfPrefix = nameCandidate;
        }
        // Add new copyOf and counter prefix.
        return Messages.StringUtils_copyOfNamePrefix + uniqueCopyOfCounterText + nameWithoutCopyOfPrefix;
    }

    /**
     * Returns the text in quotes (e.g. "anyText"). Does not add additional quotes if the given text
     * already starts or ends with a quote (").
     * 
     * @param text the text to surround with quotes (").
     */
    public static String quote(String text) {
        StringBuilder sb = new StringBuilder();
        if (text == null || !text.startsWith(QUOTE)) {
            sb.append(QUOTE);
        }
        sb.append(text);
        if (text == null || !text.endsWith(QUOTE)) {
            sb.append(QUOTE);
        }

        return sb.toString();
    }

}
