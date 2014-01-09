/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;

public class SumList extends AbstractListFunction {

    public SumList(String name, String description) {
        super(name, description, FunctionSignatures.SumList);
    }

    @Override
    protected JavaCodeFragment generateReturnFallBackValueCall() {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("return ");
        fragment.append(getCompiler().getDatatypeHelper(getDatatype()).newInstance("0"));
        return fragment;
    }

    @Override
    protected JavaCodeFragment generateFunctionCall(CompilationResultImpl argument1, CompilationResultImpl argument2) {
        return getCompiler().getBinaryOperation("+", argument1, argument2).getCodeFragment();
    }
}
