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

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.CodeFragment;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
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
import org.faktorips.fl.parser.FlParserVisitor;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.runtime.Message;

/**
 * Visitor that visits the parse tree and generates the {@link CodeFragment source code} that
 * represents the expression in a target language.
 * 
 * @param <T> a {@link CodeFragment} implementation for a specific target language
 */
public abstract class ParseTreeVisitor<T extends CodeFragment> implements FlParserVisitor {

    private final ExprCompiler<T> compiler;

    protected ParseTreeVisitor(ExprCompiler<T> compiler) {
        this.compiler = compiler;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.SimpleNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(SimpleNode node, Object data) {
        return null;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTStart,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTStart node, Object data) {
        SimpleNode childNode = (SimpleNode)node.jjtGetChild(0);
        return childNode.jjtAccept(this, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTEQNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTEQNode node, Object data) {
        return generateBinaryOperation(BinaryOperation.EQUAL, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTNotEQNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTNotEQNode node, Object data) {
        return generateBinaryOperation(BinaryOperation.NOT_EQUAL, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTLTNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTLTNode node, Object data) {
        return generateBinaryOperation(BinaryOperation.LESSER_THAN, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTGTNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTGTNode node, Object data) {
        return generateBinaryOperation(BinaryOperation.GREATER_THAN, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTLENode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTLENode node, Object data) {
        return generateBinaryOperation(BinaryOperation.LESSER_THAN_OR_EQUAL, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTGENode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTGENode node, Object data) {
        return generateBinaryOperation(BinaryOperation.GREATER_THAN_OR_EQUAL, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTAddNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTAddNode node, Object data) {
        return generateBinaryOperation(BinaryOperation.PLUS, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTSubNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTSubNode node, Object data) {
        return generateBinaryOperation(BinaryOperation.MINUS, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMultNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTMultNode node, Object data) {
        return generateBinaryOperation(BinaryOperation.MULTIPLY, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTDivNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTDivNode node, Object data) {
        return generateBinaryOperation(BinaryOperation.DIVIDE, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTPlusNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTPlusNode node, Object data) {
        return generateUnaryOperation(BinaryOperation.PLUS, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMinusNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTMinusNode node, Object data) {
        return generateUnaryOperation(BinaryOperation.MINUS, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTNotNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTNotNode node, Object data) {
        return generateUnaryOperation(UnaryOperation.NOT, node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTParenthesisNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTParenthesisNode node, Object data) {
        return generateUnaryOperation("()", node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTIdentifierNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTIdentifierNode node, Object data) {
        String identifier = node.getLastToken().toString();
        AbstractCompilationResult<T> result = (AbstractCompilationResult<T>)compiler.getIdentifierResolver().compile(
                identifier, compiler, compiler.getLocale());
        return result;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTBooleanNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTBooleanNode node, Object data) {
        return generateConstant(node, Datatype.PRIMITIVE_BOOLEAN);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTIntegerNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTIntegerNode node, Object data) {
        return generateConstant(node, Datatype.PRIMITIVE_INT);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTDecimalNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTDecimalNode node, Object data) {
        return generateConstant(node, Datatype.DECIMAL);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTStringNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTStringNode node, Object data) {
        String value = node.getLastToken().toString();
        // note: we can't use generateConstant here because value contains
        // the String value including double quotes, but the StringHelper class
        // expects the value without.
        return newCompilationResultImpl(value, Datatype.STRING);
    }

    protected abstract AbstractCompilationResult<T> newCompilationResultImpl(String sourcecode, Datatype datatype);

    protected abstract AbstractCompilationResult<T> newCompilationResultImpl(T sourcecode, Datatype datatype);

    protected abstract AbstractCompilationResult<T> newCompilationResultImpl(Message message);

    protected abstract AbstractCompilationResult<T> newCompilationResultImpl();

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMoneyNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTMoneyNode node, Object data) {
        boolean isParsable = ((ValueDatatype)DatatypeHelper.MONEY.getDatatype()).isParsable(node.getLastToken()
                .toString());
        if (isParsable) {
            return generateConstant(node, Datatype.MONEY);
        }
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.WRONG_MONEY_LITERAL,
                compiler.getLocale(), node.getLastToken().toString());
        return newCompilationResultImpl(Message.newError(ExprCompiler.SYNTAX_ERROR, text));
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTNullNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTNullNode node, Object data) {
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.NULL_NOT_ALLOWED, compiler.getLocale());
        return newCompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTFunctionCallNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTFunctionCallNode node, Object data) {
        AbstractCompilationResult<T>[] argResults = getCompilationResultForFunctionArguments(node, data);
        AbstractCompilationResult<T> result = createNewResultAndCopyErrorMessagesIfExistent(argResults);
        if (result.failed()) {
            return result;
        }

        Datatype[] argTypes = result.getDatatypes(argResults);
        String fctName = node.getFirstToken().toString();

        return compiler.getMatchingFunctionUsingConversion(argResults, argTypes, fctName);
    }

    private AbstractCompilationResult<T> createNewResultAndCopyErrorMessagesIfExistent(
            AbstractCompilationResult<T>[] argResults) {
        // compilation errors in the result?
        AbstractCompilationResult<T> result = newCompilationResultImpl();
        for (AbstractCompilationResult<T> argResult : argResults) {
            if (argResult.failed()) {
                result.addMessages(argResult.getMessages());
            }
        }
        return result;
    }

    private AbstractCompilationResult<T>[] getCompilationResultForFunctionArguments(ASTFunctionCallNode node,
            Object data) {
        AbstractCompilationResult<T>[] argResults;
        if (node.jjtGetNumChildren() == 0) {
            @SuppressWarnings("unchecked")
            AbstractCompilationResult<T>[] compilationResultImpls = new AbstractCompilationResult[0];
            argResults = compilationResultImpls;
        } else {
            @SuppressWarnings("unchecked")
            AbstractCompilationResult<T>[] compilationResultImpls = (AbstractCompilationResult<T>[])node.jjtGetChild(0)
                    .jjtAccept(this, data);
            argResults = compilationResultImpls;
        }
        return argResults;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTArgListNode,
     *      java.lang.Object)
     */
    @Override
    public Object visit(ASTArgListNode node, Object data) {
        int numOfArgs = node.jjtGetNumChildren();
        @SuppressWarnings("unchecked")
        AbstractCompilationResult<T>[] argListResult = new AbstractCompilationResult[numOfArgs];

        for (int i = 0; i < numOfArgs; i++) {
            SimpleNode argNode = (SimpleNode)node.jjtGetChild(i);
            @SuppressWarnings("unchecked")
            AbstractCompilationResult<T> compilationResultImpl = (AbstractCompilationResult<T>)argNode.jjtAccept(this,
                    data);
            argListResult[i] = compilationResultImpl;
        }
        return argListResult;
    }

    protected CompilationResult<T> generateConstant(SimpleNode node, Datatype datatype) {
        BaseDatatypeHelper<T> helper = compiler.getDatatypeHelper(datatype);
        if (helper == null) {
            String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.DATATYPE_CREATION_ERROR,
                    compiler.getLocale(), datatype.getName());
            return newCompilationResultImpl(Message.newError(ExprCompiler.DATATYPE_CREATION_ERROR, text));
        }
        String value = node.getLastToken().toString();
        return newCompilationResultImpl(helper.newInstance(value), datatype);
    }

    private CompilationResult<T> generateUnaryOperation(String operator, SimpleNode node, Object data) {
        SimpleNode argNode = (SimpleNode)node.jjtGetChild(0);
        @SuppressWarnings("unchecked")
        AbstractCompilationResult<T> argResult = (AbstractCompilationResult<T>)argNode.jjtAccept(this, data);

        if (argResult.failed()) {
            return argResult;
        }

        UnaryOperation<T> operation = null;
        UnaryOperation<T>[] operations = compiler.getUnaryOperations(operator);
        for (UnaryOperation<T> operation2 : operations) {
            // exact match?
            if (operation2.getDatatype().equals(argResult.getDatatype())) {
                CompilationResult<T> compilationResult = operation2.generate(argResult);
                return compilationResult;
            }
            // match with implicit casting
            if (compiler.getConversionCodeGenerator().canConvert(argResult.getDatatype(), operation2.getDatatype())) {
                operation = operation2;
            }
        }
        if (operation != null) {
            // use operation with implicit casting
            T converted = compiler.getConversionCodeGenerator().getConversionCode(argResult.getDatatype(),
                    operation.getDatatype(), argResult.getCodeFragment());
            AbstractCompilationResult<T> convertedArgResult = newCompilationResultImpl(converted,
                    operation.getDatatype());
            convertedArgResult.addMessages(argResult.getMessages());
            CompilationResult<T> compilationResult = operation.generate(convertedArgResult);
            return compilationResult;
        }
        Object[] replacements = new Object[] { operator, argResult.getDatatype().getName() };
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.UNDEFINED_OPERATOR,
                compiler.getLocale(), replacements);
        return newCompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_OPERATOR, text));
    }

    private CompilationResult<T> generateBinaryOperation(String operator, SimpleNode node, Object data) {
        SimpleNode lhsNode = (SimpleNode)node.jjtGetChild(0);
        SimpleNode rhsNode = (SimpleNode)node.jjtGetChild(1);
        @SuppressWarnings("unchecked")
        AbstractCompilationResult<T> lhsResult = (AbstractCompilationResult<T>)lhsNode.jjtAccept(this, data);
        @SuppressWarnings("unchecked")
        AbstractCompilationResult<T> rhsResult = (AbstractCompilationResult<T>)rhsNode.jjtAccept(this, data);

        if (lhsResult.failed()) {
            lhsResult.addMessages(rhsResult.getMessages());
            return lhsResult;
        }
        if (rhsResult.failed()) {
            return rhsResult;
        }

        return compiler.getBinaryOperation(operator, lhsResult, rhsResult);
    }

}
