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

import java.math.BigDecimal;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.fl.functions.AbstractFlFunction;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.Decimal;

public class NrOfMonths extends AbstractFlFunction {

    private static final String MONTHS_HELPER_CLASS = "org.joda.time.Months";

    private static final String PERIOD_CLASS = "org.joda.time.Period";

    private static final String PERIOD_TYPE_CLASS = "org.joda.time.PeriodType";

    public NrOfMonths(String name, String description) {
        super(name, description, FunctionSignatures.NrOfMonths);
    }

    /**
     * Decimal.valueOf(Months.monthsBetween(d1, d2).getMonths() + new Period(d1, d2,
     * PeriodType.yearMonthDay().withYearsRemoved()).getDays()).divide(31, 15,
     * BigDecimal.ROUND_HALF_UP)
     */
    public CompilationResult<JavaCodeFragment> compile(CompilationResult<JavaCodeFragment>[] argResults) {
        ArgumentCheck.length(argResults, 2);
        JavaCodeFragment d1 = argResults[0].getCodeFragment();
        JavaCodeFragment d2 = argResults[1].getCodeFragment();
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(Decimal.class).append(".valueOf(");
        fragment.appendClassName(MONTHS_HELPER_CLASS);
        fragment.append(".monthsBetween(").append(d1).append(", ").append(d2).append(")").append(".getMonths()");
        fragment.append('+');
        fragment.append("new ").appendClassName(PERIOD_CLASS).append("(").append(d1).append(", ").append(d2)
                .append(", ").append(PERIOD_TYPE_CLASS).append(".yearMonthDay().withYearsRemoved()).getDays())");
        fragment.append(".divide(31, 10, ").appendClassName(BigDecimal.class).append(".ROUND_HALF_UP)");
        return new CompilationResultImpl(fragment, getType());
    }
}
