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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.faktorips.runtime.internal.IpsStringUtils;

public class AlphaNumericComparator implements Comparator<String>, Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4934515714500740839L;

    @Override
    public int compare(String o1, String o2) {
        return new AlphaNumericCompare(o1, o2).compare();
    }

    private static class AlphaNumericCompare {

        private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+"); //$NON-NLS-1$

        private final String input1;

        private final String input2;

        private final Matcher input1Matcher;

        private final Matcher input2Matcher;

        public AlphaNumericCompare(String input1, String input2) {
            this.input1 = input1;
            this.input2 = input2;

            input1Matcher = NUMERIC_PATTERN.matcher(input1);
            input2Matcher = NUMERIC_PATTERN.matcher(input2);
        }

        public int compare() {
            return compareAlphabeticPart(0, 0);
        }

        /**
         * Tries to find alphanumeric parts beginning from starting position and compares both
         * parts. If they are equal it delegates to {@link #compareNumericPart()} to check further
         * numeric parts. If they differ the compare result is returns. The starting position of the
         * both compared strings may differ because there may be numbers with leading zeros compared
         * with those without leading zeroes.
         * 
         * @see #compareNumericPart()
         */
        private int compareAlphabeticPart(int prevPosition1, int prevPosition2) {
            if (!input1Matcher.find() || !input2Matcher.find() || input1Matcher.start() != input2Matcher.start()) {
                return input1.substring(prevPosition1).compareTo(input2.substring(prevPosition2));
            }
            int startNum = input1Matcher.start();
            String alphaPart1 = input1.substring(prevPosition1, startNum);
            String alphaPart2 = input2.substring(prevPosition2, startNum);
            int alphaResult = alphaPart1.compareTo(alphaPart2);
            if (alphaResult == 0) {
                return compareNumericPart();
            } else {
                return alphaResult;
            }
        }

        /**
         * Tries to find some numeric part and compares these numbers to each other. Needs to be
         * called after {@link #compareAlphabeticPart(int, int)} because it has called the find
         * methods for the matchers. If both numbers are the same it tries to find further
         * alphabetical parts by delegating to {@link #compareAlphabeticPart(int, int)}.
         * <p>
         * A special case are numbers with leading zeros. For example the string "a1a" should be
         * smaller than "a01b". Because of this special part the length of both string may differ
         * and we need to track the parsing position of both strings separately. When both strings
         * are equals except of a leading zero, the numbers (starting with the last number) are
         * compared as strings. For example:
         * <ul>
         * <li>a01 < a1</li>
         * <li>a1xxx01 < a0001xxx1</li>
         * </ul>
         */
        private int compareNumericPart() {
            String numPart1 = input1Matcher.group();
            String numPart2 = input2Matcher.group();
            if (IpsStringUtils.isEmpty(numPart1) || IpsStringUtils.isEmpty(numPart2)) {
                return numPart1.compareTo(numPart2);
            } else {
                return compareAsNumbers(numPart1, numPart2);
            }
        }

        private int compareAsNumbers(String numPart1, String numPart2) {
            BigInteger num1 = new BigInteger(numPart1);
            BigInteger num2 = new BigInteger(numPart2);
            if (num1.equals(num2)) {
                int furtherCompare = compareAlphabeticPart(input1Matcher.end(), input2Matcher.end());
                if (furtherCompare == 0) {
                    return numPart1.compareTo(numPart2);
                } else {
                    return furtherCompare;
                }
            } else {
                return num1.compareTo(num2);
            }
        }

    }
}
