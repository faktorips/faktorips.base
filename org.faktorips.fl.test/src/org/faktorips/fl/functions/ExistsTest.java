/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ExistsTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        compiler.setEnsureResultIsObject(false);
        registerFunction(new Exists("EXISTS", ""));
    }

    /**
     * Compiles the given expression and tests if the compilation was successfull and if the
     * datatype is the expected one.
     */
    private void compileSuccessfull(String expression) throws Exception {
        CompilationResult result = compiler.compile(expression);
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_BOOLEAN, result.getDatatype());
    }

    @Test
    public void testDecimal() throws Exception {
        compileSuccessfull("EXISTS(1.0)");
        registerFunction(new DecimalNullFct());
        compileSuccessfull("EXISTS(DECIMALNULL())");
    }

    @Test
    public void testMoney() throws Exception {
        compileSuccessfull("EXISTS(10EUR)");
        registerFunction(new MoneyNullFct());
        compileSuccessfull("EXISTS(MONEYNULL())");
    }

    @Test
    public void testString() throws Exception {
        compileSuccessfull("EXISTS(\"a\")");
    }

    @Test
    public void testInt() throws Exception {
        execAndTestSuccessfull("EXISTS(1)", true);
    }

    @Test
    public void testBoolean() throws Exception {
        registerFunction(new BooleanFct("TRUEOBJ", Boolean.TRUE));
        registerFunction(new BooleanFct("BOOLEANNULL", null));
        compileSuccessfull("EXISTS(TRUEOBJ())");
        compileSuccessfull("EXISTS(BOOLEANNULL())");
    }

    @Test
    public void testPrimitiveBoolean() throws Exception {
        execAndTestSuccessfull("EXISTS(true)", true);
    }

}
