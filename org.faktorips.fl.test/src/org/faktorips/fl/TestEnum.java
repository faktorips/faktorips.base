/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
