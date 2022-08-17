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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.CodeFragment;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.parser.ASTAddNode;
import org.faktorips.fl.parser.ASTArgListNode;
import org.faktorips.fl.parser.ASTBooleanNode;
import org.faktorips.fl.parser.ASTDecimalNode;
import org.faktorips.fl.parser.ASTDivNode;
import org.faktorips.fl.parser.ASTEQNode;
import org.faktorips.fl.parser.ASTFunctionCallNode;
import org.faktorips.fl.parser.ASTGENode;
import org.faktorips.fl.parser.ASTGTNode;
import org.faktorips.fl.parser.ASTIdentifierNode;
import org.faktorips.fl.parser.ASTIntegerNode;
import org.faktorips.fl.parser.ASTLENode;
import org.faktorips.fl.parser.ASTLTNode;
import org.faktorips.fl.parser.ASTMinusNode;
import org.faktorips.fl.parser.ASTMoneyNode;
import org.faktorips.fl.parser.ASTMultNode;
import org.faktorips.fl.parser.ASTNotEQNode;
import org.faktorips.fl.parser.ASTNotNode;
import org.faktorips.fl.parser.ASTNullNode;
import org.faktorips.fl.parser.ASTParenthesisNode;
import org.faktorips.fl.parser.ASTPlusNode;
import org.faktorips.fl.parser.ASTStart;
import org.faktorips.fl.parser.ASTStringNode;
import org.faktorips.fl.parser.ASTSubNode;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.fl.parser.Token;
import org.faktorips.runtime.Message;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ParseTreeVisitor}.
 */
public class ParseTreeVisitorTest {

    private ParseTreeVisitor<CodeFragment> visitor;
    private ExprCompiler<CodeFragment> compiler;

    @Before
    public void setUp() {
        compiler = new DummyExprCompiler();
        compiler.setDatatypeHelperProvider(new DummyDatatypeHelperProvider());
        visitor = new DummyParseTreeVisitor(compiler);
    }

    private static class DummyCompilationResultImpl extends AbstractCompilationResult<CodeFragment> {

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

    private class DummyDatatypeHelperProvider implements DatatypeHelperProvider<CodeFragment> {

        @Override
        public BaseDatatypeHelper<CodeFragment> getDatatypeHelper(Datatype datatype) {
            BaseDatatypeHelper<CodeFragment> datatypeHelper = new BaseDatatypeHelper<>() {

                @SuppressWarnings("hiding")
                private Datatype datatype;

                @Override
                public Datatype getDatatype() {
                    return datatype;
                }

                @Override
                public void setDatatype(Datatype datatype) {
                    this.datatype = datatype;
                }

                @Override
                public CodeFragment nullExpression() {
                    return new CodeFragment("null");
                }

                @Override
                public CodeFragment newInstance(String value) {
                    return new CodeFragment("CONSTANT " + value);
                }

                @Override
                public CodeFragment newInstanceFromExpression(String expression) {
                    return new CodeFragment("CONSTANT " + expression);
                }

                @Override
                public CodeFragment newInstanceFromExpression(String expression, boolean checkForNull) {
                    return new CodeFragment("CONSTANT " + expression);
                }

                @Override
                public CodeFragment getToStringExpression(String fieldName) {
                    return new CodeFragment(fieldName);
                }

            };
            datatypeHelper.setDatatype(datatype);
            return datatypeHelper;
        }

    }

    private static class DummyParseTreeVisitor extends ParseTreeVisitor<CodeFragment> {

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

    private static class DummyExprCompiler extends ExprCompiler<CodeFragment> {

        @Override
        protected void registerDefaults() {
            // no defaults
        }

        @Override
        protected CodeFragment convertPrimitiveToWrapper(Datatype resultType, CodeFragment codeFragment) {
            return null;
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

    }

    @Test
    public void testVisitSimpleNodeObject() {
        SimpleNode node = mock(SimpleNode.class);
        Object result = visitor.visit(node, "Foo");

        assertNull(result);
    }

    @Test
    public void testVisitASTStartObject() {
        ASTStart node = mock(ASTStart.class);
        SimpleNode child = mock(SimpleNode.class);
        when(node.jjtGetChild(0)).thenReturn(child);
        String data = "Foo";

        visitor.visit(node, data);

        verify(child).jjtAccept(visitor, data);
    }

    @Test
    public void testVisitASTEQNodeObject() {
        testVisitAstForBinaryMethod(ASTEQNode.class, BinaryOperation.EQUAL);
    }

