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
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.datatype.joda.MonthDayDatatype;

/**
 * {@link DatatypeHelper} for {@link LocalTimeDatatype}.
 */
public class MonthDayHelper extends BaseJodaDatatypeHelper {

    public static final String ORG_JODA_TIME_MONTH_DAY = "org.joda.time.MonthDay"; //$NON-NLS-1$

    private static final String PARSE_METHOD = "toMonthDay"; //$NON-NLS-1$

    public MonthDayHelper() {
        super(ORG_JODA_TIME_MONTH_DAY, PARSE_METHOD);
    }

    public MonthDayHelper(MonthDayDatatype d) {
        super(d, ORG_JODA_TIME_MONTH_DAY, PARSE_METHOD);
    }
}
