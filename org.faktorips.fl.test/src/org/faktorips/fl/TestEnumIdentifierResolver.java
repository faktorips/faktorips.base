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

import org.faktorips.codegen.JavaCodeFragment;

/**
 * 
 * @author Jan Ortmann
 */
public class TestEnumIdentifierResolver extends DefaultIdentifierResolver {

    /**
     * 
     */
    public TestEnumIdentifierResolver() {
        super();
        JavaCodeFragment codeMonth = new JavaCodeFragment();
        codeMonth.appendClassName(TestEnum.class);
        codeMonth.append(".MONTH");
        register("TestEnum.MONTH", codeMonth, TestEnumDatatype.INSTANCE);

        JavaCodeFragment codeYear = new JavaCodeFragment();
        codeYear.appendClassName(TestEnum.class);
        codeYear.append(".YEAR");
        register("TestEnum.YEAR", codeYear, TestEnumDatatype.INSTANCE);
    }

}