    private <T extends SimpleNode> void testVisitAstForBinaryMethod(Class<T> clazz, String operand) {
        T node = mock(clazz);
        String data = "FooBar";
        DummyCompilationResultImpl lhs = new DummyCompilationResultImpl("\"Foo\"", Datatype.STRING);
        SimpleNode lhsNode = mockChildNode(node, 0, data, lhs);
        DummyCompilationResultImpl rhs = new DummyCompilationResultImpl("\"Bar\"", Datatype.STRING);
        SimpleNode rhsNode = mockChildNode(node, 1, data, rhs);
        BinaryOperation<CodeFragment> operation = mockBinaryOperation(operand, Datatype.STRING, Datatype.STRING);
        DummyCompilationResultImpl resultImpl = new DummyCompilationResultImpl("\"Foo\" " + operand + " \"Bar\"",
                Datatype.STRING);
        when(operation.generate(lhs, rhs)).thenReturn(resultImpl);

        Object result = null;
        try {
            result = visitor.getClass().getMethod("visit", clazz, Object.class).invoke(visitor, node, data);
        } catch (Exception e) {
            fail("Theres's no visit method for type " + clazz);
        }

        verify(lhsNode).jjtAccept(visitor, data);
        verify(rhsNode).jjtAccept(visitor, data);
        verify(operation).generate(lhs, rhs);
        assertEquals("Not the expected compilation result", resultImpl, result);
    }

    private <T extends SimpleNode> void testVisitAstForUnaryMethod(Class<T> clazz, String operand) {
        T node = mock(clazz);
        String data = "FooBar";
        DummyCompilationResultImpl arg = new DummyCompilationResultImpl("\"Foo\"", Datatype.STRING);
        SimpleNode childNode = mockChildNode(node, 0, data, arg);
        UnaryOperation<CodeFragment> operation = mockUnaryOperation(operand, Datatype.STRING);
        DummyCompilationResultImpl resultImpl = new DummyCompilationResultImpl(operand + " \"Foo\"", Datatype.STRING);
        when(operation.generate(arg)).thenReturn(resultImpl);

        Object result = null;
        try {
            result = visitor.getClass().getMethod("visit", clazz, Object.class).invoke(visitor, node, data);
        } catch (Exception e) {
            fail("Theres's no visit method for type " + clazz);
        }
        verify(childNode).jjtAccept(visitor, data);
        verify(operation).generate(arg);
        assertEquals("Not the expected compilation result", resultImpl, result);
    }

    private SimpleNode mockChildNode(SimpleNode parentNode, int index, String data, Object compilationResult) {
        SimpleNode lhsNode = mock(SimpleNode.class);
        when(parentNode.jjtGetChild(index)).thenReturn(lhsNode);
        when(lhsNode.jjtAccept(visitor, data)).thenReturn(compilationResult);
        when(parentNode.jjtGetNumChildren()).thenReturn(index + 1);
        return lhsNode;
    }

    private BinaryOperation<CodeFragment> mockBinaryOperation(String operator,
            Datatype lhsDatatype,
            Datatype rhsDatatype) {
        @SuppressWarnings("unchecked")
        BinaryOperation<CodeFragment> operation = mock(BinaryOperation.class);
        when(operation.getOperator()).thenReturn(operator);
        when(operation.getLhsDatatype()).thenReturn(lhsDatatype);
        when(operation.getRhsDatatype()).thenReturn(rhsDatatype);
        compiler.register(operation);
        return operation;
    }

    private UnaryOperation<CodeFragment> mockUnaryOperation(String operator, Datatype datatype) {
        @SuppressWarnings("unchecked")
        UnaryOperation<CodeFragment> operation = mock(UnaryOperation.class);
        when(operation.getOperator()).thenReturn(operator);
        when(operation.getDatatype()).thenReturn(datatype);
        compiler.register(operation);
        return operation;
    }

    @Test
    public void testVisitASTNotEQNodeObject() {
        testVisitAstForBinaryMethod(ASTNotEQNode.class, BinaryOperation.NOT_EQUAL);
    }

    @Test
    public void testVisitASTLTNodeObject() {
        testVisitAstForBinaryMethod(ASTLTNode.class, BinaryOperation.LESSER_THAN);
    }

    @Test
    public void testVisitASTGTNodeObject() {
        testVisitAstForBinaryMethod(ASTGTNode.class, BinaryOperation.GREATER_THAN);
    }

    @Test
    public void testVisitASTLENodeObject() {
        testVisitAstForBinaryMethod(ASTLENode.class, BinaryOperation.LESSER_THAN_OR_EQUAL);
    }

    @Test
    public void testVisitASTGENodeObject() {
        testVisitAstForBinaryMethod(ASTGENode.class, BinaryOperation.GREATER_THAN_OR_EQUAL);
    }

