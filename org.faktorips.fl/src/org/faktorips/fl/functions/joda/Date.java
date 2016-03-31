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
package org.faktorips.fl.functions.joda;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.fl.functions.AbstractFlFunction;
import org.faktorips.util.ArgumentCheck;

public class Date extends AbstractFlFunction {

    public Date(String name, String description) {
        super(name, description, FunctionSignatures.DATE);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 3);
        JavaCodeFragment year = argResults[0].getCodeFragment();
        JavaCodeFragment month = argResults[1].getCodeFragment();
        JavaCodeFragment day = argResults[2].getCodeFragment();
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ").appendClassName(getJavaClassName(getType())).append("(");
        fragment.append(year).append(", ").append(month).append(", ").append(day).append(")");
        return new CompilationResultImpl(fragment, getType());
    }

}
