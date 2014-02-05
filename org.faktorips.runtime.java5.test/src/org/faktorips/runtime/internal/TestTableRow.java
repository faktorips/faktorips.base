/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.values.Decimal;

public class TestTableRow {

    private final String company;

    private final Integer gender;

    private final Decimal rate;

    public TestTableRow(String company, Integer gender, Decimal rate) {
        this.company = company;
        this.gender = gender;
        this.rate = rate;
    }

    public String getCompany() {
        return company;
    }

    public Integer getGender() {
        return gender;
    }

    public Decimal getRate() {
        return rate;
    }

}
