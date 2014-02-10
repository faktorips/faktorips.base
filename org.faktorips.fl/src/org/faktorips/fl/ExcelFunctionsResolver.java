/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import java.math.BigDecimal;
import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.functions.Abs;
import org.faktorips.fl.functions.And;
import org.faktorips.fl.functions.Count;
import org.faktorips.fl.functions.If;
import org.faktorips.fl.functions.IsEmpty;
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
import org.faktorips.fl.functions.WholeNumber;

/**
 * A {@link FunctionResolver} that supports Excel functions. The functions are available in
 * different languages.
 */
public class ExcelFunctionsResolver extends LocalizedFunctionsResolver<JavaCodeFragment> {

    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String ISEMPTY = "isempty";
    public static final String WHOLENUMBER = "wholenumber";
    public static final String ROUNDDOWN = "rounddown";
    public static final String ROUNDUP = "roundup";
    public static final String ROUND = "round";
    public static final String IF = "if";
    public static final String ABS = "abs";
    public static final String NOT = "not";
    public static final String OR = "or";
    public static final String AND = "and";
    public static final String POWER = "power";
    public static final String SQRT = "sqrt";
    public static final String COUNT = "count";

    /**
     * Creates a new resolver that contains a set of functions that are similar by name and argument
     * list to those provided by Microsoft's Excel.
     * 
     * @param locale The locale that determines the language of the function names.
     */
    public ExcelFunctionsResolver(Locale locale) {
        super(locale);
        add(new Abs(getFctName(ABS), getFctDescription(ABS)));
        add(new If(getFctName(IF), getFctDescription(IF)));
        add(new Or(getFctName(OR), getFctDescription(OR)));
        add(new And(getFctName(AND), getFctDescription(AND)));
        add(new Round(getFctName(ROUND), getFctDescription(ROUND), BigDecimal.ROUND_HALF_UP));
        add(new Round(getFctName(ROUNDUP), getFctDescription(ROUNDUP), BigDecimal.ROUND_UP));
        add(new Round(getFctName(ROUNDDOWN), getFctDescription(ROUNDDOWN), BigDecimal.ROUND_DOWN));
        add(new WholeNumber(getFctName(WHOLENUMBER), getFctDescription(WHOLENUMBER)));
        add(new IsEmpty(getFctName(ISEMPTY), getFctDescription(ISEMPTY)));
        add(new Not(getFctName(NOT), getFctDescription(NOT)));
        add(new NotBoolean(getFctName(NOT), getFctDescription(NOT)));
        add(new MinMaxMoney(getFctName(MAX), getFctDescription(MAX), true));
        add(new MinMaxMoney(getFctName(MIN), getFctDescription(MIN), false));
        add(new MinMaxDecimal(getFctName(MAX), getFctDescription(MAX), true));
        add(new MinMaxDecimal(getFctName(MIN), getFctDescription(MIN), false));
        add(new MinMaxInt(getFctName(MAX), getFctDescription(MAX), true));
        add(new MinMaxInt(getFctName(MIN), getFctDescription(MIN), false));
        add(new MinMaxLong(getFctName(MAX), getFctDescription(MAX), true));
        add(new MinMaxLong(getFctName(MIN), getFctDescription(MIN), false));
        add(new MinMaxDouble(getFctName(MAX), getFctDescription(MAX), true));
        add(new MinMaxDouble(getFctName(MIN), getFctDescription(MIN), false));
        add(new PowerDecimal(getFctName(POWER), getFctDescription(POWER)));
        add(new PowerInt(getFctName(POWER), getFctDescription(POWER)));
        add(new SqrtDecimal(getFctName(SQRT), getFctDescription(SQRT)));
        add(new Count(getFctName(COUNT), getFctDescription(COUNT)));
    }

    @Override
    public String toString() {
        return "ExcelFunctionResolver"; //$NON-NLS-1$
    }

    @Override
    protected String getLocalizationFileBaseName() {
        return "org.faktorips.fl.ExcelFunctions"; //$NON-NLS-1$
    }

}
