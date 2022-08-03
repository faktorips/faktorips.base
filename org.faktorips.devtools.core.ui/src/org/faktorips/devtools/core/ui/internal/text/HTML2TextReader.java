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
import java.io.PushbackReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

/**
 * Reads the text contents from a reader of HTML contents and translates the tags or cut them out.
 * <p>
 * NOTE: This class is a copy of the corresponding internal Eclipse class. It is copied as the
 * class' package has changed from Eclipse version 3.2 to 3.3.
 */
public class HTML2TextReader extends SubstitutionTextReader {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$
    private static final Map<String, String> FG_ENTITY_LOOKUP;
    private static final Set<String> FG_TAGS;

    static {
        FG_TAGS = new HashSet<>();
        FG_TAGS.add("b"); //$NON-NLS-1$
        FG_TAGS.add("br"); //$NON-NLS-1$
        FG_TAGS.add("br/"); //$NON-NLS-1$
        FG_TAGS.add("div"); //$NON-NLS-1$
        FG_TAGS.add("h1"); //$NON-NLS-1$
        FG_TAGS.add("h2"); //$NON-NLS-1$
        FG_TAGS.add("h3"); //$NON-NLS-1$
        FG_TAGS.add("h4"); //$NON-NLS-1$
        FG_TAGS.add("h5"); //$NON-NLS-1$
        FG_TAGS.add("p"); //$NON-NLS-1$
        FG_TAGS.add("dl"); //$NON-NLS-1$
        FG_TAGS.add("dt"); //$NON-NLS-1$
        FG_TAGS.add("dd"); //$NON-NLS-1$
        FG_TAGS.add("li"); //$NON-NLS-1$
        FG_TAGS.add("ul"); //$NON-NLS-1$
        FG_TAGS.add("pre"); //$NON-NLS-1$
        FG_TAGS.add("head"); //$NON-NLS-1$

        FG_ENTITY_LOOKUP = new HashMap<>(7);
        FG_ENTITY_LOOKUP.put("lt", "<"); //$NON-NLS-1$ //$NON-NLS-2$
        FG_ENTITY_LOOKUP.put("gt", ">"); //$NON-NLS-1$ //$NON-NLS-2$
        FG_ENTITY_LOOKUP.put("nbsp", " "); //$NON-NLS-1$ //$NON-NLS-2$
        FG_ENTITY_LOOKUP.put("amp", "&"); //$NON-NLS-1$ //$NON-NLS-2$
        FG_ENTITY_LOOKUP.put("circ", "^"); //$NON-NLS-1$ //$NON-NLS-2$
        FG_ENTITY_LOOKUP.put("tilde", "~"); //$NON-NLS-2$ //$NON-NLS-1$
        FG_ENTITY_LOOKUP.put("quot", "\""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private int fCounter = 0;
    private TextPresentation fTextPresentation;
    private int fBold = 0;
    private int fStartOffset = -1;
    private boolean fInParagraph = false;
    private boolean fIsPreformattedText = false;
    private boolean fIgnore = false;
    private boolean fHeaderDetected = false;

    /**
     * Transforms the HTML text from the reader to formatted text.
     * 
     * @param reader the reader
     * @param presentation If not <code>null</code>, formattings will be applied to the
     *            presentation.
     */
    public HTML2TextReader(Reader reader, TextPresentation presentation) {
        super(new PushbackReader(reader));
        fTextPresentation = presentation;
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c != -1) {
            ++fCounter;
        }
        return c;
    }

    protected void startBold() {
        if (fBold == 0) {
            fStartOffset = fCounter;
        }
        ++fBold;
    }

    protected void startPreformattedText() {
        fIsPreformattedText = true;
        setSkipWhitespace(false);
    }

    protected void stopPreformattedText() {
        fIsPreformattedText = false;
        setSkipWhitespace(true);
    }

    protected void stopBold() {
        --fBold;
        if (fBold == 0) {
            if (fTextPresentation != null) {
                fTextPresentation.addStyleRange(new StyleRange(fStartOffset, fCounter - fStartOffset, null, null,
                        SWT.BOLD));
            }
            fStartOffset = -1;
        }
    }

    @Override
    protected String computeSubstitution(int c) throws IOException {
        if (c == '<') {
            return processHTMLTag();
        } else if (fIgnore) {
            return EMPTY_STRING;
        } else if (c == '&') {
            return processEntity();
        } else if (fIsPreformattedText) {
            return processPreformattedText(c);
        }
        return null;
    }

    private String html2Text(String html) {
        if (html == null || html.length() == 0) {
            return EMPTY_STRING;
        }

        html = html.toLowerCase();

        String tag = html;
        if ('/' == tag.charAt(0)) {
            tag = tag.substring(1);
        }

        if (!FG_TAGS.contains(tag)) {
            return EMPTY_STRING;
        }

        if ("pre".equals(html)) { //$NON-NLS-1$
            startPreformattedText();
            return EMPTY_STRING;
        }

        if ("/pre".equals(html)) { //$NON-NLS-1$
            stopPreformattedText();
            return EMPTY_STRING;
        }

        if (fIsPreformattedText) {
            return EMPTY_STRING;
        }

        if ("b".equals(html)) { //$NON-NLS-1$
            startBold();
            return EMPTY_STRING;
        }

        if ((html.length() > 1 && html.charAt(0) == 'h' && Character.isDigit(html.charAt(1))) || "dt".equals(html)) { //$NON-NLS-1$
            startBold();
            return EMPTY_STRING;
        }

        if ("dl".equals(html)) { //$NON-NLS-1$
            return LINE_DELIM;
        }

        if ("dd".equals(html)) { //$NON-NLS-1$
            return "\t"; //$NON-NLS-1$
        }

        if ("li".equals(html)) { //$NON-NLS-1$
            // FIXME: this hard-coded prefix does not work for RTL languages, see
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=91682
            return LINE_DELIM + HTMLMessages.getString("HTML2TextReader.listItemPrefix"); //$NON-NLS-1$
        }

        if ("/b".equals(html)) { //$NON-NLS-1$
            stopBold();
            return EMPTY_STRING;
        }

        if ("p".equals(html)) { //$NON-NLS-1$
            fInParagraph = true;
            return LINE_DELIM;
        }

        if ("br".equals(html) || "br/".equals(html) || "div".equals(html)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return LINE_DELIM;
        }

        if ("/p".equals(html)) { //$NON-NLS-1$
            boolean inParagraph = fInParagraph;
            fInParagraph = false;
            return inParagraph ? EMPTY_STRING : LINE_DELIM;
        }

        if ((html.startsWith("/h") && html.length() > 2 && Character.isDigit(html.charAt(2))) || "/dt".equals(html)) { //$NON-NLS-1$ //$NON-NLS-2$
            stopBold();
            return LINE_DELIM;
        }

        if ("/dd".equals(html)) { //$NON-NLS-1$
            return LINE_DELIM;
        }

        if ("head".equals(html) && !fHeaderDetected) { //$NON-NLS-1$
            fHeaderDetected = true;
            fIgnore = true;
            return EMPTY_STRING;
        }

        if ("/head".equals(html) && fHeaderDetected && fIgnore) { //$NON-NLS-1$
            fIgnore = false;
        }

        return EMPTY_STRING;
    }

    /**
     * A '<' has been read. Process a html tag
     */
    private String processHTMLTag() throws IOException {

        StringBuilder sb = new StringBuilder();
        int ch;
        do {
            ch = nextChar();
            while (ch != -1 && ch != '>') {
                sb.append(Character.toLowerCase((char)ch));
                ch = nextChar();
                if (ch == '"') {
                    sb.append(Character.toLowerCase((char)ch));
                    ch = nextChar();
                    while (ch != -1 && ch != '"') {
                        sb.append(Character.toLowerCase((char)ch));
                        ch = nextChar();
                    }
                }
                if (ch == '<') {
                    unread(ch);
                    return '<' + sb.toString();
                }
            }

            if (ch == -1) {
                return null;
            }

            int tagLen = sb.length();
            // needs special treatment for comments
            if ((tagLen >= 3 && "!--".equals(sb.substring(0, 3))) //$NON-NLS-1$
                    && !(tagLen >= 5 && "--".equals(sb.substring(tagLen - 2)))) { //$NON-NLS-1$
                // unfinished comment
                sb.append(ch);
            } else {
                break;
            }
        } while (true);

        return html2Text(sb.toString());
    }

    private String processPreformattedText(int c) {
        if (c == '\r' || c == '\n') {
            fCounter++;
        }
        return null;
    }

    private void unread(int ch) throws IOException {
        ((PushbackReader)getReader()).unread(ch);
    }

    protected String entity2Text(String symbol) {
        if (symbol.length() > 1 && symbol.charAt(0) == '#') {
            int ch;
            try {
                if (symbol.charAt(1) == 'x') {
                    ch = Integer.parseInt(symbol.substring(2), 16);
                } else {
                    ch = Integer.parseInt(symbol.substring(1), 10);
                }
                return EMPTY_STRING + (char)ch;
            } catch (NumberFormatException e) {
                // ignore exception
            }
        } else {
            String str = FG_ENTITY_LOOKUP.get(symbol);
            if (str != null) {
                return str;
            }
        }
        return "&" + symbol; // not found //$NON-NLS-1$
    }

    /**
     * A '&' has been read. Process a entity
     */
    private String processEntity() throws IOException {
        StringBuilder buf = new StringBuilder();
        int ch = nextChar();
        while (Character.isLetterOrDigit((char)ch) || ch == '#') {
            buf.append((char)ch);
            ch = nextChar();
        }

        if (ch == ';') {
            return entity2Text(buf.toString());
        }

        buf.insert(0, '&');
        if (ch != -1) {
            buf.append((char)ch);
        }
        return buf.toString();
    }
}
