/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.util.ArgumentCheck;

public abstract class AbstractListFunction extends AbstractFlFunction {

    private Datatype datatype;

    public AbstractListFunction(String name, String description, FunctionSignatures signature) {
        super(name, description, signature);
    }

    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        AbstractCompilationResult<JavaCodeFragment> listArgument = (AbstractCompilationResult<JavaCodeFragment>)argResults[0];

        datatype = getBasicType(listArgument);

        CompilationResult<JavaCodeFragment> datatypeResult = validateBasicDatatype(datatype);
        if (compilationFailed(datatypeResult)) {
            return datatypeResult;
        } else {
            return createCompilationResult(listArgument);
        }
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

    protected JavaCodeFragment createCodeFragment(AbstractCompilationResult<JavaCodeFragment> listArgument) {
        JavaCodeFragment fragment = new JavaCodeFragment();

        CompilationResultImpl arg1Result = new CompilationResultImpl("currentResult", getBasicType(listArgument));
        CompilationResultImpl arg2Result = new CompilationResultImpl("nextValue", getBasicType(listArgument));

        String datatypeClassName = getBasicType(listArgument).getJavaClassName();

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
        fragment.append(generateFunctionCall(arg1Result, arg2Result));
        fragment.append(";}\n@Override public ");
        fragment.appendClassName(datatypeClassName);
        fragment.append(" getFallBackValue(){");
        fragment.append(generateReturnFallBackValueCall());
        fragment.append(";}}.getResult(");
        fragment.append(listArgument.getCodeFragment());
        fragment.append(")");

        return fragment;
    }

    protected abstract JavaCodeFragment generateReturnFallBackValueCall();

    protected JavaCodeFragment generateFunctionCall(CompilationResultImpl argument1, CompilationResultImpl argument2) {
        CompilationResultImpl[] arguments = new CompilationResultImpl[] { argument1, argument2 };
        Datatype[] datatypes = new Datatype[] { argument1.getDatatype(), argument2.getDatatype() };
        return getCompiler().getMatchingFunctionUsingConversion(arguments, datatypes, getName()).getCodeFragment();
    }

    protected CompilationResultImpl createCompilationResult(AbstractCompilationResult<JavaCodeFragment> listArgument) {
        JavaCodeFragment fragment = createCodeFragment(listArgument);
        CompilationResultImpl result = new CompilationResultImpl(fragment, getDatatype());
        result.addMessages(listArgument.getMessages());
        return result;
    }

    private Datatype getBasicType(CompilationResult<JavaCodeFragment> listArgument) {
        ListOfTypeDatatype listDatatype = (ListOfTypeDatatype)listArgument.getDatatype();
        Datatype basicDatatype = listDatatype.getBasicDatatype();
        return basicDatatype;
    }

    public Datatype getDatatype() {
        return datatype;
    }

}
