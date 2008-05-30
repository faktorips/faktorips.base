/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.values.Decimal;

public class TestTableRow {
    
    private String company;

    private Integer gender;

    private Decimal rate;

    /**
     * 
     */
    public TestTableRow(String company, Integer gender, Decimal rate) {
        this.company = company;
        this.gender = gender;
        this.rate = rate;
    }

    /**
     * 
     */
    public String getCompany() {
        return company;
    }

    /**
     * 
     */
    public Integer getGender() {
        return gender;
    }

    /**
     * 
     */
    public Decimal getRate() {
        return rate;
    }

}