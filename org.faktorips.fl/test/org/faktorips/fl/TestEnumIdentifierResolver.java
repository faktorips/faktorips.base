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
