/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.internal;

import org.faktorips.runtime.model.annotation.IpsTableColumn;
import org.faktorips.values.Decimal;

/**
 * Diese Klasse repraesentiert eine (Read-Only) Zeile einer Tabelle.
 * 
 * @generated
 */
public class TestTableRow {

    /**
     * @generated
     */
    public static final TestTableRow NULL_ROW = new TestTableRow(null, null, Decimal.NULL);

    /**
     * @generated
     */
    private final String company;
    /**
     * @generated
     */
    private final Integer gender;
    /**
     * @generated
     */
    private final Decimal rate;

    /**
     * Erzeugt eine neue Zeile.
     * 
     * @generated
     */
    public TestTableRow(String company, Integer gender, Decimal rate) {
        this.company = company;
        this.gender = gender;
        this.rate = rate;
    }

    /**
     * @generated
     */
    @IpsTableColumn(name = "company")
    public String getCompany() {
        return company;
    }

    /**
     * @generated
     */
    @IpsTableColumn(name = "Gender")
    public Integer getGender() {
        return gender;
    }

    /**
     * @generated
     */
    @IpsTableColumn(name = "rate")
    public Decimal getRate() {
        return rate;
    }

    /**
     * @generated
     */
    @Override
    public String toString() {
        return "" + company + "|" + gender + "|" + rate;
    }
}
