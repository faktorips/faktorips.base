/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.text;

import java.io.IOException;
import java.io.Reader;

/**
 * Reads the text contents from a reader and computes for each character a potential substitution.
 * The substitution may eat more characters than only the one passed into the computation routine.
 * <p>
 * Moved into this package from <code>org.eclipse.jface.internal.text.revisions</code>.
 * <p>
 * NOTE: This class is a copy of the corresponding internal Eclipse class. It is copied as the
 * class' package has changed from Eclipse version 3.2 to 3.3.
 */
public abstract class SubstitutionTextReader extends SingleCharReader {

    protected static final String LINE_DELIM = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

    private Reader fReader;
    protected boolean fWasWhiteSpace;
    private int fCharAfterWhiteSpace;

    /**
     * Tells whether white space characters are skipped.
     */
    private boolean fSkipWhiteSpace = true;

    private boolean fReadFromBuffer;
    private StringBuffer fBuffer;
    private int fIndex;

    protected SubstitutionTextReader(Reader reader) {
        fReader = reader;
        fBuffer = new StringBuffer();
        fIndex = 0;
        fReadFromBuffer = false;
        fCharAfterWhiteSpace = -1;
        fWasWhiteSpace = true;
    }

    /**
     * Computes the substitution for the given character and if necessary subsequent characters.
     * Implementation should use <code>nextChar</code> to read subsequent characters.
     * 
     * @param c the character to be substituted
     * @return the substitution for <code>c</code>
     * @throws IOException in case computing the substitution fails
     */
    protected abstract String computeSubstitution(int c) throws IOException;

    /**
     * Returns the internal reader.
     * 
     * @return the internal reader
     */
    protected Reader getReader() {
        return fReader;
    }

    /**
     * Returns the next character.
     * 
     * @return the next character
     * @throws IOException in case reading the character fails
     */
    protected int nextChar() throws IOException {
        fReadFromBuffer = (fBuffer.length() > 0);
        if (fReadFromBuffer) {
            char ch = fBuffer.charAt(fIndex++);
            if (fIndex >= fBuffer.length()) {
                fBuffer.setLength(0);
                fIndex = 0;
            }
            return ch;
        }

        int ch = fCharAfterWhiteSpace;
        if (ch == -1) {
            ch = fReader.read();
        }
        if (fSkipWhiteSpace && Character.isWhitespace((char)ch)) {
            do {
                ch = fReader.read();
            } while (Character.isWhitespace((char)ch));
            if (ch != -1) {
                fCharAfterWhiteSpace = ch;
                return ' ';
            }
        } else {
            fCharAfterWhiteSpace = -1;
        }
        return ch;
    }

    @Override
    public int read() throws IOException {
        int c;
        do {
            c = nextChar();
            while (!fReadFromBuffer) {
                String s = computeSubstitution(c);
                if (s == null) {
                    break;
                }
                if (s.length() > 0) {
                    fBuffer.insert(0, s);
                }
                c = nextChar();
            }
        } while (fSkipWhiteSpace && fWasWhiteSpace && (c == ' '));
        fWasWhiteSpace = (c == ' ' || c == '\r' || c == '\n');
        return c;
    }

    @Override
    public boolean ready() throws IOException {
        return fReader.ready();
    }

    @Override
    public void close() throws IOException {
        fReader.close();
    }

    @Override
    public void reset() throws IOException {
        fReader.reset();
        fWasWhiteSpace = true;
        fCharAfterWhiteSpace = -1;
        fBuffer.setLength(0);
        fIndex = 0;
    }

    protected final void setSkipWhitespace(boolean state) {
        fSkipWhiteSpace = state;
    }

    protected final boolean isSkippingWhitespace() {
        return fSkipWhiteSpace;
    }
}
