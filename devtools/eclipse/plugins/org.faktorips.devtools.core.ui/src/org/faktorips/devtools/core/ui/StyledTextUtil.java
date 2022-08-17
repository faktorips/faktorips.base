/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;

/**
 * Convenience wrapper for {@link StyledText}. Allows to easily add formatted text blocks to a
 * widget. CAVEAT: This class has no authority over changes to the referenced widget from other
 * sources.
 * 
 * @author NKammerer
 */
public class StyledTextUtil {

    private static final String WIDGET_NULL_WARNING = "StyledText widget must not be null"; //$NON-NLS-1$

    protected StyledTextUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the widget's content to empty.
     */
    public static void clear(StyledText targetWidget) {
        Objects.requireNonNull(targetWidget, WIDGET_NULL_WARNING);
        targetWidget.setText(StringUtils.EMPTY);
    }

    /**
     * Append text without formatting
     * 
     * @param text The String to append.
     */
    public static void appendPlain(StyledText targetWidget, String text) {
        Objects.requireNonNull(targetWidget, WIDGET_NULL_WARNING);
        targetWidget.append(text);
    }

    /**
     * Appends newline characters, if it's not the beginning of the content.
     * 
     * @param text Is added after the line break!
     */
    public static void appendLinePlain(StyledText targetWidget, String text) {
        Objects.requireNonNull(targetWidget, WIDGET_NULL_WARNING);
        if (targetWidget.getCharCount() > 0) {
            targetWidget.append(System.lineSeparator());
        }
        appendPlain(targetWidget, text);
    }

    public static void appendNewLine(StyledText targetWidget) {
        Objects.requireNonNull(targetWidget, WIDGET_NULL_WARNING);
        appendLinePlain(targetWidget, StringUtils.EMPTY);
    }

    /**
     * Appends newline characters, if it's not the beginning of the content.
     * 
     * @param text Is added with formatting after the line break!
     * @param fontStyle SWT formatting style to configure {@link StyleRange}
     */
    public static void appendLineStyled(StyledText targetWidget, String text, int fontStyle) {
        Objects.requireNonNull(targetWidget, WIDGET_NULL_WARNING);
        if (targetWidget.getCharCount() > 0) {
            targetWidget.append(System.lineSeparator());
        }
        appendStyled(targetWidget, text, fontStyle);
    }

    /**
     * Append text with custom formatting
     * 
     * @param text A string to append
     * @param fontStyle SWT formatting style to configure {@link StyleRange}
     */
    public static void appendStyled(StyledText targetWidget, String text, int fontStyle) {
        Objects.requireNonNull(targetWidget, WIDGET_NULL_WARNING);
        // get current index, set text, adjust style object and return style
        int currentStart = targetWidget.getCharCount();
        targetWidget.append(text);

        StyleRange style = new StyleRange(currentStart, text.length(), null, null, fontStyle);
        targetWidget.setStyleRange(style);
    }
}
