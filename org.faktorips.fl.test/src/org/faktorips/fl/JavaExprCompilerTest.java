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

package org.faktorips.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.conversion.PrimitiveIntToDecimalCg;
import org.faktorips.codegen.conversion.PrimitiveIntToIntegerCg;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.operations.AddDecimalDecimal;
import org.faktorips.fl.operations.PlusInteger;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Test;

/**
 *
 */
public class JavaExprCompilerTest extends JavaExprCompilerAbstractTest {

    /**
     * Test if a syntax error message is generated when the expression is not a valid expression as
     * defined by the grammar.
     */
    @Test
    public void testSyntaxError() {
        CompilationResult<JavaCodeFragment> result = compiler.compile("1 * * 2");
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
        CompilationResult<JavaCodeFragment> result = compiler.compile("1.5 + 2EUR");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_OPERATOR, msg.getCode());
        assertEquals("The operator + is undefined for the type(s) Decimal, Money.", msg.getText());
    }

    @Test
    public void testBinaryOperationCasting() throws Exception {
        // Make only implicit conversions int to Decimal.
        ConversionCodeGenerator<JavaCodeFragment> ccg = new ConversionCodeGenerator<JavaCodeFragment>();
        ccg.add(new PrimitiveIntToDecimalCg());
        compiler.setConversionCodeGenerator(ccg);

        // Only binary operator is Decimal+Decimal
        @SuppressWarnings("unchecked")
        BinaryOperation<JavaCodeFragment>[] binaryOperations = new BinaryOperation[] { new AddDecimalDecimal() };
        compiler.setBinaryOperations(binaryOperations);

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
        CompilationResult<JavaCodeFragment> result = compiler.compile("a + 2");
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
        CompilationResult<JavaCodeFragment> result = compiler.compile("a + 2");
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
        CompilationResult<JavaCodeFragment> result = compiler.compile("a + b");
        assertTrue(result.failed());
        assertEquals(2, result.getMessages().size());
        Message lhsMsg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, lhsMsg.getCode());
        Message rhsMsg = result.getMessages().getMessage(1);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, rhsMsg.getCode());
    }

    @Test
    public void testIdentifierResolvingFailed() {
        CompilationResult<JavaCodeFragment> result = compiler.compile("a + 2");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, msg.getCode());
    }

    @Test
    public void testIdentifierResolvingSuccessfull() {
        compiler = new JavaExprCompiler(Locale.ENGLISH);
        DefaultIdentifierResolver resolver = new DefaultIdentifierResolver();
        resolver.register("a", new JavaCodeFragment("getA()"), Datatype.DECIMAL);
        compiler.setIdentifierResolver(resolver);
        CompilationResult<JavaCodeFragment> result = compiler.compile("a + 2.1");
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
        CompilationResult<JavaCodeFragment> result = execAndTestFail("+ false", ExprCompiler.UNDEFINED_OPERATOR);
        assertEquals("The operator + is undefined for the type(s) boolean.",
                result.getMessages().getMessageByCode(ExprCompiler.UNDEFINED_OPERATOR).getText());
    }

    @Test
    public void testUnaryOperationCasting() throws Exception {
        // Make only implicit conversions int to Integer.
        ConversionCodeGenerator<JavaCodeFragment> ccg = new ConversionCodeGenerator<JavaCodeFragment>();
        ccg.add(new PrimitiveIntToIntegerCg());
        compiler.setConversionCodeGenerator(ccg);

        // Only unary plus operator is defined on Integer
        @SuppressWarnings("unchecked")
        UnaryOperation<JavaCodeFragment>[] unaryOperations = new UnaryOperation[] { new PlusInteger() };
        compiler.setUnaryOperations(unaryOperations);

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
        CompilationResult<JavaCodeFragment> result = compiler.compile("false");
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
        CompilationResult<JavaCodeFragment> result = compiler.compile("\"blabla\"");
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
        CompilationResult<JavaCodeFragment> result = compiler.compile("42");
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
    public void testSemicolonAtEnd() {
        CompilationResult<JavaCodeFragment> result = compiler.compile("1");
        assertTrue(result.successfull());
        result = compiler.compile("1;a");
        assertFalse(result.successfull());
    }

    @Test
    public void testUsedIdentifiers() {
        compiler.setIdentifierResolver(new IdentifierResolver<JavaCodeFragment>() {
            public CompilationResult<JavaCodeFragment> compile(String identifier,
                    ExprCompiler<JavaCodeFragment> exprCompiler,
                    Locale locale) {
                CompilationResultImpl compilationResult = new CompilationResultImpl(identifier,
                        Datatype.INTEGER);
                // the identifier is always used as parameter
                compilationResult.addIdentifierUsed(identifier);
                return compilationResult;
            }

        });
        CompilationResult<JavaCodeFragment> result = compiler.compile("1");
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

        result = compiler.compile("b+a");
        assertEquals(2, result.getResolvedIdentifiers().length);
        assertEquals("b", result.getResolvedIdentifiers()[0]);
        assertEquals("a", result.getResolvedIdentifiers()[1]);
    }

    @Test
    public void testSpaceInSyntax() {
        assertTrue(compiler.compile("1+1").successfull());
        assertTrue(compiler.compile("1 +1").successfull());
        assertTrue(compiler.compile("1+ 1").successfull());
        assertTrue(compiler.compile("1 + 1").successfull());
        assertTrue(compiler.compile("1-1").successfull());
        assertTrue(compiler.compile("1 -1").successfull());
        assertTrue(compiler.compile("1- 1").successfull());
        assertTrue(compiler.compile("1 - 1").successfull());
        CompilationResult<JavaCodeFragment> compile = compiler.compile("1+-1");
        assertTrue(compile.successfull());
        assertEquals(SystemUtils.LINE_SEPARATOR + "new Integer(1 + -1)", compile.getCodeFragment().toString());
        assertEquals(0, new Integer(1 + -1).intValue());
        assertTrue(compiler.compile("1>1").successfull());
        assertTrue(compiler.compile("1=1").successfull());
    }

    @Test
    public void testCompileWithAmbiguousFunctions() {

        Datatype[] argTypesDecimal = { Datatype.DECIMAL };
        Datatype[] argTypesString = { Datatype.STRING };

        String matchingFunctionName = "x.functionMatchingName";

        FlFunction<JavaCodeFragment> function1 = createFunction("function1", argTypesDecimal);
        FlFunction<JavaCodeFragment> function2 = createFunction(matchingFunctionName, argTypesDecimal);
        FlFunction<JavaCodeFragment> function3 = createFunction(matchingFunctionName, argTypesDecimal);
        FlFunction<JavaCodeFragment> function4 = createFunction(matchingFunctionName, argTypesString);
        FlFunction<JavaCodeFragment> function5 = createFunction("function5", argTypesDecimal);

        matchingFunctions(function2, function3);

        @SuppressWarnings("unchecked")
        FunctionResolver<JavaCodeFragment> fctResolver1 = createFunctionResolver(function1, function2);
        @SuppressWarnings("unchecked")
        FunctionResolver<JavaCodeFragment> fctResolver2 = createFunctionResolver(function3, function4, function5);

        compiler.add(fctResolver1);
        compiler.add(fctResolver2);

        CompilationResult<JavaCodeFragment> resultWithAmbiguousFunctionCall = compiler.compile(matchingFunctionName
                + "(2.0)");

        MessageList messageList = resultWithAmbiguousFunctionCall.getMessages();
        assertTrue(messageList.containsErrorMsg());

        Message message = messageList.getFirstMessage(Message.ERROR);

        assertEquals(ExprCompiler.AMBIGUOUS_FUNCTION_CALL, message.getCode());

    }

    private FunctionResolver<JavaCodeFragment> createFunctionResolver(FlFunction<JavaCodeFragment>... functions) {
        @SuppressWarnings("unchecked")
        FunctionResolver<JavaCodeFragment> newFctResolver = mock(FunctionResolver.class);

        when(newFctResolver.getFunctions()).thenReturn(functions);

        return newFctResolver;
    }

    private FlFunction<JavaCodeFragment> createFunction(String name, Datatype[] argTypes) {
        @SuppressWarnings("unchecked")
        FlFunction<JavaCodeFragment> newFunction = mock(FlFunction.class);

        when(newFunction.getName()).thenReturn(name);
        when(newFunction.getArgTypes()).thenReturn(argTypes);
        when(newFunction.getType()).thenReturn(Datatype.INTEGER);

        return newFunction;
    }

    private void matchingFunctions(FlFunction<JavaCodeFragment> matchingFunction,
            FlFunction<JavaCodeFragment> newFunction) {
        when(newFunction.isSame(matchingFunction)).thenReturn(true);
        when(matchingFunction.isSame(newFunction)).thenReturn(true);

        Datatype[] matchingArgTypes = matchingFunction.getArgTypes();
        String matchingName = matchingFunction.getName();
        when(newFunction.match(eq(matchingName), aryEq(matchingArgTypes))).thenReturn(true);

        String name = newFunction.getName();
        Datatype[] argTypes = newFunction.getArgTypes();
        when(matchingFunction.match(eq(name), aryEq(argTypes))).thenReturn(true);
    }
}
