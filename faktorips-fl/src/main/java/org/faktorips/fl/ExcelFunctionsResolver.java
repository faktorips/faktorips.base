/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.math.RoundingMode;
import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.fl.functions.Abs;
import org.faktorips.fl.functions.And;
import org.faktorips.fl.functions.Count;
import org.faktorips.fl.functions.If;
import org.faktorips.fl.functions.IfBoolean;
import org.faktorips.fl.functions.IsEmpty;
import org.faktorips.fl.functions.MinMaxComparableDatatypes;
import org.faktorips.fl.functions.MinMaxDecimal;
import org.faktorips.fl.functions.MinMaxDouble;
import org.faktorips.fl.functions.MinMaxInt;
import org.faktorips.fl.functions.MinMaxLong;
import org.faktorips.fl.functions.MinMaxMoney;
import org.faktorips.fl.functions.Not;
import org.faktorips.fl.functions.NotBoolean;
import org.faktorips.fl.functions.Or;
import org.faktorips.fl.functions.PowerDecimal;
import org.faktorips.fl.functions.PowerInt;
import org.faktorips.fl.functions.Round;
import org.faktorips.fl.functions.SqrtDecimal;
import org.faktorips.fl.functions.TextFunction;
import org.faktorips.fl.functions.WholeNumber;
import org.faktorips.fl.functions.date.Date;
import org.faktorips.fl.functions.date.Days;
import org.faktorips.fl.functions.date.Days360;
import org.faktorips.fl.functions.date.Months;
import org.faktorips.fl.functions.date.Weeks;
import org.faktorips.fl.functions.date.Years;

/**
 * A {@link FunctionResolver} that supports Excel functions. The functions are available in
 * different languages.
 */
public class ExcelFunctionsResolver extends LocalizedFunctionsResolver<JavaCodeFragment> {

    /**
     * Creates a new resolver that contains a set of functions that are similar by name and argument
     * list to those provided by Microsoft's Excel.
     * 
     * @param locale The locale that determines the language of the function names.
     */
    public ExcelFunctionsResolver(Locale locale) {
        super(locale);
        add(new Abs(getFctName(ExcelFunction.ABS), getFctDescription(ExcelFunction.ABS)));
        add(new IfBoolean(getFctName(ExcelFunction.IF), getFctDescription(ExcelFunction.IF)));
        add(new If(getFctName(ExcelFunction.IF), getFctDescription(ExcelFunction.IF)));
        add(new Or(getFctName(ExcelFunction.OR), getFctDescription(ExcelFunction.OR)));
        add(new And(getFctName(ExcelFunction.AND), getFctDescription(ExcelFunction.AND)));
        add(new Round(getFctName(ExcelFunction.ROUND), getFctDescription(ExcelFunction.ROUND), RoundingMode.HALF_UP));
        add(new Round(getFctName(ExcelFunction.ROUNDUP), getFctDescription(ExcelFunction.ROUNDUP), RoundingMode.UP));
        add(new Round(getFctName(ExcelFunction.ROUNDDOWN), getFctDescription(ExcelFunction.ROUNDDOWN),
                RoundingMode.DOWN));
        add(new WholeNumber(getFctName(ExcelFunction.WHOLENUMBER), getFctDescription(ExcelFunction.WHOLENUMBER)));
        add(new IsEmpty(getFctName(ExcelFunction.ISEMPTY), getFctDescription(ExcelFunction.ISEMPTY)));
        add(new Not(getFctName(ExcelFunction.NOT), getFctDescription(ExcelFunction.NOT)));
        add(new NotBoolean(getFctName(ExcelFunction.NOT), getFctDescription(ExcelFunction.NOT)));
        add(new MinMaxMoney(getFctName(ExcelFunction.MAX), getFctDescription(ExcelFunction.MAX), true));
        add(new MinMaxMoney(getFctName(ExcelFunction.MIN), getFctDescription(ExcelFunction.MIN), false));
        add(new MinMaxDecimal(getFctName(ExcelFunction.MAX), getFctDescription(ExcelFunction.MAX), true));
        add(new MinMaxDecimal(getFctName(ExcelFunction.MIN), getFctDescription(ExcelFunction.MIN), false));
        add(new MinMaxInt(getFctName(ExcelFunction.MAX), getFctDescription(ExcelFunction.MAX), true));
        add(new MinMaxInt(getFctName(ExcelFunction.MIN), getFctDescription(ExcelFunction.MIN), false));
        add(new MinMaxLong(getFctName(ExcelFunction.MAX), getFctDescription(ExcelFunction.MAX), true));
        add(new MinMaxLong(getFctName(ExcelFunction.MIN), getFctDescription(ExcelFunction.MIN), false));
        add(new MinMaxDouble(getFctName(ExcelFunction.MAX), getFctDescription(ExcelFunction.MAX), true));
        add(new MinMaxDouble(getFctName(ExcelFunction.MIN), getFctDescription(ExcelFunction.MIN), false));

        add(new MinMaxComparableDatatypes(getFctName(ExcelFunction.MIN), getFctDescription(ExcelFunction.MIN), false,
                LocalDateDatatype.DATATYPE));
        add(new MinMaxComparableDatatypes(getFctName(ExcelFunction.MAX), getFctDescription(ExcelFunction.MAX), true,
                LocalDateDatatype.DATATYPE));

        add(new PowerDecimal(getFctName(ExcelFunction.POWER), getFctDescription(ExcelFunction.POWER)));
        add(new PowerInt(getFctName(ExcelFunction.POWER), getFctDescription(ExcelFunction.POWER)));
        add(new SqrtDecimal(getFctName(ExcelFunction.SQRT), getFctDescription(ExcelFunction.SQRT)));
        add(new Count(getFctName(ExcelFunction.COUNT), getFctDescription(ExcelFunction.COUNT)));
        add(new Days(getFctName(ExcelFunction.DAYS), getFctDescription(ExcelFunction.DAYS)));
        add(new Weeks(getFctName(ExcelFunction.WEEKS), getFctDescription(ExcelFunction.WEEKS)));
        add(new Months(getFctName(ExcelFunction.MONTHS), getFctDescription(ExcelFunction.MONTHS)));
        add(new Years(getFctName(ExcelFunction.YEARS), getFctDescription(ExcelFunction.YEARS)));
        add(new Date(getFctName(ExcelFunction.DATE), getFctDescription(ExcelFunction.DATE)));
        add(new Days360(getFctName(ExcelFunction.DAYS360), getFctDescription(ExcelFunction.DAYS360)));
        add(new TextFunction(getFctName(ExcelFunction.TEXT), getFctDescription(ExcelFunction.TEXT)));
    }

    @Override
    public String toString() {
        return "ExcelFunctionResolver"; //$NON-NLS-1$
    }

    @Override
    protected String getLocalizationFileBaseName() {
        return "org.faktorips.fl.ExcelFunctions"; //$NON-NLS-1$
    }

    private String getFctName(ExcelFunction function) {
        return getFctName(function.getPropertyKey());
    }

    private String getFctDescription(ExcelFunction function) {
        return getFctDescription(function.getPropertyKey());
    }

}
