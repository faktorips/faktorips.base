/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.ILocalDateHelper;
import org.faktorips.datatype.joda.LocalDateDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalDateDatatype}.
 */
public class LocalDateHelper extends BaseJodaDatatypeHelper implements ILocalDateHelper {

    public static final String ORG_JODA_TIME_LOCAL_DATE = "org.joda.time.LocalDate"; //$NON-NLS-1$
    private static final String YEAR = "year()"; //$NON-NLS-1$
    private static final String MONTH_OF_YEAR = "monthOfYear()"; //$NON-NLS-1$
    private static final String DAY_OF_MONTH = "dayOfMonth()"; //$NON-NLS-1$
    private static final String ORG_JODA_TIME_DATE_TIME_FIELD_TYPE = "org.joda.time.DateTimeFieldType"; //$NON-NLS-1$
    private static final String DAYS_HELPER_CLASS = "org.joda.time.Days"; //$NON-NLS-1$
    private static final String WEEKS_HELPER_CLASS = "org.joda.time.Weeks"; //$NON-NLS-1$
    private static final String MONTHS_HELPER_CLASS = "org.joda.time.Months"; //$NON-NLS-1$
    private static final String YEARS_HELPER_CLASS = "org.joda.time.Years"; //$NON-NLS-1$

    private static final String PARSE_METHOD = "toLocalDate"; //$NON-NLS-1$

    public LocalDateHelper() {
        super(ORG_JODA_TIME_LOCAL_DATE, PARSE_METHOD);
    }

    public LocalDateHelper(LocalDateDatatype d) {
        super(d, ORG_JODA_TIME_LOCAL_DATE, PARSE_METHOD);
    }

    @Override
    public JavaCodeFragment getPeriodCode(JavaCodeFragment arg1, JavaCodeFragment arg2, Period period) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        switch (period) {
            case DAYS:
                fragment.appendClassName(DAYS_HELPER_CLASS).append(".daysBetween"); //$NON-NLS-1$
                break;
            case WEEKS:
                fragment.appendClassName(WEEKS_HELPER_CLASS).append(".weeksBetween"); //$NON-NLS-1$
                break;
            case MONTHS:
                fragment.appendClassName(MONTHS_HELPER_CLASS).append(".monthsBetween"); //$NON-NLS-1$
                break;
            case YEARS:
                fragment.appendClassName(YEARS_HELPER_CLASS).append(".yearsBetween"); //$NON-NLS-1$
                break;

            default:
                break;
        }
        fragment.append("(").append(arg1).append(", ").append(arg2).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        switch (period) {
            case DAYS:
                fragment.append(".getDays()"); //$NON-NLS-1$
                break;
            case WEEKS:
                fragment.append(".getWeeks()"); //$NON-NLS-1$
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
        fragment.append("new ").appendClassName(getJavaClassName()).append("(")//$NON-NLS-1$ //$NON-NLS-2$
                .append(year).append(", ").append(month).append(", ").append(day).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return fragment;
    }

    @Override
    public String getDateFieldEnumClass() {
        return ORG_JODA_TIME_DATE_TIME_FIELD_TYPE;
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
