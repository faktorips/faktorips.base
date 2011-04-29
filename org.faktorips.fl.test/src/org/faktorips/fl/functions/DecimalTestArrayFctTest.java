/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.fl.functions;

import static org.junit.Assert.assertTrue;

import org.faktorips.fl.CompilationResult;
import org.faktorips.values.Decimal;
import org.junit.Test;

/**
 *
 */
public class DecimalTestArrayFctTest extends FunctionAbstractTest {

    @Test
    public void test() throws Exception {
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(new Decimal[] { Decimal.valueOf(10, 0), Decimal.valueOf(32, 0) });
        registerFunction(testFct);
        CompilationResult result = compiler.compile("DECIMALTESTARRAY()");
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
    }

    @Test
    public void testNull() throws Exception {
        DecimalTestArrayFct testFct = new DecimalTestArrayFct();
        testFct.setValues(null);
        registerFunction(testFct);
        CompilationResult result = compiler.compile("DECIMALTESTARRAY()");
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
    }

}
