/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.util;

/**
 * A utility class for strings.
 * 
 * @author Peter Erzberger
 */
public class StringUtils {

    /**
     * Wraps and returns the provide text according to the split length and tab size.
     * 
     * @param text the text that needs to be wrapped up
     * @param length the length after which a line break should occur
     * @param lineSeparator the line separator that is used for the wrapped text
     */
    public final static String wrapText(String text, int length, String lineSeparator) {
        if (text == null || "".equals(text)) { //$NON-NLS-1$
            return text;
        }
        String[] lines = text.split(lineSeparator);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            String lineText = lines[i];
            while (lineText.length() > length) {
                int index = lineText.indexOf(' ', length);
                if (index != -1) {
                    buf.append(lineText.substring(0, index));
                    if (lineText.length() > index + 1) {
                        buf.append(lineSeparator);
                        lineText = lineText.substring(index + 1, lineText.length());
                    } else {
                        lineText = ""; //$NON-NLS-1$
                        break;
                    }
                } else {
                    break;
                }
            }
            buf.append(lineText);
            if (i + 1 < lines.length && !"".equals(lines[i + 1])) { //$NON-NLS-1$
                buf.append(lineSeparator);
            }
        }
        return buf.toString();
    }

    /**
     * Returns the given name with a "copyOf_" and counter prefix that can be used to make the name
     * unique.
     * <p>
     * Example:
     * <table border="true">
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
        nameCandidate = Messages.StringUtils_copyOfNamePrefix + uniqueCopyOfCounterText + nameWithoutCopyOfPrefix;
        return nameCandidate;
    }

    private StringUtils() {
        // Utility class not to be instantiated.
    }

}
