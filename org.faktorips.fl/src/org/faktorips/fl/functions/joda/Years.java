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

public class Years extends AbstractFlFunction {

    private static final String YEARS_HELPER_CLASS = "org.joda.time.Years";

    public Years(String name, String description) {
        super(name, description, FunctionSignatures.YEARS);
    }

    @Override
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(YEARS_HELPER_CLASS);
        fragment.append(".yearsBetween(").append(argResults[0].getCodeFragment()).append(", ")
        .append(argResults[1].getCodeFragment()).append(")").append(".getYears()");
        return new CompilationResultImpl(fragment, getType());
    }

}
