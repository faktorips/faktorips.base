/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.conversion.PrimitiveIntToDecimalCg;
import org.faktorips.codegen.conversion.PrimitiveIntToIntegerCg;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.operations.AddDecimalDecimal;
import org.faktorips.fl.operations.PlusInteger;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Test;

/**
 *
 */
public class JavaExprCompilerTest extends JavaExprCompilerAbstractTest {

    @Test
    public void testBinaryOperationCasting() throws Exception {
        // Make only implicit conversions int to Decimal.
        ConversionCodeGenerator<JavaCodeFragment> ccg = new ConversionCodeGenerator<>();
        ccg.add(new PrimitiveIntToDecimalCg());
        getCompiler().setConversionCodeGenerator(ccg);

        // Only binary operator is Decimal+Decimal
        @SuppressWarnings("unchecked")
        BinaryOperation<JavaCodeFragment>[] binaryOperations = new BinaryOperation[] { new AddDecimalDecimal() };
        getCompiler().setBinaryOperations(binaryOperations);

        // compiler should convert primitive int on lhs to Decimal
        execAndTestSuccessfull("41 + 1.2", Decimal.valueOf(422, 1), Datatype.DECIMAL);
        // same for rhs
        execAndTestSuccessfull("1.2 + 41", Decimal.valueOf(422, 1), Datatype.DECIMAL);
        // same for lhs and rhs
        execAndTestSuccessfull("1.2 + 41.0", Decimal.valueOf(422, 1), Datatype.DECIMAL);
    }

    @Test
    public void testOperation_double_minus_double_plus_double() throws Exception {
        getCompiler().setConversionCodeGenerator(ConversionCodeGenerator.getDefault());
        getCompiler().registerDefaults();

        execAndTestSuccessfull("a - b + c", Decimal.valueOf(0.4), new String[] { "a", "b", "c" },
                new Datatype[] { Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE }, new Object[] { 0.2, 0.3, 0.5 },
                Datatype.DECIMAL);
    }

    @Test
    public void testFunctionResolvingSuccessfull() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
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
        ConversionCodeGenerator<JavaCodeFragment> ccg = new ConversionCodeGenerator<>();
        ccg.add(new PrimitiveIntToIntegerCg());
        getCompiler().setConversionCodeGenerator(ccg);

        // Only unary plus operator is defined on Integer
        @SuppressWarnings("unchecked")
        UnaryOperation<JavaCodeFragment>[] unaryOperations = new UnaryOperation[] { new PlusInteger() };
        getCompiler().setUnaryOperations(unaryOperations);

        // compiler should convert primitive int to Integer
        execAndTestSuccessfull("+ 42", Integer.valueOf(42), Datatype.INTEGER);
    }

    @Test
    public void testFunctionResolving_FailWithInvalidFunction() throws Exception {
        execAndTestFail("InvalidFunction(2.34; 1)", ExprCompiler.UNDEFINED_FUNCTION);
    }

    @Test
    public void testFunctionResolving_FailWithWrongArgTypes() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestFail("ROUND(false; 1)", ExprCompiler.WRONG_ARGUMENT_TYPES);
    }

    @Test
    public void testFunctionCall_ErrorInArgumentsTypes() throws Exception {
        getCompiler().add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestFail("ROUND(false + 1; 1)", ExprCompiler.UNDEFINED_OPERATOR);
    }

    @Test
    public void testBooleanConstant() {
        getCompiler().setEnsureResultIsObject(false);
        CompilationResult<JavaCodeFragment> result = getCompiler().compile("false");
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_BOOLEAN, result.getDatatype());
        JavaCodeFragment expected = DatatypeHelper.PRIMITIVE_BOOLEAN.newInstance("false");
        assertEquals(expected, result.getCodeFragment());

        result = getCompiler().compile("true");
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_BOOLEAN, result.getDatatype());
        expected = DatatypeHelper.PRIMITIVE_BOOLEAN.newInstance("true");
        assertEquals(expected, result.getCodeFragment());
    }

    @Test
    public void testStringConstant() {
        CompilationResult<JavaCodeFragment> result = getCompiler().compile("\"blabla\"");
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
        getCompiler().setEnsureResultIsObject(false);
        CompilationResult<JavaCodeFragment> result = getCompiler().compile("42");
        assertTrue(result.successfull());
        assertEquals(Datatype.PRIMITIVE_INT, result.getDatatype());
        JavaCodeFragment expected = DatatypeHelper.PRIMITIVE_INTEGER.newInstance("42");
        assertEquals(expected, result.getCodeFragment());

        result = getCompiler().compile("-42");
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
        CompilationResult<JavaCodeFragment> result = getCompiler().compile("1");
        assertTrue(result.successfull());
        result = getCompiler().compile("1;a");
        assertFalse(result.successfull());
    }

    @Test
    public void testSpaceInSyntax() {
        assertTrue(getCompiler().compile("1+1").successfull());
        assertTrue(getCompiler().compile("1 +1").successfull());
        assertTrue(getCompiler().compile("1+ 1").successfull());
        assertTrue(getCompiler().compile("1 + 1").successfull());
        assertTrue(getCompiler().compile("1-1").successfull());
        assertTrue(getCompiler().compile("1 -1").successfull());
        assertTrue(getCompiler().compile("1- 1").successfull());
        assertTrue(getCompiler().compile("1 - 1").successfull());
        CompilationResult<JavaCodeFragment> compile = getCompiler().compile("1+-1");
        assertTrue(compile.successfull());
        assertEquals(System.lineSeparator() + "Integer.valueOf(1 + -1)", compile.getCodeFragment().toString());
        assertEquals(0, Integer.valueOf(1 + -1).intValue());
        assertTrue(getCompiler().compile("1>1").successfull());
        assertTrue(getCompiler().compile("1=1").successfull());
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

        getCompiler().add(fctResolver1);
        getCompiler().add(fctResolver2);

        CompilationResult<JavaCodeFragment> resultWithAmbiguousFunctionCall = getCompiler()
                .compile(matchingFunctionName + "(2.0)");

        MessageList messageList = resultWithAmbiguousFunctionCall.getMessages();
        assertTrue(messageList.containsErrorMsg());

        Message message = messageList.getFirstMessage(Message.ERROR);

        assertEquals(ExprCompiler.AMBIGUOUS_FUNCTION_CALL, message.getCode());

    }

    @SuppressWarnings("unchecked")
    private FunctionResolver<JavaCodeFragment> createFunctionResolver(FlFunction<JavaCodeFragment>... functions) {
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
