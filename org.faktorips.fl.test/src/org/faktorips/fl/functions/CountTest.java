/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
        compiler.setIdentifierResolver(resolver);
    }

    /**
     * Compiles the given expression and tests if the compilation was successfull and if the
     * datatype is the expected one.
     */
    private void compileSuccessfull(String expression, String expectedValue) throws Exception {
        CompilationResult<JavaCodeFragment> result = compiler.compile(expression);
        if (result.failed()) {
            System.out.println(result);
        }
        assertTrue(result.successfull());
        assertEquals(Datatype.INTEGER, result.getDatatype());
        assertEquals(expectedValue, result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testCount() throws Exception {
        compileSuccessfull("COUNT(hsVertrag.Deckung)", "new Integer(hsVertrag.getDeckungen().size())");
        execAndTestFail("COUNT(hsVertrag.VersSumme)", ExprCompiler.WRONG_ARGUMENT_TYPES);
    }
}