    @Test
    public void testVisitASTAddNodeObject() {
        testVisitAstForBinaryMethod(ASTAddNode.class, BinaryOperation.PLUS);
    }

    @Test
    public void testVisitASTSubNodeObject() {
        testVisitAstForBinaryMethod(ASTSubNode.class, BinaryOperation.MINUS);
    }

    @Test
    public void testVisitASTMultNodeObject() {
        testVisitAstForBinaryMethod(ASTMultNode.class, BinaryOperation.MULTIPLY);
    }

    @Test
    public void testVisitASTDivNodeObject() {
        testVisitAstForBinaryMethod(ASTDivNode.class, BinaryOperation.DIVIDE);
    }

    @Test
    public void testVisitASTPlusNodeObject() {
        testVisitAstForUnaryMethod(ASTPlusNode.class, BinaryOperation.PLUS);
    }

    @Test
    public void testVisitASTMinusNodeObject() {
        testVisitAstForUnaryMethod(ASTMinusNode.class, BinaryOperation.MINUS);
    }

    @Test
    public void testVisitASTNotNodeObject() {
        testVisitAstForUnaryMethod(ASTNotNode.class, UnaryOperation.NOT);
    }

    @Test
    public void testVisitASTParenthesisNodeObject() {
        testVisitAstForUnaryMethod(ASTParenthesisNode.class, "()");
    }

    @Test
    public void testVisitASTIdentifierNodeObject() {
        ASTIdentifierNode node = mock(ASTIdentifierNode.class);
        String data = "FooBar";
        mockToken(node, data);
        @SuppressWarnings("unchecked")
        IdentifierResolver<CodeFragment> resolver = mock(IdentifierResolver.class);
        compiler.setIdentifierResolver(resolver);
        compiler.setLocale(Locale.KOREAN);
        DummyCompilationResultImpl resultImpl = new DummyCompilationResultImpl(data, Datatype.STRING);
        when(resolver.compile(data, compiler, Locale.KOREAN)).thenReturn(resultImpl);

        Object result = visitor.visit(node, data);

        verify(resolver).compile(data, compiler, Locale.KOREAN);
        assertEquals("Not the expected compilation result", resultImpl, result);
    }

    private void mockToken(SimpleNode node, String data) {
        Token value = mock(Token.class);
        when(value.toString()).thenReturn(data);
        when(node.getLastToken()).thenReturn(value);
        when(node.getFirstToken()).thenReturn(value);
    }

