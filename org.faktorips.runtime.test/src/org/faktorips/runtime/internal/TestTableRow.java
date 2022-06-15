/*
 * BEGIN FAKTORIPS GENERATOR INFORMATION SECTION
 * 
 * builder set: org.faktorips.devtools.stdbuilder.ipsstdbuilderset, Version: 3.0.0
 * 
 * END FAKTORIPS GENERATOR INFORMATION SECTION
 */
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
