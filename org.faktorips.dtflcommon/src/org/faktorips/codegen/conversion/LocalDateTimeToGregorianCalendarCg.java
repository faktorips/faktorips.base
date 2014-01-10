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

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.joda.BaseJodaDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;

public class LocalDateTimeToGregorianCalendarCg extends AbstractSingleConversionCg {

    public LocalDateTimeToGregorianCalendarCg() {
        super(LocalDateTimeDatatype.DATATYPE, Datatype.GREGORIAN_CALENDAR);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // JodaUtil.toGregorianCalendar(fromValue)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(BaseJodaDatatypeHelper.ORG_FAKTORIPS_UTIL_JODA_UTIL) //
                .append(".toGregorianCalendar(").append(fromValue).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        return fragment;
    }

}
