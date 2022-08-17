/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import org.faktorips.codegen.JavaCodeFragment;

/**
 * A {@link AbstractTimeHelper} that supports a LocalDate datatype.
 */
public interface ILocalDateHelper {

    enum Period {
        DAYS,
        WEEKS,
        MONTHS,
        YEARS
    }

    /**
     * Returns the code to calculate the period between the two arguments.
     */
    JavaCodeFragment getPeriodCode(JavaCodeFragment arg1, JavaCodeFragment arg2, Period period);

    /**
     * Returns the code to create a new LocalDate.
     */
    JavaCodeFragment getDateInitialization(JavaCodeFragment year, JavaCodeFragment month, JavaCodeFragment day);

    /**
     * Returns the name of the class listing the accessible fields of the LocalDate.
     */
    String getDateFieldEnumClass();

    /**
     * Returns the field for the day of the month in the {@link #getDateFieldEnumClass()
     * DateFieldEnumClass}.
     */
    String getDayOfMonthField();

    /**
     * Returns the field for the month of the year in the {@link #getDateFieldEnumClass()
     * DateFieldEnumClass}.
     */

    String getMonthOfYearField();

    /**
     * Returns the field for the year in the {@link #getDateFieldEnumClass() DateFieldEnumClass}.
     */
    String getYearField();

}
