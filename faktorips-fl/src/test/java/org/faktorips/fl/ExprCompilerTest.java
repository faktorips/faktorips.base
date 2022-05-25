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
import java.util.Set;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.operations.AbstractBinaryOperation;
import org.faktorips.runtime.Message;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ExprCompilerTest {

    private class DummyCompilationResultImpl extends AbstractCompilationResult<CodeFragment> {

        public DummyCompilationResultImpl(CodeFragment sourcecode, Datatype datatype) {
            super(sourcecode, datatype);
        }

        public DummyCompilationResultImpl(Message message) {
            super(message, new CodeFragment());
        }

        public DummyCompilationResultImpl(String sourcecode, Datatype datatype) {
            super(new CodeFragment(sourcecode), datatype);
        }

        public DummyCompilationResultImpl() {
            super(new CodeFragment());
        }

    }

    private class DummyParseTreeVisitor extends ParseTreeVisitor<CodeFragment> {

        DummyParseTreeVisitor(ExprCompiler<CodeFragment> compiler) {
            super(compiler);
        }

        @Override
        protected AbstractCompilationResult<CodeFragment> newCompilationResultImpl(String sourcecode,
                Datatype datatype) {
            return new DummyCompilationResultImpl(sourcecode, datatype);
        }

        @Override
        protected AbstractCompilationResult<CodeFragment> newCompilationResultImpl(CodeFragment sourcecode,
                Datatype datatype) {
            return new DummyCompilationResultImpl(sourcecode, datatype);
        }

        @Override
        protected AbstractCompilationResult<CodeFragment> newCompilationResultImpl(Message message) {
            return new DummyCompilationResultImpl(message);
        }

        @Override
        protected AbstractCompilationResult<CodeFragment> newCompilationResultImpl() {
            return new DummyCompilationResultImpl();
        }

    }

    protected ExprCompiler<CodeFragment> compiler;

    @Before
    public void setUp() throws Exception {
        compiler = new ExprCompiler<>() {

            @Override
            protected void registerDefaults() {
                // no defaults
            }

            @Override
            protected CodeFragment convertPrimitiveToWrapper(Datatype resultType, CodeFragment codeFragment) {
                CodeFragment wrappedCode = new CodeFragment("WRAPPED(");
                wrappedCode.append(codeFragment);
                wrappedCode.append(')');
                return wrappedCode;
            }

            @Override
            protected ParseTreeVisitor<CodeFragment> newParseTreeVisitor() {
                return new DummyParseTreeVisitor(this);
            }

            @Override
            protected AbstractCompilationResult<CodeFragment> newCompilationResultImpl(Message message) {
                return new DummyCompilationResultImpl(message);
            }

            @Override
            protected AbstractCompilationResult<CodeFragment> newCompilationResultImpl(CodeFragment sourcecode,
                    Datatype datatype) {
                return new DummyCompilationResultImpl(sourcecode, datatype);
            }

        };
        Locale.setDefault(Locale.ENGLISH);
        compiler.setLocale(Locale.ENGLISH);
        compiler.setDatatypeHelperProvider(datatype -> new BaseDatatypeHelper<>() {

            @Override
            public Datatype getDatatype() {
                return datatype;
            }

            @Override
            public void setDatatype(Datatype datatype) {
                //don't
            }

            @Override
            public CodeFragment nullExpression() {
                return new CodeFragment("null");
            }

            @Override
            public CodeFragment newInstance(String value) {
                return new CodeFragment(value);
            }

            @Override
            public CodeFragment newInstanceFromExpression(String expression) {
                return new CodeFragment(expression);
            }

            @Override
            public CodeFragment newInstanceFromExpression(String expression, boolean checkForNull) {
                return new CodeFragment(expression);
            }

            @Override
            public CodeFragment getToStringExpression(String fieldName) {
                return new CodeFragment(fieldName);
            }
        });
    }

    /**
     * Test if a syntax error message is generated when the expression is not a valid expression as
     * defined by the grammar.
     */
    @Test
    public void testSyntaxError() {
        CompilationResult<CodeFragment> result = compiler.compile("1 * * 2");
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
        CompilationResult<CodeFragment> result = compiler.compile("1.5 + 2EUR");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_OPERATOR, msg.getCode());
        assertEquals("The operator + is undefined for the type(s) Decimal, Money.", msg.getText());
    }

    @Test
    public void testGetFunctions() {
        DefaultFunctionResolver<CodeFragment> r1 = new DefaultFunctionResolver<>();
        FlFunction<CodeFragment> f1 = createFunction("IF1",
                new Datatype[] { Datatype.PRIMITIVE_BOOLEAN, AnyDatatype.INSTANCE, AnyDatatype.INSTANCE });
        r1.add(f1);
        DefaultFunctionResolver<CodeFragment> r2 = new DefaultFunctionResolver<>();
        FlFunction<CodeFragment> f2 = createFunction("IF2",
                new Datatype[] { Datatype.PRIMITIVE_BOOLEAN, AnyDatatype.INSTANCE, AnyDatatype.INSTANCE });
        FlFunction<CodeFragment> f3 = createFunction("IF3",
                new Datatype[] { Datatype.PRIMITIVE_BOOLEAN, AnyDatatype.INSTANCE, AnyDatatype.INSTANCE });
        r2.add(f2);
        r2.add(f3);
        FlFunction<CodeFragment>[] functions = compiler.getFunctions();
        assertEquals(0, functions.length);
        compiler.add(r1);
        compiler.add(r2);
        functions = compiler.getFunctions();
        assertEquals(3, functions.length);
        assertEquals(f1, functions[0]);
        assertEquals(f2, functions[1]);
        assertEquals(f3, functions[2]);
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
    public void testGetAmbiguousFunctions() {

        Datatype[] argTypesDecimal = { Datatype.DECIMAL };
        Datatype[] argTypesString = { Datatype.STRING };

        String matchingFunctionName = "functionMatchingName";

        FlFunction<CodeFragment> function1 = createFunction("function1", argTypesDecimal);
        FlFunction<CodeFragment> function2 = createFunction(matchingFunctionName, argTypesDecimal);
        FlFunction<CodeFragment> function3 = createFunction(matchingFunctionName, argTypesDecimal);
        FlFunction<CodeFragment> function4 = createFunction(matchingFunctionName, argTypesString);
        FlFunction<CodeFragment> function5 = createFunction("function5", argTypesDecimal);

        matchingFunctions(function2, function3);

        @SuppressWarnings("unchecked")
        FunctionResolver<CodeFragment> fctResolver1 = createFunctionResolver(function1, function2);
        @SuppressWarnings("unchecked")
        FunctionResolver<CodeFragment> fctResolver2 = createFunctionResolver(function3, function4, function5);

        compiler.add(fctResolver1);
        compiler.add(fctResolver2);

        Set<FlFunction<CodeFragment>> ambiguousFunctions = compiler.getAmbiguousFunctions(compiler.getFunctions());

        assertEquals(2, ambiguousFunctions.size());

        assertTrue(ambiguousFunctions.contains(function2));
        assertTrue(ambiguousFunctions.contains(function3));
    }

    @Test
    public void testIdentifierResolvingFailed() {
        setFailingIdentifierResolver();
        CompilationResult<CodeFragment> result = compiler.compile("a");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, msg.getCode());
    }

    private void setFailingIdentifierResolver() {
        compiler.setIdentifierResolver((identifier, exprCompiler, locale) -> {
            String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale,
                    identifier);
            DummyCompilationResultImpl compilationResult = new DummyCompilationResultImpl(
                    Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
            return compilationResult;
        });
    }

    @Test
    public void testIdentifierResolvingSuccessfull() {
        compiler.setIdentifierResolver((identifier, exprCompiler, locale) -> new DummyCompilationResultImpl(
                new CodeFragment("IDENTIFIER " + identifier), Datatype.STRING));
        CompilationResult<CodeFragment> result = compiler.compile("a");
        assertTrue(result.successfull());
        assertTrue(result.getCodeFragment().getSourcecode().startsWith("IDENTIFIER a"));
        assertEquals(Datatype.STRING, result.getDatatype());
    }

    private void registerAddIntInt() {
        compiler.register(
                new AbstractBinaryOperation<>("+", Datatype.PRIMITIVE_INT, Datatype.PRIMITIVE_INT) {

                    @Override
                    public CompilationResult<CodeFragment> generate(CompilationResult<CodeFragment> lhs,
                            CompilationResult<CodeFragment> rhs) {
                        lhs.getCodeFragment().append(" + "); //$NON-NLS-1$
                        ((AbstractCompilationResult<CodeFragment>)lhs).add(rhs);
                        return lhs;
                    }
                });
    }

    /**
     * Test if the lhs of a binary operation contains an error message, that the result of the
     * operation is a compilation result with that message.
     */
    @Test
    public void testBinaryOperationWithLhsError() {
        setFailingIdentifierResolver();
        registerAddIntInt();
        CompilationResult<CodeFragment> result = compiler.compile("a + 2");
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
        setFailingIdentifierResolver();
        registerAddIntInt();
        CompilationResult<CodeFragment> result = compiler.compile("a + 2");
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
        setFailingIdentifierResolver();
        registerAddIntInt();
        CompilationResult<CodeFragment> result = compiler.compile("a + b");
        assertTrue(result.failed());
        assertEquals(2, result.getMessages().size());
        Message lhsMsg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, lhsMsg.getCode());
        Message rhsMsg = result.getMessages().getMessage(1);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, rhsMsg.getCode());
    }

    @SuppressWarnings("unchecked")
    private FunctionResolver<CodeFragment> createFunctionResolver(FlFunction<CodeFragment>... functions) {
        FunctionResolver<CodeFragment> newFctResolver = mock(FunctionResolver.class);

        when(newFctResolver.getFunctions()).thenReturn(functions);

        return newFctResolver;
    }

    private FlFunction<CodeFragment> createFunction(String name, Datatype[] argTypes) {
        @SuppressWarnings("unchecked")
        FlFunction<CodeFragment> newFunction = mock(FlFunction.class);

        when(newFunction.getName()).thenReturn(name);
        when(newFunction.getArgTypes()).thenReturn(argTypes);
        when(newFunction.getType()).thenReturn(Datatype.INTEGER);

        return newFunction;
    }

    private void matchingFunctions(FlFunction<CodeFragment> matchingFunction, FlFunction<CodeFragment> newFunction) {
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
