/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl;

import java.math.BigDecimal;
import java.util.Locale;

import org.faktorips.fl.functions.Abs;
import org.faktorips.fl.functions.And;
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
import org.faktorips.fl.functions.Round;
import org.faktorips.fl.functions.WholeNumber;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A <code>FunctionResolver</code> that supports Excel functions. The functions are available in
 * different languages.
 */
public class ExcelFunctionsResolver extends DefaultFunctionResolver {

    private LocalizedStringsSet localizedStrings;

    // the locale used for function names and descriptions.
    private Locale locale;

    /**
     * Creates a new resolver that contains a set of functions that are similiar by name and
     * argument list as those provided by Microsoft's Excel.
     * 
     * @param locale The locale that determines the language of the function names.
     */
    public ExcelFunctionsResolver(Locale locale) {
        super();
        this.locale = locale;
        localizedStrings = new LocalizedStringsSet("org.faktorips.fl.ExcelFunctions", getClass().getClassLoader()); //$NON-NLS-1$
        add(new Abs(getFctName("abs"), getFctDescription("abs"))); //$NON-NLS-1$ //$NON-NLS-2$
        add(new If(getFctName("if"), getFctDescription("if"))); //$NON-NLS-1$ //$NON-NLS-2$
        add(new Or(getFctName("or"), getFctDescription("or"))); //$NON-NLS-1$ //$NON-NLS-2$
        add(new And(getFctName("and"), getFctDescription("and"))); //$NON-NLS-1$ //$NON-NLS-2$
        add(new Round(getFctName("round"), getFctDescription("round"), BigDecimal.ROUND_HALF_UP)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new Round(getFctName("roundup"), getFctDescription("roundup"), BigDecimal.ROUND_UP)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new Round(getFctName("rounddown"), getFctDescription("rounddown"), BigDecimal.ROUND_UP)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new WholeNumber(getFctName("wholenumber"), getFctDescription("wholenumber"))); //$NON-NLS-1$ //$NON-NLS-2$
        add(new IsEmpty(getFctName("isempty"), getFctDescription("isempty"))); //$NON-NLS-1$ //$NON-NLS-2$
        add(new Not(getFctName("not"), getFctDescription("not"))); //$NON-NLS-1$ //$NON-NLS-2$
        add(new NotBoolean(getFctName("not"), getFctDescription("not"))); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxMoney(getFctName("max"), getFctDescription("max"), true)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxMoney(getFctName("min"), getFctDescription("min"), false)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxDecimal(getFctName("max"), getFctDescription("max"), true)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxDecimal(getFctName("min"), getFctDescription("min"), false)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxInt(getFctName("max"), getFctDescription("max"), true)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxInt(getFctName("min"), getFctDescription("min"), false)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxLong(getFctName("max"), getFctDescription("max"), true)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxLong(getFctName("min"), getFctDescription("min"), false)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxDouble(getFctName("max"), getFctDescription("max"), true)); //$NON-NLS-1$ //$NON-NLS-2$
        add(new MinMaxDouble(getFctName("min"), getFctDescription("min"), false)); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String getFctName(String key) {
        return localizedStrings.getString(key + ".name", locale); //$NON-NLS-1$
    }

    private String getFctDescription(String key) {
        return localizedStrings.getString(key + ".description", locale); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        return "ExcelFunctionResolver"; //$NON-NLS-1$
    }

    class NameDescription {

        NameDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }

        String name;
        String description;
    }

}
