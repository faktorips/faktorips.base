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
import java.util.Set;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.operations.AbstractBinaryOperation;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.util.message.Message;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ExprCompilerTest {

    private class DummyCodeFragment extends CodeFragment {

        public DummyCodeFragment(String sourcecode) {
            super(sourcecode);
        }

        public DummyCodeFragment() {
            super();
        }

    }

    private class DummyCompilationResultImpl extends AbstractCompilationResult<DummyCodeFragment> {

        public DummyCompilationResultImpl(DummyCodeFragment sourcecode, Datatype datatype) {
            super(sourcecode, datatype);
        }

        public DummyCompilationResultImpl(Message message) {
            super(message, new DummyCodeFragment());
        }

        public DummyCompilationResultImpl(String sourcecode, Datatype datatype) {
            super(new DummyCodeFragment(sourcecode), datatype);
        }

        public DummyCompilationResultImpl() {
            super(new DummyCodeFragment());
        }

    }

    private class DummyParseTreeVisitor extends ParseTreeVisitor<DummyCodeFragment> {

        DummyParseTreeVisitor(ExprCompiler<DummyCodeFragment> compiler) {
            super(compiler);
        }

        @Override
        protected AbstractCompilationResult<DummyCodeFragment> newCompilationResultImpl(String sourcecode,
                Datatype datatype) {
            return new DummyCompilationResultImpl(sourcecode, datatype);
        }

        @Override
        protected AbstractCompilationResult<DummyCodeFragment> newCompilationResultImpl(DummyCodeFragment sourcecode,
                Datatype datatype) {
            return new DummyCompilationResultImpl(sourcecode, datatype);
        }

        @Override
        protected AbstractCompilationResult<DummyCodeFragment> newCompilationResultImpl(Message message) {
            return new DummyCompilationResultImpl(message);
        }

        @Override
        protected AbstractCompilationResult<DummyCodeFragment> newCompilationResultImpl() {
            return new DummyCompilationResultImpl();
        }

        @Override
        protected AbstractCompilationResult<DummyCodeFragment> generateConstant(SimpleNode node, DatatypeHelper helper) {
            String value = node.getLastToken().toString();
            return new DummyCompilationResultImpl("CONSTANT " + value, helper.getDatatype());
        }

    }

    protected ExprCompiler<DummyCodeFragment> compiler;

    @Before
    public void setUp() throws Exception {
        compiler = new ExprCompiler<DummyCodeFragment>() {

            @Override
            protected void registerDefaults() {
                // no defaults
            }

            @Override
            protected DummyCodeFragment convertPrimitiveToWrapper(Datatype resultType, DummyCodeFragment codeFragment) {
                DummyCodeFragment wrappedCode = new DummyCodeFragment("WRAPPED(");
                wrappedCode.append(codeFragment);
                wrappedCode.append(')');
                return wrappedCode;
            }

            @Override
            protected ParseTreeVisitor<DummyCodeFragment> newParseTreeVisitor() {
                return new DummyParseTreeVisitor(this);
            }

            @Override
            protected AbstractCompilationResult<DummyCodeFragment> newCompilationResultImpl(Message message) {
                return new DummyCompilationResultImpl(message);
            }

            @Override
            protected AbstractCompilationResult<DummyCodeFragment> newCompilationResultImpl(DummyCodeFragment sourcecode,
                    Datatype datatype) {
                return new DummyCompilationResultImpl(sourcecode, datatype);
            }

        };
        Locale.setDefault(Locale.ENGLISH);
        compiler.setLocale(Locale.ENGLISH);
    }

    /**
     * Test if a syntax error message is generated when the expression is not a valid expression as
     * defined by the grammar.
     */
    @Test
    public void testSyntaxError() {
        CompilationResult<DummyCodeFragment> result = compiler.compile("1 * * 2");
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
        CompilationResult<DummyCodeFragment> result = compiler.compile("1.5 + 2EUR");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_OPERATOR, msg.getCode());
        assertEquals("The operator + is undefined for the type(s) Decimal, Money.", msg.getText());
    }

    @Test
    public void testGetFunctions() {
        DefaultFunctionResolver<DummyCodeFragment> r1 = new DefaultFunctionResolver<DummyCodeFragment>();
        FlFunction<DummyCodeFragment> f1 = createFunction("IF1", new Datatype[] { Datatype.PRIMITIVE_BOOLEAN,
                AnyDatatype.INSTANCE, AnyDatatype.INSTANCE });
        r1.add(f1);
        DefaultFunctionResolver<DummyCodeFragment> r2 = new DefaultFunctionResolver<DummyCodeFragment>();
        FlFunction<DummyCodeFragment> f2 = createFunction("IF2", new Datatype[] { Datatype.PRIMITIVE_BOOLEAN,
                AnyDatatype.INSTANCE, AnyDatatype.INSTANCE });
        FlFunction<DummyCodeFragment> f3 = createFunction("IF3", new Datatype[] { Datatype.PRIMITIVE_BOOLEAN,
                AnyDatatype.INSTANCE, AnyDatatype.INSTANCE });
        r2.add(f2);
        r2.add(f3);
        FlFunction<DummyCodeFragment>[] functions = compiler.getFunctions();
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

        FlFunction<DummyCodeFragment> function1 = createFunction("function1", argTypesDecimal);
        FlFunction<DummyCodeFragment> function2 = createFunction(matchingFunctionName, argTypesDecimal);
        FlFunction<DummyCodeFragment> function3 = createFunction(matchingFunctionName, argTypesDecimal);
        FlFunction<DummyCodeFragment> function4 = createFunction(matchingFunctionName, argTypesString);
        FlFunction<DummyCodeFragment> function5 = createFunction("function5", argTypesDecimal);

        matchingFunctions(function2, function3);

        @SuppressWarnings("unchecked")
        FunctionResolver<DummyCodeFragment> fctResolver1 = createFunctionResolver(function1, function2);
        @SuppressWarnings("unchecked")
        FunctionResolver<DummyCodeFragment> fctResolver2 = createFunctionResolver(function3, function4, function5);

        compiler.add(fctResolver1);
        compiler.add(fctResolver2);

        Set<FlFunction<DummyCodeFragment>> ambiguousFunctions = compiler.getAmbiguousFunctions(compiler.getFunctions());

        assertEquals(2, ambiguousFunctions.size());

        assertTrue(ambiguousFunctions.contains(function2));
        assertTrue(ambiguousFunctions.contains(function3));
    }

    @Test
    public void testIdentifierResolvingFailed() {
        setFailingIdentifierResolver();
        CompilationResult<DummyCodeFragment> result = compiler.compile("a");
        assertTrue(result.failed());
        assertEquals(1, result.getMessages().size());
        Message msg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, msg.getCode());
    }

    private void setFailingIdentifierResolver() {
        compiler.setIdentifierResolver(new IdentifierResolver<DummyCodeFragment>() {

            public CompilationResult<DummyCodeFragment> compile(String identifier,
                    ExprCompiler<DummyCodeFragment> exprCompiler,
                    Locale locale) {
                String text = ExprCompiler.LOCALIZED_STRINGS.getString(ExprCompiler.UNDEFINED_IDENTIFIER, locale,
                        identifier);
                DummyCompilationResultImpl compilationResult = new DummyCompilationResultImpl(Message.newError(
                        ExprCompiler.UNDEFINED_IDENTIFIER, text));
                compilationResult.addIdentifierUsed(identifier);
                return compilationResult;
            }
        });
    }

    @Test
    public void testIdentifierResolvingSuccessfull() {
        compiler.setIdentifierResolver(new IdentifierResolver<DummyCodeFragment>() {

            public CompilationResult<DummyCodeFragment> compile(String identifier,
                    ExprCompiler<DummyCodeFragment> exprCompiler,
                    Locale locale) {
                return new DummyCompilationResultImpl(new DummyCodeFragment("IDENTIFIER " + identifier),
                        Datatype.STRING);
            }
        });
        CompilationResult<DummyCodeFragment> result = compiler.compile("a");
        assertTrue(result.successfull());
        assertTrue(result.getCodeFragment().getSourcecode().startsWith("IDENTIFIER a"));
        assertEquals(Datatype.STRING, result.getDatatype());
    }

    @Test
    public void testUsedIdentifiers() {
        setIntIdentifierResolver();
        registerAddIntInt();

        CompilationResult<DummyCodeFragment> result = compiler.compile("1");
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

    private void setIntIdentifierResolver() {
        compiler.setIdentifierResolver(new IdentifierResolver<DummyCodeFragment>() {
            public CompilationResult<DummyCodeFragment> compile(String identifier,
                    ExprCompiler<DummyCodeFragment> exprCompiler,
                    Locale locale) {
                DummyCompilationResultImpl compilationResult = new DummyCompilationResultImpl(identifier,
                        Datatype.PRIMITIVE_INT);
                // the identifier is always used as parameter
                compilationResult.addIdentifierUsed(identifier);
                return compilationResult;
            }

        });
    }

    private void registerAddIntInt() {
        compiler.register(new AbstractBinaryOperation<DummyCodeFragment>("+", Datatype.PRIMITIVE_INT,
                Datatype.PRIMITIVE_INT) {

            public CompilationResult<DummyCodeFragment> generate(CompilationResult<DummyCodeFragment> lhs,
                    CompilationResult<DummyCodeFragment> rhs) {
                lhs.getCodeFragment().append(" + "); //$NON-NLS-1$
                ((AbstractCompilationResult<DummyCodeFragment>)lhs).add(rhs);
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
        CompilationResult<DummyCodeFragment> result = compiler.compile("a + 2");
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
        CompilationResult<DummyCodeFragment> result = compiler.compile("a + 2");
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
        CompilationResult<DummyCodeFragment> result = compiler.compile("a + b");
        assertTrue(result.failed());
        assertEquals(2, result.getMessages().size());
        Message lhsMsg = result.getMessages().getMessage(0);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, lhsMsg.getCode());
        Message rhsMsg = result.getMessages().getMessage(1);
        assertEquals(ExprCompiler.UNDEFINED_IDENTIFIER, rhsMsg.getCode());
    }

    private FunctionResolver<DummyCodeFragment> createFunctionResolver(FlFunction<DummyCodeFragment>... functions) {
        @SuppressWarnings("unchecked")
        FunctionResolver<DummyCodeFragment> newFctResolver = mock(FunctionResolver.class);

        when(newFctResolver.getFunctions()).thenReturn(functions);

        return newFctResolver;
    }

    private FlFunction<DummyCodeFragment> createFunction(String name, Datatype[] argTypes) {
        @SuppressWarnings("unchecked")
        FlFunction<DummyCodeFragment> newFunction = mock(FlFunction.class);

        when(newFunction.getName()).thenReturn(name);
        when(newFunction.getArgTypes()).thenReturn(argTypes);
        when(newFunction.getType()).thenReturn(Datatype.INTEGER);

        return newFunction;
    }

    private void matchingFunctions(FlFunction<DummyCodeFragment> matchingFunction,
            FlFunction<DummyCodeFragment> newFunction) {
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
