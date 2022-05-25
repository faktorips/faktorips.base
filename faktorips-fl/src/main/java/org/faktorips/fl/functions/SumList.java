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
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.runtime.Message;

public class SumList extends AbstractListFunction {

    static final String MSG_CODE_SUM_INVALID_DATATYPE = ExprCompiler.PREFIX + "SUM-INVALID-DATATYPE";

    public SumList(String name, String description) {
        super(name, description, FunctionSignatures.SumList);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> validateBasicDatatype(Datatype basicDatatype) {
        if (!isNumeric(basicDatatype)) {
            return createInvalidDatatypeResult(basicDatatype);
        }
        return super.validateBasicDatatype(basicDatatype);
    }

    private boolean isNumeric(Datatype basicDatatype) {
        return basicDatatype instanceof NumericDatatype;
    }

    private CompilationResult<JavaCodeFragment> createInvalidDatatypeResult(Datatype basicDatatype) {
        String text = Messages.INSTANCE.getString(MSG_CODE_SUM_INVALID_DATATYPE, getName(), basicDatatype.getName());
        return new CompilationResultImpl(Message.newError(MSG_CODE_SUM_INVALID_DATATYPE, text));
    }

    @Override
    protected JavaCodeFragment generateReturnFallBackValueCall(Datatype datatype) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("return ");
        fragment.append(getDatatypeHelper(datatype).newInstance("0"));
        return fragment;
    }

    @Override
    protected CompilationResult<JavaCodeFragment> generateFunctionCall(CompilationResultImpl argument1,
            CompilationResultImpl argument2) {
        return new CompilationResultImpl(getCompiler().getBinaryOperation("+", argument1, argument2).getCodeFragment(),
                argument1.getDatatype());
    }
}
