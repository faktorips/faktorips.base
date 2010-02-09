/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.fl;

import java.util.Iterator;

import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
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
 * Visitor that visits the parse tree and generates the Java sourcecode that
 * represents the expression in Java.
 */
class ParseTreeVisitor implements FlParserVisitor {
    
    private ExprCompiler compiler;
    
    ParseTreeVisitor(ExprCompiler compiler) {
        this.compiler = compiler;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.SimpleNode, java.lang.Object)
     */
    public Object visit(SimpleNode node, Object data) {
        return null;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTStart, java.lang.Object)
     */
    public Object visit(ASTStart node, Object data) {
		SimpleNode childNode = (SimpleNode) node.jjtGetChild(0); 
		return childNode.jjtAccept(this,data);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTEQNode, java.lang.Object)
     */
    public Object visit(ASTEQNode node, Object data) {
        return generateBinaryOperation("=", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTNotEQNode, java.lang.Object)
     */
    public Object visit(ASTNotEQNode node, Object data) {
        return generateBinaryOperation("!=", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTLTNode, java.lang.Object)
     */
    public Object visit(ASTLTNode node, Object data) {
        return generateBinaryOperation("<", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTGTNode, java.lang.Object)
     */
    public Object visit(ASTGTNode node, Object data) {
        return generateBinaryOperation(">", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTLENode, java.lang.Object)
     */
    public Object visit(ASTLENode node, Object data) {
        return generateBinaryOperation("<=", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTGENode, java.lang.Object)
     */
    public Object visit(ASTGENode node, Object data) {
        return generateBinaryOperation(">=", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTAddNode, java.lang.Object)
     */
    public Object visit(ASTAddNode node, Object data) {
        return generateBinaryOperation("+", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTSubNode, java.lang.Object)
     */
    public Object visit(ASTSubNode node, Object data) {
        return generateBinaryOperation("-", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMultNode, java.lang.Object)
     */
    public Object visit(ASTMultNode node, Object data) {
        return generateBinaryOperation("*", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTDivNode, java.lang.Object)
     */
    public Object visit(ASTDivNode node, Object data) {
        return generateBinaryOperation("/", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTPlusNode, java.lang.Object)
     */
    public Object visit(ASTPlusNode node, Object data) {
        return generateUnaryOperation("+", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMinusNode, java.lang.Object)
     */
    public Object visit(ASTMinusNode node, Object data) {
        return generateUnaryOperation("-", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTNotNode, java.lang.Object)
     */
    public Object visit(ASTNotNode node, Object data) {
        return generateUnaryOperation("!", node, data); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTParenthesisNode, java.lang.Object)
     */
    public Object visit(ASTParenthesisNode node, Object data) {
        return generateUnaryOperation("()", node, data);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTIdentifierNode, java.lang.Object)
     */
    public Object visit(ASTIdentifierNode node, Object data) {
        String identifier = node.getLastToken().toString();
        CompilationResultImpl result = (CompilationResultImpl)compiler.getIdentifierResolver().compile(identifier, compiler, compiler.getLocale());
        if (! result.failed()){
            // add the identifier only if there are no errors in the compilation result
            result.addIdentifierUsed(identifier); // note: add method does not create duplicates
        }
        return result;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTBooleanNode, java.lang.Object)
     */
    public Object visit(ASTBooleanNode node, Object data) {
        return generateConstant(node, DatatypeHelper.PRIMITIVE_BOOLEAN);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTIntegerNode, java.lang.Object)
     */
    public Object visit(ASTIntegerNode node, Object data) {
        return generateConstant(node, DatatypeHelper.PRIMITIVE_INTEGER);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTDecimalNode, java.lang.Object)
     */
    public Object visit(ASTDecimalNode node, Object data) {
        return generateConstant(node, DatatypeHelper.DECIMAL);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTStringNode, java.lang.Object)
     */
    public Object visit(ASTStringNode node, Object data) {
		String value = node.getLastToken().toString();
        // note: we can't use generateConstant here because value contains
		// the String value including double quotes, but the StringHelper class
		// expects the value without.
		return new CompilationResultImpl(value, Datatype.STRING);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMoneyNode, java.lang.Object)
     */
    public Object visit(ASTMoneyNode node, Object data) {
        boolean isParable = ((ValueDatatype)DatatypeHelper.MONEY.getDatatype()).isParsable(node.getLastToken().toString());
        if(isParable){
            return generateConstant(node, DatatypeHelper.MONEY);
        }
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.WRONG_MONEY_LITERAL, compiler.getLocale(), node.getLastToken().toString());
        return new CompilationResultImpl(Message.newError(ExprCompiler.SYNTAX_ERROR, text));
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTNullNode, java.lang.Object)
     */
    public Object visit(ASTNullNode node, Object data) {
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.NULL_NOT_ALLOWED, compiler.getLocale());
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTMethodCallNode, java.lang.Object)
     */
    public Object visit(ASTFunctionCallNode node, Object data) {
        
        String fctName = node.getFirstToken().toString();
        
        CompilationResultImpl[] argResults;
		if (node.jjtGetNumChildren() == 0) {
		    argResults = new CompilationResultImpl[0];
		} else {
		    argResults = (CompilationResultImpl[])node.jjtGetChild(0).jjtAccept(this, data);
		}
		
		// compilation errors in the result?
		CompilationResultImpl result = new CompilationResultImpl(); 
		for (int i=0; i<argResults.length; i++) {
		    if (argResults[i].failed()) {
		        result.addMessages(argResults[i].getMessages());
		    }
		}
		if (result.failed()) {
		    return result;
		}
		
        Datatype[] argTypes = CompilationResultImpl.getDatatypes(argResults);
		
		FlFunction function = null; // function that matches using implicit conversions
		boolean functionFoundByName = false;
        for(Iterator it=compiler.getFunctionResolvers(); it.hasNext();) {
            FunctionResolver resolver = (FunctionResolver)it.next();
            FlFunction[] functions = resolver.getFunctions();
            for (int i=0; i<functions.length; i++) {
                if (functions[i].match(fctName, argTypes)) {
                    return functions[i].compile(argResults);
                } else if (functions[i].matchUsingConversion(fctName, argTypes, compiler.getConversionCodeGenerator())) {
                    function = functions[i];
                } else if (!functionFoundByName && functions[i].getName().equals(fctName)) {
                      functionFoundByName = true;
                }
            }
        }
        if (function!=null) {
            return function.compile(convert(function, argResults));
        }
        
        // if the function name is defined but the argument types are wrong
        // generate a ExprCompiler.WRONG_ARGUMENT_TYPES error message. 
        if (functionFoundByName) {
            String[] replacements = new String[] {fctName, argTypesToString(argResults)};
            String text = ExprCompiler.localizedStrings.getString(ExprCompiler.WRONG_ARGUMENT_TYPES, compiler.getLocale(), replacements);
            return new CompilationResultImpl(Message.newError(ExprCompiler.WRONG_ARGUMENT_TYPES, text));
        }
        
        // The function is undefined. Generate a ExprCompiler.UNDEFINED_FUNCTION errror message
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.UNDEFINED_FUNCTION, compiler.getLocale(), fctName);
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_FUNCTION, text));
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.parser.FlParserVisitor#visit(org.faktorips.fl.parser.ASTArgListNode, java.lang.Object)
     */
    public Object visit(ASTArgListNode node, Object data) {
		int numOfArgs = node.jjtGetNumChildren();
		CompilationResultImpl[] argListResult = new CompilationResultImpl[numOfArgs]; 

		for (int i = 0; i < numOfArgs; i++)
		{
			SimpleNode argNode = (SimpleNode) node.jjtGetChild(i);
			argListResult[i] = (CompilationResultImpl) argNode.jjtAccept(this, data);
		}
		return argListResult;
    }
    
    private CompilationResultImpl generateConstant(SimpleNode node, DatatypeHelper helper) {
        String value = node.getLastToken().toString();
        return new CompilationResultImpl(helper.newInstance(value), helper.getDatatype());
    }
    
    private CompilationResultImpl generateUnaryOperation(
            String operator, SimpleNode node, Object data) {
        
		SimpleNode argNode = (SimpleNode) node.jjtGetChild(0);
		CompilationResultImpl argResult = (CompilationResultImpl) argNode.jjtAccept(this, data);
		
		if (argResult.failed()) {
	        return argResult;
		}

	    UnaryOperation operation = null; 
        UnaryOperation[] operations = compiler.getUnaryOperations(operator);
        for (int i=0; i<operations.length; i++) {
            // exact match?
            if (operations[i].getDatatype().equals(argResult.getDatatype())) {
                CompilationResultImpl compilationResult = operations[i].generate(argResult);
                compilationResult.addIdentifiersUsed(argResult.getIdentifiersUsedAsSet());
                return compilationResult;                
            }
            // match with implicit casting
            if (compiler.getConversionCodeGenerator().canConvert(argResult.getDatatype(), operations[i].getDatatype())) {
                operation = operations[i];
            }
        }
        if (operation!=null) {
            // use operation with implicit casting
            JavaCodeFragment converted = compiler.getConversionCodeGenerator().
        	getConversionCode(argResult.getDatatype(), operation.getDatatype(), argResult.getCodeFragment()); 
            CompilationResultImpl convertedArgResult = new CompilationResultImpl(converted, operation.getDatatype());
            convertedArgResult.addMessages(argResult.getMessages());
            CompilationResultImpl compilationResult = operation.generate(convertedArgResult);
            compilationResult.addIdentifiersUsed(argResult.getIdentifiersUsedAsSet());
            return compilationResult;
        }
        Object[] replacements = new Object[]{operator, argResult.getDatatype().getName()}; 
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.UNDEFINED_OPERATOR, compiler.getLocale(), replacements); 
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_OPERATOR, text));
    }

    private CompilationResultImpl generateBinaryOperation(
            String operator, SimpleNode node, Object data) {
        
		SimpleNode lhsNode = (SimpleNode) node.jjtGetChild(0);
		SimpleNode rhsNode = (SimpleNode) node.jjtGetChild(1);
		CompilationResultImpl lhsResult = (CompilationResultImpl) lhsNode.jjtAccept(this, data);
		CompilationResultImpl rhsResult = (CompilationResultImpl) rhsNode.jjtAccept(this, data);
		
		if (lhsResult.failed()) {
	        lhsResult.addMessages(rhsResult.getMessages());
	        return lhsResult;
		}
	    if (rhsResult.failed()) {
	        return rhsResult;
		}

	    BinaryOperation operation = null; 
        BinaryOperation[] operations = compiler.getBinaryOperations(operator);
        for (int i=0; i<operations.length; i++) {
            // exact match?
            if (operations[i].getLhsDatatype().equals(lhsResult.getDatatype())
                && operations[i].getRhsDatatype().equals(rhsResult.getDatatype())) {
                CompilationResultImpl compilationResult = operations[i].generate(lhsResult, rhsResult);
                compilationResult.addIdentifiersUsed(lhsResult.getIdentifiersUsedAsSet());
                compilationResult.addIdentifiersUsed(rhsResult.getIdentifiersUsedAsSet());
                return compilationResult;
            }
            // match with implicit casting
            if (compiler.getConversionCodeGenerator().canConvert(lhsResult.getDatatype(), operations[i].getLhsDatatype())
                && compiler.getConversionCodeGenerator().canConvert(rhsResult.getDatatype(), operations[i].getRhsDatatype())
                && operation==null) { // we use the operation that matches with code conversion
                operation = operations[i];
            }
        }
        if (operation!=null) {
            // use operation with implicit casting
            CompilationResultImpl convertedLhsResult = lhsResult;
            if (!lhsResult.getDatatype().equals(operation.getLhsDatatype()) 
                    && (!(operation.getLhsDatatype() instanceof AnyDatatype)) ) {
                JavaCodeFragment convertedLhs = compiler.getConversionCodeGenerator().
            	    getConversionCode(lhsResult.getDatatype(), operation.getLhsDatatype(), lhsResult.getCodeFragment()); 
                convertedLhsResult = new CompilationResultImpl(convertedLhs, operation.getLhsDatatype());
                convertedLhsResult.addMessages(lhsResult.getMessages());
                convertedLhsResult.addIdentifiersUsed(lhsResult.getIdentifiersUsedAsSet());
            }
            CompilationResultImpl convertedRhsResult = rhsResult;
            if (!rhsResult.getDatatype().equals(operation.getRhsDatatype())
                && (!(operation.getRhsDatatype() instanceof AnyDatatype)) ) {
	            JavaCodeFragment convertedRhs = compiler.getConversionCodeGenerator().
	        		getConversionCode(rhsResult.getDatatype(), operation.getRhsDatatype(), rhsResult.getCodeFragment());
	            convertedRhsResult = new CompilationResultImpl(convertedRhs, operation.getRhsDatatype());
	            convertedRhsResult.addMessages(rhsResult.getMessages());
                convertedRhsResult.addIdentifiersUsed(rhsResult.getIdentifiersUsedAsSet());
            }
            CompilationResultImpl result = operation.generate(convertedLhsResult, convertedRhsResult);
            result.addIdentifiersUsed(convertedLhsResult.getIdentifiersUsedAsSet());
            result.addIdentifiersUsed(convertedRhsResult.getIdentifiersUsedAsSet());
            return result;
        }
        Object[] replacements = new Object[]{operator, lhsResult.getDatatype().getName() + ", " + rhsResult.getDatatype().getName()};  //$NON-NLS-1$
        String text = ExprCompiler.localizedStrings.getString(ExprCompiler.UNDEFINED_OPERATOR, compiler.getLocale(), replacements); 
        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_OPERATOR, text));
    }

    private String argTypesToString(CompilationResult[] results) {
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<results.length; i++) {
            if (i>0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(results[i].getDatatype().getName());
        }
        return buffer.toString();
    }
    
    private CompilationResult[] convert(
            FlFunction flFunction,
            CompilationResult[] argResults) {
        
        ConversionCodeGenerator conversionCg = compiler.getConversionCodeGenerator();        
        CompilationResultImpl[] convertedArgs = new CompilationResultImpl[argResults.length];
        for (int i=0; i<argResults.length; i++) {
            
            Datatype functionDatatype = flFunction.hasVarArgs() ? flFunction.getArgTypes()[0] : flFunction.getArgTypes()[i];
            if (functionDatatype instanceof AnyDatatype) {
                convertedArgs[i] = (CompilationResultImpl)argResults[i];
            } else {
                JavaCodeFragment fragment = conversionCg.getConversionCode(
                        argResults[i].getDatatype(), functionDatatype, argResults[i].getCodeFragment());
                convertedArgs[i] = new CompilationResultImpl(fragment, functionDatatype);
                convertedArgs[i].addMessages(argResults[i].getMessages());
                if (argResults[i] instanceof CompilationResultImpl){
                    convertedArgs[i].addIdentifiersUsed(((CompilationResultImpl)argResults[i]).getIdentifiersUsedAsSet());
                }
            }
        }
        return convertedArgs;
    }

    

}
