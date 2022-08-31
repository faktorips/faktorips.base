/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.util.Objects;

/**
 * The TextRegion describes a certain range in a String. The TextRegion is defined by the start and
 * end variables. The variables don't change over time.
 * 
 */
public class TextRegion implements Comparable<TextRegion> {

    private final String text;

    private final int start;

    private final int end;

    /**
     * Creates a new Instance.
     * 
     * @param text The complete identifier String
     * @param start The starting position
     * @param end The end position
     */
    public TextRegion(String text, int start, int end) {
        this.text = text;
        this.start = start;
        this.end = end;
    }

    public String getText() {
        return text;
    }

    /**
     * Returns the start position for an examined String
     * 
     * @return start
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the end position for an examined String
     * 
     * @return end
     */
    public int getEnd() {
        return end;
    }

    /**
     * Creates a new text region that is moved by the specified offset. The starting position of the
     * new text region is <code>start + offset</code> the ending position is at
     * <code>end + offset</code>.
     * 
     * @param offset The offset by which the new {@link TextRegion} is moved.
     * @return The new {@link TextRegion} moved by the offset
     */
    public TextRegion offset(int offset) {
        return new TextRegion(text, start + offset, end + offset);
    }

    /**
     * Creates a new text region thats start position is moved by the specified offset. The starting
     * position of the new text region is <code>start + offset</code> the ending position stays the
     * same.
     * 
     * @param offset The offset by which the start of the new {@link TextRegion} is moved.
     * @return The new {@link TextRegion} with the moved starting position
     */
    public TextRegion startOffset(int offset) {
        return new TextRegion(text, start + offset, end);
    }

    /**
     * Creates a new text region thats end position is moved by the specified offset. The starting
     * position stays the same the ending position is <code>end + offset</code>.
     * 
     * @param offset The offset by which the end of the new {@link TextRegion} is moved.
     * @return The new {@link TextRegion} with the moved ending position
     */
    public TextRegion endOffset(int offset) {
        return new TextRegion(text, start, end + offset);
    }

    /**
     * Replaces a part of the given input string with the replacement string. The region to be
     * replaced is defined by "start" and "end" values of this {@link TextRegion}. If the positions
     * are invalid, the method will return the input String without any changes.
     * 
     * @param inputString The string of which a region should be replaced
     * @param replacementString the string that replaces a region in the input string.
     * @return the resulting string
     */
    public String replaceTextRegion(String inputString, String replacementString) {
        if (!isValidStartAndEnd(inputString)) {
            return inputString;
        }
        return inputString.substring(0, getStart()) + replacementString + inputString.substring(getEnd());
    }

    private boolean isValidStartAndEnd(String completeIdentifierString) {
        if (isInitParametersValid() && getEnd() <= completeIdentifierString.length()) {
            return true;
        }
        return false;
    }

    private boolean isInitParametersValid() {
        if (getStart() >= 0 && getStart() <= getEnd()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the substring that is defined by this text region within the input string. If the
     * text region is invalid the text is returned without any changes.
     * <p>
     * For example in string "abc123" you get the following results:
     * <ul>
     * <li>Text region 1 to 5: <strong>bc12</strong></li>
     * <li>Text region 1 to -5: <strong>abc123</strong></li>
     * </ul>
     * 
     * @return The string that is the defined substring of the input string.
     */
    public String getTextRegionString() {
        if (!isValidStartAndEnd(text)) {
            return text;
        }
        return text.substring(getStart(), getEnd());
    }

    /**
     * Returns whether the given char is at the position <code>start + offset</code>.
     * 
     * @param offset The offset that is added to the start position
     * @param expected The expected char at the position <code>start + offset</code>
     */
    public boolean isRelativeChar(int offset, char expected) {
        int index = start + offset;
        return index >= 0 && index < text.length() && text.charAt(index) == expected;
    }

    @Override
    public int compareTo(TextRegion o) {
        if (!Objects.equals(text, o.text)) {
            return text.compareTo(o.text);
        }
        int compareStart = getStart() - o.getStart();
        if (compareStart == 0) {
            return getEnd() - o.getEnd();
        } else {
            return compareStart;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(end, start, text);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        TextRegion other = (TextRegion)obj;
        return (end == other.end)
                && (start == other.start)
                && Objects.equals(text, other.text);
    }

    @Override
    public String toString() {
        return "TextRegion [text=" + text + ", start=" + start + ", end=" + end + "]"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

}
