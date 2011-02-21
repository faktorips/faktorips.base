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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.conversion.PrimitiveIntToDecimalCg;
import org.faktorips.codegen.conversion.PrimitiveIntToIntegerCg;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.functions.If;
import org.faktorips.fl.operations.AddDecimalDecimal;
import org.faktorips.fl.operations.PlusInteger;
import org.faktorips.util.message.Message;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Test;

/**
 *
 */
public class ExprCompilerTest extends CompilerAbstractTest {

    @Test
    public void testGetFunctions() {
        DefaultFunctionResolver r1 = new DefaultFunctionResolver();
        FlFunction f1 = new If("IF1", "");
        r1.add(f1);
        DefaultFunctionResolver r2 = new DefaultFunctionResolver();
        FlFunction f2 = new If("IF2", "");
        FlFunction f3 = new If("IF3", "");
        r2.add(f2);
        r2.add(f3);
        FlFunction[] functions = compiler.getFunctions();
        assertEquals(0, functions.length);
        compiler.add(r1);
        compiler.add(r2);
        functions = compiler.getFunctions();
        assertEquals(3, functions.length);
        assertEquals(f1, functions[0]);
        assertEquals(f2, functions[1]);
        assertEquals(f3, functions[2]);
    }

    /**
     * Test if a syntax error message is generated when the expression is not a valid expression as
     * defined by the grammar.
     */
    @Test
    public void testSyntaxError() {
        CompilationResult result = compiler.compile("1 * * 2");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.SYNTAX_ERROR, result.getMessages().getMessage(0).getCode());

