/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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
import org.faktorips.util.ArgumentCheck;

/**
 *
 */
public class MinMaxMoney extends AbstractFlFunction {

    private String functionName = null;

    public MinMaxMoney(String name, String description, boolean isMax) {
        super(name, description, isMax ? FunctionSignatures.MaxMoney : FunctionSignatures.MinMoney);
        functionName = isMax ? "max" : "min";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        // value1.max(value2)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append(argResults[0].getCodeFragment());
        fragment.append('.');
        fragment.append(functionName);
        fragment.append('(');
        fragment.append(argResults[1].getCodeFragment());
        fragment.append(')');

        CompilationResultImpl result = new CompilationResultImpl(fragment, Datatype.MONEY);
        result.addMessages(argResults[0].getMessages());
        result.addMessages(argResults[1].getMessages());
        return result;
    }

}
