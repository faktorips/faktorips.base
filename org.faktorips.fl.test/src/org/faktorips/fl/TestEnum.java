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

package org.faktorips.fl;

/**
 * Test enum with two values.
 * 
 * @author Jan Ortmann
 */
public class TestEnum {

    public final static TestEnum MONTH = new TestEnum("MONTH");
    public final static TestEnum YEAR = new TestEnum("YEAR");

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