        // Tokens like , and " cause a TokenMgrError (Error not Exception!)
        // Test if this is catched
        result = compiler.compile("1, 2");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.LEXICAL_ERROR, result.getMessages().getMessage(0).getCode());

        result = compiler.compile("1\" 2");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        assertEquals(ExprCompiler.LEXICAL_ERROR, result.getMessages().getMessage(0).getCode());
    }

    @Test
    public void testOpInvalidTypesError() {
        CompilationResult result = compiler.compile("1.5 + 2EUR");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_OPERATOR, msg.getCode());
        assertEquals("The operator + is undefined for the type(s) Decimal, Money.", msg.getText());
    }

    @Test
    public void testBinaryOperationCasting() throws Exception {
        // Make only implicit conversions int to Decimal.
        ConversionCodeGenerator ccg = new ConversionCodeGenerator();
        ccg.add(new PrimitiveIntToDecimalCg());
        compiler.setConversionCodeGenerator(ccg);

        // Only binary operator is Decimal+Decimal
        compiler.setBinaryOperations(new BinaryOperation[] { new AddDecimalDecimal() });

        // compiler should convert primitive int on lhs to Decimal
        execAndTestSuccessfull("41 + 1.2", Decimal.valueOf(422, 1), Datatype.DECIMAL);
        // same for rhs
        execAndTestSuccessfull("1.2 + 41", Decimal.valueOf(422, 1), Datatype.DECIMAL);
        // same for lhs and rhs
        execAndTestSuccessfull("1.2 + 41.0", Decimal.valueOf(422, 1), Datatype.DECIMAL);
    }

    /**
     * Test if the lhs of a binary operation contains an error message, that the result of the
     * operation is a compilation result with that message.
     */
    @Test
    public void testBinaryOperationWithLhsError() {
        CompilationResult result = compiler.compile("a + 2");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, msg.getCode());
    }

    /**
     * Test if the rhs of a binary operation contains an error message, that the result of the
     * operation is a compilation result with that message.
     */
    @Test
    public void testBinaryOperationWithRhsError() {
        CompilationResult result = compiler.compile("a + 2");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, msg.getCode());
    }

    /**
     * Test if the lhs and the rhs of a binary operation contains an error message, that the result
     * of the operation is a compilation result with the two messages.
     */
    @Test
    public void testBinaryOperationWithLhsAndRhsError() {
        CompilationResult result = compiler.compile("a + b");
        assertTrue(result.failed());
        assertEquals(2, result.getMessages().size());
        Message lhsMsg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, lhsMsg.getCode());
        Message rhsMsg = result.getMessages().getMessage(1);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, rhsMsg.getCode());
    }

    @Test
    public void testIdentifierResolvingFailed() {
        CompilationResult result = compiler.compile("a + 2");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, msg.getCode());
    }

    @Test
    public void testIdentifierResolvingSuccessfull() {
        compiler = new ExprCompiler(Locale.ENGLISH);
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("a", new JavaCodeFragment("getA()"), Datatype.DECIMAL);
        compiler.setIdentifierResolver(resolver);
        CompilationResult result = compiler.compile("a + 2.1");
        assertTrue(result.successfull());
        assertTrue(result.getCodeFragment().getSourcecode().startsWith("getA().add("));
        assertEquals(Datatype.DECIMAL, result.getDatatype());
    }

    @Test
    public void testFunctionResolvingSuccessfull() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("ROUND(2.34; 1)", Decimal.valueOf("2.3"), Datatype.DECIMAL);
    }

    @Test
    public void testFunctionResolvingWithImplicitConversion() {
        //
    }

    @Test
    public void testUndefinedOperator() throws Exception {
        CompilationResult result = execAndTestFail("+ false", ExprCompiler.UNDEFINED_OPERATOR);
        assertEquals("The operator + is undefined for the type(s) boolean.",
                result.getMessages().getMessageByCode(ExprCompiler.UNDEFINED_OPERATOR).getText());
    }

    @Test
    public void testUnaryOperationCasting() throws Exception {
        // Make only implicit conversions int to Integer.
        ConversionCodeGenerator ccg = new ConversionCodeGenerator();
        ccg.add(new PrimitiveIntToIntegerCg());
        compiler.setConversionCodeGenerator(ccg);

        // Only unary plus operator is defined on Integer
        compiler.setUnaryOperations(new UnaryOperation[] { new PlusInteger() });

        // compiler should convert primitive int to Integer
        execAndTestSuccessfull("+ 42", new Integer(42), Datatype.INTEGER);
    }

    @Test
    public void testFunctionResolving_FailWithInvalidFunction() throws Exception {
        execAndTestFail("InvalidFunction(2.34; 1)", ExprCompiler.UNDEFINED_FUNCTION);
    }

    @Test
    public void testFunctionResolving_FailWithWrongArgTypes() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestFail("ROUND(false; 1)", ExprCompiler.WRONG_ARGUMENT_TYPES);
    }

    @Test
    public void testFunctionCall_ErrorInArgumentsTypes() throws Exception {
        execAndTestFail("ROUND(false + 1; 1)", ExprCompiler.UNDEFINED_OPERATOR);
    }

    @Test
    public void testBooleanConstant() {
        compiler.setEnsureResultIsObject(false);
        CompilationResult result = compiler.compile("false");
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_BOOLEAN, result.getDatatype());
        JavaCodeFragment expected = DatatypeHelper.PRIMITIVE_BOOLEAN.newInstance("false");
        assertEquals(expected, result.getCodeFragment());

        result = compiler.compile("true");
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_BOOLEAN, result.getDatatype());
        expected = DatatypeHelper.PRIMITIVE_BOOLEAN.newInstance("true");
        assertEquals(expected, result.getCodeFragment());
    }

    @Test
    public void testStringConstant() {
        CompilationResult result = compiler.compile("\"blabla\"");
        assertTrue(result.successfull());
        assertEquals(Datatype.STRING, result.getDatatype());
        JavaCodeFragment expected = DatatypeHelper.STRING.newInstance("blabla");
        assertEquals(expected, result.getCodeFragment());
    }

    @Test
    public void testDecimalConstant() throws Exception {
        execAndTestSuccessfull("10.123", Decimal.valueOf("10.123"), Datatype.DECIMAL);
        execAndTestSuccessfull("-10.123", Decimal.valueOf("-10.123"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerConstant() {
        compiler.setEnsureResultIsObject(false);
        CompilationResult result = compiler.compile("42");
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_INT, result.getDatatype());
        JavaCodeFragment expected = DatatypeHelper.PRIMITIVE_INTEGER.newInstance("42");
        assertEquals(expected, result.getCodeFragment());

        result = compiler.compile("-42");
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_INT, result.getDatatype());
        expected = DatatypeHelper.PRIMITIVE_INTEGER.newInstance("-42");
        assertEquals(expected, result.getCodeFragment());
    }

    @Test
    public void testMoneyConstant() throws Exception {
        execAndTestSuccessfull("10.12EUR", Money.valueOf("10.12EUR"), Datatype.MONEY);
        execAndTestSuccessfull("-10.12EUR", Money.valueOf("-10.12EUR"), Datatype.MONEY);
    }

    @Test
    public void testIsValidIdentifier() {
        assertTrue(ExprCompiler.isValidIdentifier("a"));
        assertTrue(ExprCompiler.isValidIdentifier("a.a"));
        assertFalse(ExprCompiler.isValidIdentifier("/"));
        assertFalse(ExprCompiler.isValidIdentifier("true"));
        assertFalse(ExprCompiler.isValidIdentifier("-a"));
        assertFalse(ExprCompiler.isValidIdentifier("a-a"));
        assertFalse(ExprCompiler.isValidIdentifier("9"));
        assertTrue(ExprCompiler.isValidIdentifier("a9"));
    }

    @Test
    public void testSemicolonAtEnd() {
        CompilationResult result = compiler.compile("1");
        assertTrue(result.successfull());
        result = compiler.compile("1;a");
        assertFalse(result.successfull());
    }

    @Test
    public void testUsedIdentifiers() {
        compiler.setIdentifierResolver(new IdentifierResolver() {
            public CompilationResult compile(String identifier, ExprCompiler exprCompiler, Locale locale) {
                CompilationResultImpl compilationResult = new CompilationResultImpl(identifier, Datatype.INTEGER);
                // the identifier is always used as parameter
                compilationResult.addIdentifierUsed(identifier);
                return compilationResult;
            }

        });
        CompilationResult result = compiler.compile("1");
        assertEquals(0, result.getResolvedIdentifiers().length);

        result = compiler.compile("a + 1");
        assertEquals(1, result.getResolvedIdentifiers().length);
        assertEquals("a", result.getResolvedIdentifiers()[0]);

        result = compiler.compile("a + b");
        assertEquals(2, result.getResolvedIdentifiers().length);
        assertEquals("a", result.getResolvedIdentifiers()[0]);
        assertEquals("b", result.getResolvedIdentifiers()[1]);

        result = compiler.compile("b + a");
        assertEquals(2, result.getResolvedIdentifiers().length);
        assertEquals("b", result.getResolvedIdentifiers()[0]);
        assertEquals("a", result.getResolvedIdentifiers()[1]);
    }
}
