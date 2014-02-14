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
