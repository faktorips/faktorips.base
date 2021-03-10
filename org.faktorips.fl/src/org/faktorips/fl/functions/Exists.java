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

import org.faktorips.codegen.BaseDatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;

/**
 * 
 * @author Jan Ortmann
 */
public class Exists extends AbstractFlFunction {

    public static final String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "EXISTS"; //$NON-NLS-1$

    public Exists(String name, String description) {
        super(name, description, FunctionSignatures.Exists);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        Datatype argType = argResults[0].getDatatype();
        if (argType.isPrimitive()) {
            // values of primitive types can never be null
            return new CompilationResultImpl("true", Datatype.PRIMITIVE_BOOLEAN);
        }
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("new ");
        code.appendClassName("org.faktorips.runtime.formula.FormulaEvaluatorUtil.ExistsHelper");
        code.append("(){@Override\nprotected boolean existsInternal(){ return ");
        if (argType instanceof ListOfTypeDatatype) {
            code.append("!");
            code.append(argResults[0].getCodeFragment());
            code.append(".isEmpty()");
        } else if (argType.hasNullObject()) {
            code.append("!");
            BaseDatatypeHelper<JavaCodeFragment> helper = getCompiler().getDatatypeHelper(argType);
            code.append(helper.nullExpression());
            code.append(".equals(");
            code.append(argResults[0].getCodeFragment());
            code.append(")");
        } else {
            code.append(argResults[0].getCodeFragment());
            code.append("!=null");
        }
        code.append(";}}.exists()");
        return new CompilationResultImpl(code, Datatype.PRIMITIVE_BOOLEAN);
    }

}
