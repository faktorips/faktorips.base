/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.codegen.JavaCodeFragment;
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
        getCompiler().setEnsureResultIsObject(false);
        registerFunction(new Exists("EXISTS", ""));
    }

    /**
     * Compiles the given expression and tests if the compilation was successfull and if the
     * datatype is the expected one.
     */
    private void compileSuccessfull(String expression) throws Exception {
        CompilationResult<JavaCodeFragment> result = getCompiler().compile(expression);
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
