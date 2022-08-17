/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.joda;

import java.util.regex.Pattern;

import org.faktorips.datatype.ValueClassNameDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;

public class MonthDayDatatype extends ValueClassNameDatatype {

    public static final MonthDayDatatype DATATYPE = new MonthDayDatatype();
    public static final String NAME = "MonthDay"; //$NON-NLS-1$

    private static final Pattern PATERN_ISO_MONTH_DAY = Pattern.compile("^--\\d{2}-\\d{2}$"); //$NON-NLS-1$

    public MonthDayDatatype() {
        super(NAME);
    }

    @Override
    public Object getValue(String value) {
        return value;
    }

    @Override
    public boolean supportsCompare() {
        return true;
    }

    @Override
    public boolean isParsable(String value) {
        return IpsStringUtils.isEmpty(value) || isIsoMonthDay(value);
    }

    private boolean isIsoMonthDay(String value) {
        return (value == null) ? false : PATERN_ISO_MONTH_DAY.matcher(value).matches();
    }
}
