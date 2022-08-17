/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.text;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * NOTE: This class is a copy of the corresponding internal Eclipse class. It is copied as the
 * class' package has changed from Eclipse version 3.2 to 3.3.
 */
public class HTMLTextPresenter implements DefaultInformationControl.IInformationPresenter,
        DefaultInformationControl.IInformationPresenterExtension {

    private static final String LINE_DELIM = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

    private int fCounter;
    private boolean fEnforceUpperLineLimit;

    public HTMLTextPresenter(boolean enforceUpperLineLimit) {
        super();
        fEnforceUpperLineLimit = enforceUpperLineLimit;
    }

    public HTMLTextPresenter() {
        this(true);
    }

    protected Reader createReader(String hoverInfo, TextPresentation presentation) {
        return new HTML2TextReader(new StringReader(hoverInfo), presentation);
    }

    protected void adaptTextPresentation(TextPresentation presentation, int offset, int insertLength) {
        int yoursStart = offset;
        int yoursEnd = offset + insertLength - 1;
        yoursEnd = Math.max(yoursStart, yoursEnd);

        Iterator<?> e = presentation.getAllStyleRangeIterator();
        while (e.hasNext()) {

            StyleRange range = (StyleRange)e.next();

            int myStart = range.start;
            int myEnd = range.start + range.length - 1;
            myEnd = Math.max(myStart, myEnd);

            if (myEnd < yoursStart) {
                continue;
            }

            if (myStart < yoursStart) {
                range.length += insertLength;
            } else {
                range.start += insertLength;
            }
        }
    }

    private void append(StringBuilder sb, String string, TextPresentation presentation) {
        int length = string.length();
        sb.append(string);

        if (presentation != null) {
            adaptTextPresentation(presentation, fCounter, length);
        }

        fCounter += length;
    }

    private String getIndent(String line) {
        int length = line.length();

        int i = 0;
        while (i < length && Character.isWhitespace(line.charAt(i))) {
            ++i;
        }

        return (i == length ? line : line.substring(0, i)) + " "; //$NON-NLS-1$
    }

    @Override
    public String updatePresentation(Display display,
            String hoverInfo,
            TextPresentation presentation,
            int maxWidth,
            int maxHeight) {
        return updatePresentation((Drawable)display, hoverInfo, presentation, maxWidth, maxHeight);
    }

    @Override
    public String updatePresentation(Drawable drawable,
            String hoverInfo,
            TextPresentation presentation,
            int maxWidth,
            int maxHeight) {

        if (hoverInfo == null) {
            return null;
        }

        GC gc = new GC(drawable);
        try {
            StringBuilder sb = new StringBuilder();
            int maxNumberOfLines = Math.round(maxHeight / gc.getFontMetrics().getHeight());

            fCounter = 0;
            LineBreakingReader reader = new LineBreakingReader(createReader(hoverInfo, presentation), gc, maxWidth);

            boolean lastLineFormatted = false;
            String lastLineIndent = null;

            String line = reader.readLine();
            boolean lineFormatted = reader.isFormattedLine();
            boolean firstLineProcessed = false;

            while (line != null) {
                if (fEnforceUpperLineLimit && maxNumberOfLines <= 0) {
                    break;
                }

                if (firstLineProcessed) {
                    if (!lastLineFormatted) {
                        append(sb, LINE_DELIM, null);
                    } else {
                        append(sb, LINE_DELIM, presentation);
                        if (lastLineIndent != null) {
                            append(sb, lastLineIndent, presentation);
                        }
                    }
                }

                append(sb, line, null);
                firstLineProcessed = true;

                lastLineFormatted = lineFormatted;
                if (!lineFormatted) {
                    lastLineIndent = null;
                } else if (lastLineIndent == null) {
                    lastLineIndent = getIndent(line);
                }

                line = reader.readLine();
                lineFormatted = reader.isFormattedLine();

                maxNumberOfLines--;
            }

            if (line != null) {
                append(sb, LINE_DELIM, lineFormatted ? presentation : null);
                append(sb, HTMLMessages.getString("HTMLTextPresenter.ellipse"), presentation); //$NON-NLS-1$
            }

            return trim(sb, presentation);

        } catch (IOException e) {
            return null;

        } finally {
            gc.dispose();
        }
    }

    private String trim(StringBuilder sb, TextPresentation presentation) {
        int length = sb.length();

        int end = length - 1;
        while (end >= 0 && Character.isWhitespace(sb.charAt(end))) {
            --end;
        }

        if (end == -1) {
            return ""; //$NON-NLS-1$
        }

        if (end < length - 1) {
            sb.delete(end + 1, length);
        } else {
            end = length;
        }

        int start = 0;
        while (start < end && Character.isWhitespace(sb.charAt(start))) {
            ++start;
        }

        sb.delete(0, start);
        presentation.setResultWindow(new Region(start, sb.length()));
        return sb.toString();
    }

}
