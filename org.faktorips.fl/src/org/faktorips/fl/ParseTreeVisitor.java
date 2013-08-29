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

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.CodeFragment;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.AnyDatatype;
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
import org.faktorips.util.message.Message;

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
    public Object visit(SimpleNode node, Object data) {
        return null;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTStart,
     *      java.lang.Object)
     */
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
    public Object visit(ASTEQNode node, Object data) {
        return generateBinaryOperation("=", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTNotEQNode,
     *      java.lang.Object)
     */
    public Object visit(ASTNotEQNode node, Object data) {
        return generateBinaryOperation("!=", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTLTNode,
     *      java.lang.Object)
     */
    public Object visit(ASTLTNode node, Object data) {
        return generateBinaryOperation("<", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTGTNode,
     *      java.lang.Object)
     */
    public Object visit(ASTGTNode node, Object data) {
        return generateBinaryOperation(">", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTLENode,
     *      java.lang.Object)
     */
    public Object visit(ASTLENode node, Object data) {
        return generateBinaryOperation("<=", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTGENode,
     *      java.lang.Object)
     */
    public Object visit(ASTGENode node, Object data) {
        return generateBinaryOperation(">=", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTAddNode,
     *      java.lang.Object)
     */
    public Object visit(ASTAddNode node, Object data) {
        return generateBinaryOperation("+", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTSubNode,
     *      java.lang.Object)
     */
    public Object visit(ASTSubNode node, Object data) {
        return generateBinaryOperation("-", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMultNode,
     *      java.lang.Object)
     */
    public Object visit(ASTMultNode node, Object data) {
        return generateBinaryOperation("*", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTDivNode,
     *      java.lang.Object)
     */
    public Object visit(ASTDivNode node, Object data) {
        return generateBinaryOperation("/", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTPlusNode,
     *      java.lang.Object)
     */
    public Object visit(ASTPlusNode node, Object data) {
        return generateUnaryOperation("+", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMinusNode,
     *      java.lang.Object)
     */
    public Object visit(ASTMinusNode node, Object data) {
        return generateUnaryOperation("-", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTNotNode,
     *      java.lang.Object)
     */
    public Object visit(ASTNotNode node, Object data) {
        return generateUnaryOperation("!", node, data); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTParenthesisNode,
     *      java.lang.Object)
     */
    public Object visit(ASTParenthesisNode node, Object data) {
        return generateUnaryOperation("()", node, data);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTIdentifierNode,
     *      java.lang.Object)
     */
    public Object visit(ASTIdentifierNode node, Object data) {
        String identifier = node.getLastToken().toString();
        AbstractCompilationResult<T> result = (AbstractCompilationResult<T>)compiler.getIdentifierResolver().compile(
                identifier, compiler, compiler.getLocale());

        if (!result.failed()) {
            // add the identifier only if there are no errors in the compilation result
            Set<String> allIdentifiersInCurrentResult = result.getIdentifiersUsedAsSet();
            if (allIdentifiersInCurrentResult != null && allIdentifiersInCurrentResult.contains(identifier)) {
                // add the current identifier only if the compilation result knows the given
                // identifier candidate as identifier
                // e.g. enum constants must not be used as parameter identifier in formulas
                // see AbstractParameterIdentifierResolver#compileEnumDatatypeValueIdentifier
                result.addIdentifierUsed(identifier); // note: add method does not create duplicates
            }
        }
        return result;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTBooleanNode,
     *      java.lang.Object)
     */
    public Object visit(ASTBooleanNode node, Object data) {
        return generateConstant(node, Datatype.PRIMITIVE_BOOLEAN);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTIntegerNode,
     *      java.lang.Object)
     */
    public Object visit(ASTIntegerNode node, Object data) {
        return generateConstant(node, Datatype.PRIMITIVE_INT);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTDecimalNode,
     *      java.lang.Object)
     */
    public Object visit(ASTDecimalNode node, Object data) {
        return generateConstant(node, Datatype.DECIMAL);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTStringNode,
     *      java.lang.Object)
     */
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
    public Object visit(ASTFunctionCallNode node, Object data) {

        String fctName = node.getFirstToken().toString();

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

        // compilation errors in the result?
        AbstractCompilationResult<T> result = newCompilationResultImpl();
        for (AbstractCompilationResult<T> argResult : argResults) {
            if (argResult.failed()) {
                result.addMessages(argResult.getMessages());
            }
        }
        if (result.failed()) {
            return result;
        }

        Datatype[] argTypes = result.getDatatypes(argResults);

        // function that matches using implicit conversions
        FlFunction<T> function = null;
        boolean functionFoundByName = false;
        FlFunction<T>[] functions = compiler.getFunctions();
        LinkedHashSet<FlFunction<T>> ambiguousFunctions = compiler.getAmbiguousFunctions(functions);

        for (FlFunction<T> function2 : functions) {
            if (function2.match(fctName, argTypes)) {
                if (isAmbiguousFunction(function2, ambiguousFunctions)) {
                    return createAmbiguousFunctionCompilationResultImpl(function2);
                }
                return function2.compile(argResults);
            } else if (function2.matchUsingConversion(fctName, argTypes, compiler.getConversionCodeGenerator())) {
                function = function2;
            } else if (!functionFoundByName && function2.getName().equals(fctName)) {
                functionFoundByName = true;
            }
        }

        if (function != null) {
            if (isAmbiguousFunction(function, ambiguousFunctions)) {
                return createAmbiguousFunctionCompilationResultImpl(function);
            }
            return function.compile(convert(function, argResults));
        }

        // if the function name is defined but the argument types are wrong
        // generate a ExprCompiler.WRONG_ARGUMENT_TYPES error message.
        if (functionFoundByName) {
            Object[] replacements = new String[] { fctName, argTypesToString(argResults) };
            String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.WRONG_ARGUMENT_TYPES,
                    compiler.getLocale(), replacements);
            return newCompilationResultImpl(Message.newError(ExprCompiler.WRONG_ARGUMENT_TYPES, text));
        }

        // The function is undefined. Generate a ExprCompiler.UNDEFINED_FUNCTION error message
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.UNDEFINED_FUNCTION,
                compiler.getLocale(), fctName);
        return newCompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_FUNCTION, text));
    }

    private AbstractCompilationResult<T> createAmbiguousFunctionCompilationResultImpl(FlFunction<T> flFunction) {
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.AMBIGUOUS_FUNCTION_CALL,
                compiler.getLocale(), flFunction.getName());

        return newCompilationResultImpl(Message.newError(ExprCompiler.AMBIGUOUS_FUNCTION_CALL, text));
    }

    private boolean isAmbiguousFunction(FlFunction<T> flFunction, LinkedHashSet<FlFunction<T>> ambiguousFunctions) {
        for (FlFunction<T> ambiguousFlFunction : ambiguousFunctions) {
            if (flFunction.isSame(ambiguousFlFunction)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTArgListNode,
     *      java.lang.Object)
     */
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

        BinaryOperation<T> operation = null;
        BinaryOperation<T>[] operations = compiler.getBinaryOperations(operator);
        for (BinaryOperation<T> operation2 : operations) {
            // exact match?
            if (operation2.getLhsDatatype().equals(lhsResult.getDatatype())
                    && operation2.getRhsDatatype().equals(rhsResult.getDatatype())) {
                CompilationResult<T> compilationResult = operation2.generate(lhsResult, rhsResult);
                return compilationResult;
            }
            // match with implicit casting
            if (compiler.getConversionCodeGenerator().canConvert(lhsResult.getDatatype(), operation2.getLhsDatatype())
                    && compiler.getConversionCodeGenerator().canConvert(rhsResult.getDatatype(),
                            operation2.getRhsDatatype()) && operation == null) { // we use the
                // operation
                // that matches
                // with code
                // conversion
                operation = operation2;
            }
        }
        if (operation != null) {
            // use operation with implicit casting
            AbstractCompilationResult<T> convertedLhsResult = lhsResult;
            if (!lhsResult.getDatatype().equals(operation.getLhsDatatype())
                    && (!(operation.getLhsDatatype() instanceof AnyDatatype))) {
                T convertedLhs = compiler.getConversionCodeGenerator().getConversionCode(lhsResult.getDatatype(),
                        operation.getLhsDatatype(), lhsResult.getCodeFragment());
                convertedLhsResult = newCompilationResultImpl(convertedLhs, operation.getLhsDatatype());
                convertedLhsResult.addMessages(lhsResult.getMessages());
                convertedLhsResult.addIdentifiersUsed(lhsResult.getIdentifiersUsedAsSet());
            }
            AbstractCompilationResult<T> convertedRhsResult = rhsResult;
            if (!rhsResult.getDatatype().equals(operation.getRhsDatatype())
                    && (!(operation.getRhsDatatype() instanceof AnyDatatype))) {
                T convertedRhs = compiler.getConversionCodeGenerator().getConversionCode(rhsResult.getDatatype(),
                        operation.getRhsDatatype(), rhsResult.getCodeFragment());
                convertedRhsResult = newCompilationResultImpl(convertedRhs, operation.getRhsDatatype());
                convertedRhsResult.addMessages(rhsResult.getMessages());
                convertedRhsResult.addIdentifiersUsed(rhsResult.getIdentifiersUsedAsSet());
            }
            CompilationResult<T> result = operation.generate(convertedLhsResult, convertedRhsResult);
            return result;
        }
        Object[] replacements = new Object[] { operator,
                lhsResult.getDatatype().getName() + ", " + rhsResult.getDatatype().getName() }; //$NON-NLS-1$
        String text = ExprCompiler.getLocalizedStrings().getString(ExprCompiler.UNDEFINED_OPERATOR,
                compiler.getLocale(), replacements);
        return newCompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_OPERATOR, text));
    }

    private String argTypesToString(CompilationResult<T>[] results) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < results.length; i++) {
            if (i > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(results[i].getDatatype().getName());
        }
        return buffer.toString();
    }

    private CompilationResult<T>[] convert(FlFunction<T> flFunction, CompilationResult<T>[] argResults) {
        ConversionCodeGenerator<T> conversionCg = compiler.getConversionCodeGenerator();
        @SuppressWarnings("unchecked")
        AbstractCompilationResult<T>[] convertedArgs = new AbstractCompilationResult[argResults.length];
        for (int i = 0; i < argResults.length; i++) {

            Datatype functionDatatype = flFunction.hasVarArgs() ? flFunction.getArgTypes()[0] : flFunction
                    .getArgTypes()[i];
            if (functionDatatype instanceof AnyDatatype) {
                convertedArgs[i] = (AbstractCompilationResult<T>)argResults[i];
            } else {
                T fragment = conversionCg.getConversionCode(argResults[i].getDatatype(), functionDatatype,
                        argResults[i].getCodeFragment());
                convertedArgs[i] = newCompilationResultImpl(fragment, functionDatatype);
                convertedArgs[i].addMessages(argResults[i].getMessages());
                if (argResults[i] instanceof AbstractCompilationResult) {
                    convertedArgs[i].addIdentifiersUsed(((AbstractCompilationResult<T>)argResults[i])
                            .getIdentifiersUsedAsSet());
                }
            }
        }
        return convertedArgs;
    }

}
