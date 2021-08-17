/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.java8;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractTimeHelper;
import org.faktorips.codegen.dthelpers.ILocalDateHelper;
import org.faktorips.datatype.joda.LocalDateDatatype;

public class LocalDateHelper extends AbstractTimeHelper implements ILocalDateHelper {

    private static final String WEEKS = "WEEKS"; //$NON-NLS-1$
    private static final String YEAR = "YEAR"; //$NON-NLS-1$
    private static final String MONTH_OF_YEAR = "MONTH_OF_YEAR"; //$NON-NLS-1$
    private static final String DAY_OF_MONTH = "DAY_OF_MONTH"; //$NON-NLS-1$
    private static final String JAVA_TIME_TEMPORAL_CHRONO_FIELD = "java.time.temporal.ChronoField"; //$NON-NLS-1$
    private static final String JAVA_TIME_TEMPORAL_CHRONO_UNIT = "java.time.temporal.ChronoUnit"; //$NON-NLS-1$
    private static final String JAVA_TIME_LOCAL_DATE = "java.time.LocalDate"; //$NON-NLS-1$
    private static final String PERIOD_CLASS = "java.time.Period"; //$NON-NLS-1$

    public LocalDateHelper(LocalDateDatatype datatype) {
        super(datatype);
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return ParseHelper.parse(expression, JAVA_TIME_LOCAL_DATE);
    }

    @Override
    public String getJavaClassName() {
        return JAVA_TIME_LOCAL_DATE;
    }

    @Override
    public JavaCodeFragment getPeriodCode(JavaCodeFragment arg1, JavaCodeFragment arg2, Period period) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (Period.WEEKS == period) {
            fragment.append("(int) "); //$NON-NLS-1$
        }
        fragment.appendClassName(PERIOD_CLASS).append(".between(").append(arg1).append(", ").append(arg2) //$NON-NLS-1$ //$NON-NLS-2$
                .append(")"); //$NON-NLS-1$
        switch (period) {
            case DAYS:
                fragment.append(".getDays()"); //$NON-NLS-1$
                break;
            case WEEKS:
                fragment.append(".get(").appendClassName(JAVA_TIME_TEMPORAL_CHRONO_UNIT).append('.').append(WEEKS) //$NON-NLS-1$
                        .append(')');
                break;
            case MONTHS:
                fragment.append(".getMonths()"); //$NON-NLS-1$
                break;
            case YEARS:
                fragment.append(".getYears()"); //$NON-NLS-1$
                break;

            default:
                break;
        }
        return fragment;
    }

    @Override
    public JavaCodeFragment getDateInitialization(JavaCodeFragment year, JavaCodeFragment month, JavaCodeFragment day) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(getJavaClassName()).append(".of(")//$NON-NLS-1$
                .append(year).append(", ").append(month).append(", ").append(day).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return fragment;
    }

    @Override
    public String getDateFieldEnumClass() {
        return JAVA_TIME_TEMPORAL_CHRONO_FIELD;
    }

    @Override
    public String getDayOfMonthField() {
        return DAY_OF_MONTH;
    }

    @Override
    public String getMonthOfYearField() {
        return MONTH_OF_YEAR;
    }

    @Override
    public String getYearField() {
        return YEAR;
    }

}
