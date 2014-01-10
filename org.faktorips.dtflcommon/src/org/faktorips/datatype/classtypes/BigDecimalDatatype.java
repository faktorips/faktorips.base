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

package org.faktorips.datatype.classtypes;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;

/**
 * Data type for {@link BigDecimal}.
 * 
 * @author Jan Ortmann
 */
public class BigDecimalDatatype extends ValueClassDatatype implements NumericDatatype {

    public BigDecimalDatatype() {
        super(BigDecimal.class);
    }

    @Override
    public Object getValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return new BigDecimal(value);
    }

    public boolean supportsCompare() {
        return true;
    }

    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }
        BigDecimal a = (BigDecimal)getValue(dividend);
        BigDecimal b = (BigDecimal)getValue(divisor);
        try {
            a.divide(b, 0, BigDecimal.ROUND_UNNECESSARY);
        } catch (ArithmeticException e) {
            return false;
        }
        return true;
    }

    public boolean hasDecimalPlaces() {
        return true;
    }

    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null."); //$NON-NLS-1$
        }
        return ((BigDecimal)getValue(minuend)).subtract((BigDecimal)getValue(subtrahend)).toString();
    }

}
