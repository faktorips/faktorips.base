/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import java.util.StringTokenizer;

import org.apache.commons.lang.SystemUtils;

/**
 * Represents a language independant sourcecode fragment. A sourcecode fragment consists of the
 * sourcecode text and possibly additional parts like import statements. The default implementation
 * is the {@link JavaCodeFragment}.
 * 
 * @see JavaCodeFragmentBuilder
 */
public class CodeFragment {

    private static final String INDENT_HELPER = "                                                         "; //$NON-NLS-1$

    // true if lines after a call to appendOpenBracket() are indented or not
    // the default is false, as Faktor-IPS formats the generated sourcecode via the Eclipse
    // formatter.
    private boolean indent = false;

    // buffer holding the sourcecode text
    private StringBuilder sourcecode;

    // number of blanks used for indentation
    private int indentation = 4;

    // the indentation level at the end of the sourcecode
    private int indentLevel = 0;

    /**
     * Creates a new empty JavaCodeFragment.
     */
    public CodeFragment() {
        this(false);
    }

    public CodeFragment(boolean indent) {
        sourcecode = new StringBuilder(1024);
        this.indent = indent;
    }

    /**
     * Creates a new CodeFragment with the indicated source code.
     */
    public CodeFragment(String sourcecode) {
        this();
        this.sourcecode = new StringBuilder(1024);
        this.sourcecode.append(sourcecode);
    }

    /**
     * Copy constructor.
     */
    public CodeFragment(CodeFragment fragment) {
        sourcecode = fragment.sourcecode;
    }

    /**
     * Returns the sourcecode.
     */
    public String getSourcecode() {
        return sourcecode.toString();
    }

    /**
     * Increases the indentation level used for appending sourcecode.
     */
    public void incIndentationLevel() {
        indentLevel++;
    }

    /**
     * Decreases the indentation level used for appending sourcecode.
     * 
     * @throws RuntimeException if the level is 0.
     */
    public void decIndentationLevel() {
        if (indentLevel == 0) {
            throw new RuntimeException("Indentation level can't be lesser than 0."); //$NON-NLS-1$
        }
        indentLevel--;
    }

    /**
     * Returns the current indentation level at the end of the sourcecode.
     */
    public int getIndentationLevel() {
        return indentLevel;
    }

    /**
     * Appends the given String to the sourcecode.
     */
    public CodeFragment append(String s) {
        indentIfBol();
        sourcecode.append(s);
        return this;
    }

    /**
     * Encloses the given String with doublequotes (") and appends it to the sourcecode.
     */
    public CodeFragment appendQuoted(String s) {
        append('"');
        append(s);
        append('"');
        return this;
    }

    /**
     * Appends the given char to the sourcecode.
     */
    public CodeFragment append(char c) {
        if (indent) {
            indentIfBol();
        }
        sourcecode.append(c);
        return this;
    }

    /**
     * Appends a line separator to the sourcecode.
     */
    public CodeFragment appendln() {
        sourcecode.append(SystemUtils.LINE_SEPARATOR);
        return this;
    }

    /**
     * Appends the given String and a line separator to the sourcecode.
     */
    public CodeFragment appendln(String s) {
        if (indent) {
            indentIfBol();
        }
        indentIfBol();
        sourcecode.append(s);
        sourcecode.append(SystemUtils.LINE_SEPARATOR);
        return this;
    }

    /**
     * Appends the given String as is to the sourcecode without indenting it.
     */
    public CodeFragment appendlnUnindented(String arg) {
        sourcecode.append(arg);
        sourcecode.append(SystemUtils.LINE_SEPARATOR);
        return this;
    }

    /**
     * Appends the given char to the sourcecode.
     */
    public CodeFragment appendln(char c) {
        indentIfBol();
        sourcecode.append(c);
        sourcecode.append(SystemUtils.LINE_SEPARATOR);
        return this;
    }

    /**
     * Appends the given fragment to his fragment and indents it properly.
     */
    public CodeFragment append(CodeFragment fragment) {
        if (!indent) {
            sourcecode.append(fragment.sourcecode);
        } else {
            if (indent) {
                String sourcecode = fragment.getSourcecode();
                StringTokenizer tokenizer = new StringTokenizer(sourcecode, SystemUtils.LINE_SEPARATOR);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if (tokenizer.hasMoreTokens()) {
                        appendln(token);
                    } else {
                        append(token);
                    }
                }
                if (sourcecode.endsWith(SystemUtils.LINE_SEPARATOR)) {
                    appendln(""); //$NON-NLS-1$
                }
            }
        }
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourcecode == null) ? 0 : sourcecode.hashCode());
        return result;
    }

    /**
     * Two fragments are equal if they contain the same sourcecode and have the same import
     * declaration.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CodeFragment other = (CodeFragment)obj;
        if (sourcecode == null) {
            if (other.sourcecode != null) {
                return false;
            }
        } else if (!sourcecode.toString().equals(other.sourcecode.toString())) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the last character in the sourcecode is a line separator and so any text
     * appended to the sourcecode goes to a new line (bol = begin of line).
     */
    public boolean bol() {
        int length = sourcecode.length();
        if (length == 0) {
            return true;
        }
        if (SystemUtils.LINE_SEPARATOR.length() == 1) {
            return sourcecode.charAt(length - 1) == SystemUtils.LINE_SEPARATOR.charAt(0);
        }
        if (SystemUtils.LINE_SEPARATOR.length() == 2) {
            if (length == 1) {
                return false;
            }
            return (sourcecode.charAt(length - 2) == SystemUtils.LINE_SEPARATOR.charAt(0))
                    && (sourcecode.charAt(length - 1) == SystemUtils.LINE_SEPARATOR.charAt(1));
        }
        throw new RuntimeException("Unknown line separator [" + SystemUtils.LINE_SEPARATOR + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the CodeFragment as String.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return sourcecode.toString();
    }

    /*
     * Appends the proper indentation if the sourcecode ens with a line separator.
     */
    private void indentIfBol() {
        if (!indent) {
            return;
        }
        if (bol()) {
            int length = indentation * indentLevel;
            if (length < INDENT_HELPER.length()) {
                sourcecode.append(INDENT_HELPER.substring(0, length));
            } else {
                sourcecode.append(INDENT_HELPER);
            }
        }
    }
}
