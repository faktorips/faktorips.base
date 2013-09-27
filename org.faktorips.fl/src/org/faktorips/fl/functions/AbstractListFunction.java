/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

    public AbstractMinMaxList(String name, String description, FunctionSignatures functionSignature) {
        super(name, description, functionSignature);
        functionName = "sum";
    }

    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 1);
        AbstractCompilationResult<JavaCodeFragment> listArgument = (AbstractCompilationResult<JavaCodeFragment>)argResults[0];

        datatype = getBasicType(listArgument);

        JavaCodeFragment fragment = createCodeFragment(listArgument);
        CompilationResultImpl result = createCompilationResult(listArgument, fragment);

        return result;
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

    protected CompilationResultImpl createCompilationResult(CompilationResult<JavaCodeFragment> listArgument,
            JavaCodeFragment fragment) {
        Datatype basicDatatype = getBasicType(listArgument);
        CompilationResultImpl result = new CompilationResultImpl(fragment, basicDatatype);
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
