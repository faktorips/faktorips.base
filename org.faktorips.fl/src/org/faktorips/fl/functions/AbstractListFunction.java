/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.AbstractCompilationResult;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractListFunction extends AbstractFlFunction {

    private static final String MSG_CODE_INVALID_DATATYPE = ExprCompiler.PREFIX + "LIST_FUNCTION_INVALID_DATATYPE"; //$NON-NLS-1$

    public AbstractListFunction(String name, String description, FunctionSignatures signature) {
        super(name, description, signature);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        AbstractCompilationResult<JavaCodeFragment> listArgument = getListArgument(argResults);
        Datatype datatype = getBasicType(listArgument);
        CompilationResult<JavaCodeFragment> datatypeResult = validateBasicDatatype(datatype);
        if (compilationFailed(datatypeResult)) {
            return datatypeResult;
        } else {
            return generateFunctionCode(listArgument);
        }
    }

    protected AbstractCompilationResult<JavaCodeFragment> getListArgument(
            CompilationResult<JavaCodeFragment>[] argResults) {
        return (AbstractCompilationResult<JavaCodeFragment>)argResults[0];
    }

    private boolean compilationFailed(CompilationResult<JavaCodeFragment> datatypeResult) {
        return datatypeResult != null && datatypeResult.failed();
    }

    /**
     * @param basicDatatype The data type of the elements in the list this function processes.
     * 
     * @return a {@link CompilationResult} with an error if the element data type is illegal.
     *         Returns <code>null</code> or an error free compilation result if the data type is
     *         valid.
     */
    protected CompilationResult<JavaCodeFragment> validateBasicDatatype(Datatype basicDatatype) {
        return null;
    }

    protected CompilationResult<JavaCodeFragment> generateFunctionCode(
            CompilationResult<JavaCodeFragment> listArgument) {
        JavaCodeFragment fragment = new JavaCodeFragment();

        Datatype datatype = getBasicType(listArgument);
        CompilationResultImpl arg1Result = new CompilationResultImpl("currentResult", datatype);
        CompilationResultImpl arg2Result = new CompilationResultImpl("nextValue", datatype);

        String datatypeClassName = getJavaClassName(datatype);

        fragment.append("new ");
        fragment.appendClassName("org.faktorips.runtime.formula.FormulaEvaluatorUtil.FunctionWithListAsArgumentHelper");
        fragment.append("<");
        fragment.appendClassName(datatypeClassName);
        fragment.append(">(){\n@Override public ");
        fragment.appendClassName(datatypeClassName);
        fragment.append(" getPreliminaryResult(");
        fragment.appendClassName(datatypeClassName);
        fragment.append(" currentResult, ");
        fragment.appendClassName(datatypeClassName);
        fragment.append(" nextValue){return ");
        CompilationResult<JavaCodeFragment> functionCall = generateFunctionCall(arg1Result, arg2Result);
        if (functionCall.failed()) {
            String messageText = Messages.INSTANCE.getString(MSG_CODE_INVALID_DATATYPE, getName(), datatype.getName());
            return new CompilationResultImpl(Message.newError(MSG_CODE_INVALID_DATATYPE, messageText));
        } else {
            fragment.append(functionCall.getCodeFragment());
        }
        fragment.append(";}\n@Override public ");
        fragment.appendClassName(datatypeClassName);
        fragment.append(" getFallBackValue(){");
        fragment.append(generateReturnFallBackValueCall(datatype));
        fragment.append(";}}.getResult(");
        fragment.append(listArgument.getCodeFragment());
        fragment.append(")");

        return createCompilationResult(listArgument, fragment);
    }

    protected abstract JavaCodeFragment generateReturnFallBackValueCall(Datatype datatype);

    protected CompilationResult<JavaCodeFragment> generateFunctionCall(CompilationResultImpl argument1,
            CompilationResultImpl argument2) {
        CompilationResultImpl[] arguments = new CompilationResultImpl[] { argument1, argument2 };
        Datatype[] datatypes = new Datatype[] { argument1.getDatatype(), argument2.getDatatype() };
        CompilationResult<JavaCodeFragment> matchingFunctionUsingConversion = getCompiler()
                .getMatchingFunctionUsingConversion(arguments, datatypes, getName());
        return matchingFunctionUsingConversion;
    }

    protected CompilationResultImpl createCompilationResult(CompilationResult<JavaCodeFragment> listArgument,
            JavaCodeFragment fragment) {
        Datatype basicDatatype = getBasicType(listArgument);
        CompilationResultImpl result = new CompilationResultImpl(fragment, basicDatatype);
        result.addMessages(listArgument.getMessages());
        return result;
    }

    protected Datatype getBasicType(CompilationResult<JavaCodeFragment> listArgument) {
        ListOfTypeDatatype listDatatype = (ListOfTypeDatatype)listArgument.getDatatype();
        Datatype basicDatatype = listDatatype.getBasicDatatype();
        return basicDatatype;
    }

}
