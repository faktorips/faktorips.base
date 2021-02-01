/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

/**
 * Test enum with two values.
 * 
 * @author Jan Ortmann
 */
public class TestEnum {

    public static final TestEnum MONTH = new TestEnum("MONTH");
    public static final TestEnum YEAR = new TestEnum("YEAR");

    private String value;

    /**
     * 
     */
    public TestEnum(String s) {
        value = s;
    }

    @Override
    public String toString() {
        return value;
    }

}