    @Test
    public void testVisitASTBooleanNodeObject() {
        ASTBooleanNode node = mock(ASTBooleanNode.class);
        String data = "false";
        mockToken(node, data);

        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        assertEquals("Not the expected data type", Datatype.PRIMITIVE_BOOLEAN, result.getDatatype());
        assertEquals("Not the expected code", "CONSTANT " + data, result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testVisitASTIntegerNodeObject() {
        ASTIntegerNode node = mock(ASTIntegerNode.class);
        String data = "1";
        mockToken(node, data);

        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        assertEquals("Not the expected data type", Datatype.PRIMITIVE_INT, result.getDatatype());
        assertEquals("Not the expected code", "CONSTANT " + data, result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testVisitASTDecimalNodeObject() {
        ASTDecimalNode node = mock(ASTDecimalNode.class);
        String data = "2.3";
        mockToken(node, data);

        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        assertEquals("Not the expected data type", Datatype.DECIMAL, result.getDatatype());
        assertEquals("Not the expected code", "CONSTANT " + data, result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testVisitASTStringNodeObject() {
        ASTStringNode node = mock(ASTStringNode.class);
        String data = "Foo";
        mockToken(node, data);

        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        assertEquals("Not the expected data type", Datatype.STRING, result.getDatatype());
        assertEquals("Not the expected code", data, result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testVisitASTMoneyNodeObject() {
        ASTMoneyNode node = mock(ASTMoneyNode.class);
        String data = "4.5 EUR";
        mockToken(node, data);

        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        assertEquals("Not the expected data type", Datatype.MONEY, result.getDatatype());
        assertEquals("Not the expected code", "CONSTANT " + data, result.getCodeFragment().getSourcecode());
    }

    @Test
    public void testVisitASTMoneyNodeObjectNotParsable() {
        ASTMoneyNode node = mock(ASTMoneyNode.class);
        String data = "4.5 €";
        mockToken(node, data);

        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        assertTrue("Compilation should have failed", result.failed());
    }

    @Test
    public void testVisitASTNullNodeObject() {
        ASTNullNode node = mock(ASTNullNode.class);
        String data = "null";
        mockToken(node, data);

        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        assertTrue("Compilation should have failed", result.failed());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testVisitASTFunctionCallNodeObject() {
        ASTFunctionCallNode node = mock(ASTFunctionCallNode.class);
        String data = "FooBar";
        mockToken(node, data);
        DummyCompilationResultImpl result1 = new DummyCompilationResultImpl("\"Foo\"", Datatype.STRING);
        DummyCompilationResultImpl result2 = new DummyCompilationResultImpl("\"Bar\"", Datatype.STRING);
        DummyCompilationResultImpl result3 = new DummyCompilationResultImpl("42", Datatype.PRIMITIVE_INT);
        AbstractCompilationResult<CodeFragment>[] compilationResultImpls = new AbstractCompilationResult[] { result1,
                result2, result3 };
        SimpleNode childNode = mockChildNode(node, 0, data, compilationResultImpls);
        DummyCompilationResultImpl resultImpl = new DummyCompilationResultImpl(data + "(\"Foo\", \"Bar\", 42)",
                Datatype.STRING);
        FlFunction<CodeFragment> function = mockFunction(data, new Datatype[] { Datatype.STRING, Datatype.STRING,
                Datatype.PRIMITIVE_INT }, resultImpl);
        mockFunctionResolver(function);
        compiler.setConversionCodeGenerator(new ConversionCodeGenerator<>());

        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        verify(childNode).jjtAccept(visitor, data);
        verify(function).compile(any(CompilationResult[].class));
        assertEquals("Not the expected compilation result", resultImpl, result);
    }

    @SuppressWarnings("unchecked")
    private void mockFunctionResolver(FlFunction<CodeFragment> function) {
        FunctionResolver<CodeFragment> fctResolver = mock(FunctionResolver.class);
        when(fctResolver.getFunctions()).thenReturn(new FlFunction[] { function });
        compiler.add(fctResolver);
    }

    @SuppressWarnings("unchecked")
    private FlFunction<CodeFragment> mockFunction(String data,
            Datatype[] datatypes,
            DummyCompilationResultImpl resultImpl) {
        FlFunction<CodeFragment> function = mock(FlFunction.class);
        when(function.match(data, datatypes)).thenReturn(true);
        when(function.compile(any(CompilationResult[].class))).thenReturn(resultImpl);
        return function;
    }

    @Test
    public void testVisitASTFunctionCallNodeObject_ChildFailed() {
        ASTFunctionCallNode node = mock(ASTFunctionCallNode.class);
        String data = "FooBar";
        mockToken(node, data);
        DummyCompilationResultImpl result1 = new DummyCompilationResultImpl("\"Foo\"", Datatype.STRING);
        DummyCompilationResultImpl result2 = new DummyCompilationResultImpl(Message.newError("ERROR", "failed"));
        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment>[] compilationResultImpls = new AbstractCompilationResult[] { result1,
                result2 };
        SimpleNode childNode = mockChildNode(node, 0, data, compilationResultImpls);

        @SuppressWarnings("unchecked")
        AbstractCompilationResult<CodeFragment> result = (AbstractCompilationResult<CodeFragment>)visitor.visit(node,
                data);

        verify(childNode).jjtAccept(visitor, data);
        assertTrue("Compilation should have failed", result.failed());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testVisitASTArgListNodeObject() {
        ASTArgListNode node = mock(ASTArgListNode.class);
        String data = "FooBar";
        mockToken(node, data);
        DummyCompilationResultImpl result1 = new DummyCompilationResultImpl("\"Foo\"", Datatype.STRING);
        DummyCompilationResultImpl result2 = new DummyCompilationResultImpl("\"Bar\"", Datatype.STRING);
        DummyCompilationResultImpl result3 = new DummyCompilationResultImpl("42", Datatype.PRIMITIVE_INT);
        SimpleNode childNode1 = mockChildNode(node, 0, data, result1);
        SimpleNode childNode2 = mockChildNode(node, 1, data, result2);
        SimpleNode childNode3 = mockChildNode(node, 2, data, result3);

        AbstractCompilationResult<CodeFragment>[] result = (AbstractCompilationResult<CodeFragment>[])visitor.visit(
                node, data);

        verify(childNode1).jjtAccept(visitor, data);
        verify(childNode2).jjtAccept(visitor, data);
        verify(childNode3).jjtAccept(visitor, data);
        assertEquals("Not the expected compilation result array length", 3, result.length);
        assertEquals("Not the expected compilation result at index 0", result1, result[0]);
        assertEquals("Not the expected compilation result at index 1", result2, result[1]);
        assertEquals("Not the expected compilation result at index 2", result3, result[2]);
    }

}
