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

/**
 * Calculates the difference between two date values based on the ISMA 30/360 calendar.
 * 
 * 
 * 
 * @author dirmeier
 */
public class Days360 extends AbstractFlFunction {

    private static final String GET_YEAR = ".getYear()";

    private static final String GET_MONTH = ".getMonthOfYear()";

    private static final String GET_DAYS = ".getDayOfMonth()";

    public Days360(String name, String description) {
        super(name, description, FunctionSignatures.DAYS360);
    }

    /**
     * ((d2.getYear() - d1.getYear()) * 360 + (d2.getMonthOfYear() - d1.getMonthOfYear()) * 30 +
     * (Math.min(d2.getDayOfMonth(), 30) - Math.min(d1.getDayOfMonth(), 30)))
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        JavaCodeFragment d1 = argResults[0].getCodeFragment();
        JavaCodeFragment d2 = argResults[1].getCodeFragment();
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("((").append(d2).append(GET_YEAR).append(" - ").append(d1).append(GET_YEAR)
                .append(") * 360 + ");
        fragment.append("(").append(d2).append(GET_MONTH).append(" - ").append(d1).append(GET_MONTH)
                .append(") * 30 + ");
        fragment.append("(Math.min(").append(d2).append(GET_DAYS).append(", 30)").append(" - ").append("Math.min(")
                .append(d1).append(GET_DAYS).append(", 30)))");
        return new CompilationResultImpl(fragment, getType());
    }
}
