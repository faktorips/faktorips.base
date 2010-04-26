/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.Datatype;

/**
 *
 */
public class LessThanOrEqualTest extends CompilerAbstractTest {

    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("7.45 <= 7.46", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.45 <= 7.45", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.45 <= 7.44", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("6.9 <= 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 <= 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.1 <= 7", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("7 <= 7.1", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 <= 7.0", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 <= 6.9", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("6.9 <= WHOLENUMBER(7.1)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.0 <= WHOLENUMBER(7.1)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7.1 <= WHOLENUMBER(7.1)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7.1) <= 7.1", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7.1) <= 7.0", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7.1) <= 6.9", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testIntInt() throws Exception {
        execAndTestSuccessfull("7 <= 8", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 <= 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 <= 6", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("7 <= WHOLENUMBER(8)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 <= WHOLENUMBER(7)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("7 <= WHOLENUMBER(6)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) <= 8", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) <= 7", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) <= 6", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(7) <= WHOLENUMBER(8)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) <= WHOLENUMBER(7)", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("WHOLENUMBER(7) <= WHOLENUMBER(6)", Boolean.FALSE, Datatype.BOOLEAN);
    }

    public void testMoneyMoney() throws Exception {
        execAndTestSuccessfull("3.50EUR <= 3.51EUR", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("3.50EUR <= 3.50EUR", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("3.50EUR <= 3.49EUR", Boolean.FALSE, Datatype.BOOLEAN);
    }

}
