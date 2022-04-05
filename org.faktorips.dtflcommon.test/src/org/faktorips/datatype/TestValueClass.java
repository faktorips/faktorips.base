/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

/**
 * A value class to test the generic value datatype.
 * 
 * @author Jan Ortmann
 */
public class TestValueClass {

    private TestValueClass() {
        super();
    }

    public static final Integer getInteger(String s) {
        return Integer.valueOf(s);
    }

    public static final boolean isInteger(String s) {
        try {
            Integer.valueOf(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
