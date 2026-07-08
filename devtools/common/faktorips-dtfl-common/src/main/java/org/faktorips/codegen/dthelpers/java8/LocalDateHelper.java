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

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractTimeHelper;
import org.faktorips.codegen.dthelpers.ILocalDateHelper;
import org.faktorips.datatype.joda.LocalDateDatatype;

public class LocalDateHelper extends AbstractTimeHelper implements ILocalDateHelper {

    private static final String CHRONO_FIELD_YEAR = ChronoField.YEAR.name();
    private static final String CHRONO_FIELD_MONTH_OF_YEAR = ChronoField.MONTH_OF_YEAR.name();
    private static final String CHRONO_FIELD_DAY_OF_MONTH = ChronoField.DAY_OF_MONTH.name();
    private static final String JAVA_TIME_TEMPORAL_CHRONO_FIELD = ChronoField.class.getName();
    private static final String JAVA_TIME_LOCAL_DATE = LocalDate.class.getName();
    private static final String CHRONO_UNIT_YEARS = ChronoUnit.YEARS.name();
    private static final String CHRONO_UNIT_MONTHS = ChronoUnit.MONTHS.name();
    private static final String CHRONO_UNIT_WEEKS = ChronoUnit.WEEKS.name();
    private static final String CHRONO_UNIT_DAYS = ChronoUnit.DAYS.name();

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
        return new JavaCodeFragment()
                .append("(int) ") //$NON-NLS-1$
                .appendClassName(ChronoUnit.class)
                .append('.')
                .append(switch (period) {
                    case DAYS -> CHRONO_UNIT_DAYS;
                    case WEEKS -> CHRONO_UNIT_WEEKS;
                    case MONTHS -> CHRONO_UNIT_MONTHS;
                    case YEARS -> CHRONO_UNIT_YEARS;
                }).append(".between(").append(arg1).append(", ").append(arg2) //$NON-NLS-1$ //$NON-NLS-2$
                .append(")"); //$NON-NLS-1$
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
        return CHRONO_FIELD_DAY_OF_MONTH;
    }

    @Override
    public String getMonthOfYearField() {
        return CHRONO_FIELD_MONTH_OF_YEAR;
    }

    @Override
    public String getYearField() {
        return CHRONO_FIELD_YEAR;
    }

}
