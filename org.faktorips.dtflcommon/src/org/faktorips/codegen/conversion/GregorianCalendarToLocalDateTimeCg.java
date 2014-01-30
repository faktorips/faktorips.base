/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;

public class GregorianCalendarToLocalDateTimeCg extends AbstractSingleConversionCg {

    public GregorianCalendarToLocalDateTimeCg() {
        super(Datatype.GREGORIAN_CALENDAR, LocalDateTimeDatatype.DATATYPE);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // LocalDateTime.fromCalendarFields(fromValue)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(LocalDateTimeDatatype.ORG_JODA_TIME_LOCAL_DATE_TIME) //
                .append(".fromCalendarFields(").append(fromValue).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        return fragment;
    }

}
