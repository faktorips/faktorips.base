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
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.DefaultIdentifierResolver;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;

public class CountTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(new Count("COUNT", ""));
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("hsVertrag.Deckung", new JavaCodeFragment("hsVertrag.getDeckungen()"),
                new ListOfTypeDatatype(Datatype.STRING));
        resolver.register("hsVertrag.VersSumme", new JavaCodeFragment("hsVertrag.VersSumme"), Datatype.MONEY);
        getCompiler().setIdentifierResolver(resolver);
    }

    /**
     * Compiles the given expression and tests if the compilation was successfull and if the
     * datatype is the expected one.
     */
    private void compileSuccessfull(String expression, String expectedValue) throws Exception {
        CompilationResult<JavaCodeFragment> result = getCompiler().compile(expression);
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
        assertEquals(Datatype.INTEGER, result.getDatatype());
        assertEquals(expectedValue, result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testCount() throws Exception {
        compileSuccessfull("COUNT(hsVertrag.Deckung)", "Integer.valueOf(hsVertrag.getDeckungen().size())");
        execAndTestFail("COUNT(hsVertrag.VersSumme)", ExprCompiler.WRONG_ARGUMENT_TYPES);
    }
}
