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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.runtime.Message;

public class MinMaxList extends AbstractListFunction {

    private static final String MSG_CODE_INVALID_DATATYPE = ExprCompiler.PREFIX + "MIN_MAX_LIST_INVALID_DATATYPE"; //$NON-NLS-1$

    public MinMaxList(String name, String description, boolean isMax) {
        super(name, description, isMax ? FunctionSignatures.MaxList : FunctionSignatures.MinList);
    }

    @Override
    protected CompilationResult<JavaCodeFragment> validateBasicDatatype(Datatype basicDatatype) {
        if (!supportsCompare(basicDatatype)) {
            return createInvalidDatatypeResult(basicDatatype);
        }
        return super.validateBasicDatatype(basicDatatype);
    }

    private CompilationResultImpl createInvalidDatatypeResult(Datatype basicDatatype) {
        String messageText = Messages.INSTANCE.getString(MSG_CODE_INVALID_DATATYPE, getName(), basicDatatype.getName());
        return new CompilationResultImpl(Message.newError(MSG_CODE_INVALID_DATATYPE, messageText));
    }

    private boolean supportsCompare(Datatype basicDatatype) {
        return basicDatatype.isValueDatatype() && ((ValueDatatype)basicDatatype).supportsCompare();
    }

    @Override
    protected JavaCodeFragment generateReturnFallBackValueCall(Datatype datatype) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("throw new ");
        fragment.appendClassName(IllegalArgumentException.class);
        fragment.append("(\"List argument is empty or null\")");
        return fragment;
    }
}
