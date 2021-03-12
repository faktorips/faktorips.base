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
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.runtime.formula.FormulaEvaluatorUtil;
import org.faktorips.util.ArgumentCheck;

/**
 * This class implements the if statement for the condition of type {@link Datatype#BOOLEAN}.
 */
public class IfBoolean extends AbstractIf {

    public IfBoolean(String name, String description) {
        super(name, description, FunctionSignatures.IfBoolean);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 3);
        JavaCodeFragment arg0 = new JavaCodeFragment().appendClassName(FormulaEvaluatorUtil.class)
                .append(".toPrimitiveBoolean(").append(argResults[0].getCodeFragment()).append(")");
        argResults[0] = new CompilationResultImpl(arg0, Datatype.PRIMITIVE_BOOLEAN);
        return super.compile(argResults);
    }

}
