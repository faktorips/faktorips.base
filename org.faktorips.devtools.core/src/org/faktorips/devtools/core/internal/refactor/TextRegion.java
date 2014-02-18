/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.refactor;

/**
 * The TextRegion describes a certain range in an indexed String.
 * 
 */
public class TextRegion {

    private int startPoint;

    private int endPoint;

    public TextRegion(int startPoint, int endPoint) {
        super();
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public int getEndPoint() {
        return endPoint;
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
        return new TextRegion(startPoint + offset, endPoint + offset);
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
        return new TextRegion(startPoint + offset, endPoint);
    }

    /**
     * Creates a new text region thats end position is moved by the specified offset. The starting
     * position stays the same the ending position is <code>end + offset</code>.
     * 
     * @param offset The offset by which the end of the new {@link TextRegion} is moved.
     * @return The new {@link TextRegion} with the moved ending position
     */
    public TextRegion endOffset(int offset) {
        return new TextRegion(startPoint, endPoint + offset);
    }

    public String createFullRefactoredString(String completeIdentifierString, String newString) {
        if (!isValidStartAndEndPoint(completeIdentifierString)) {
            // invalid start ending Points should never happen
            return completeIdentifierString;
        }
        StringBuffer refactoredBuffer = new StringBuffer();
        refactoredBuffer.append(completeIdentifierString.substring(0, startPoint) + newString);
        refactoredBuffer.append(completeIdentifierString.substring(endPoint));
        return refactoredBuffer.toString();
    }

    private boolean isValidStartAndEndPoint(String completeIdentifierString) {
        if (getStartPoint() >= 0 && getEndPoint() <= completeIdentifierString.length()
                && getStartPoint() <= getEndPoint()) {
            return true;
        }
        return false;
    }

}
