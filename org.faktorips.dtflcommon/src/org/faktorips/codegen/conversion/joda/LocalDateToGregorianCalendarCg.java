/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion.joda;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.conversion.AbstractSingleConversionCg;
import org.faktorips.codegen.dthelpers.joda.BaseJodaDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateDatatype;

public class LocalDateToGregorianCalendarCg extends AbstractSingleConversionCg {

    public LocalDateToGregorianCalendarCg() {
        super(LocalDateDatatype.DATATYPE, Datatype.GREGORIAN_CALENDAR);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // JodaUtil.toGregorianCalendar(fromValue)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(BaseJodaDatatypeHelper.ORG_FAKTORIPS_UTIL_JODA_UTIL)
        .append(".toGregorianCalendar(").append(fromValue).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        return fragment;
    }

}
