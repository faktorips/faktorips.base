/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion.java8;

import java.util.GregorianCalendar;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.conversion.AbstractSingleConversionCg;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;

public class LocalDateTimeToGregorianCalendarCg extends AbstractSingleConversionCg {

    public LocalDateTimeToGregorianCalendarCg() {
        super(LocalDateTimeDatatype.DATATYPE, Datatype.GREGORIAN_CALENDAR);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment f) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.addImport(LocalDateToGregorianCalendarCg.JAVA_TIME_ZONE_ID);
        fragment.addImport(GregorianCalendar.class.getName());
        // f == null ? null : GregorianCalendar.from(f.atZone(ZoneId.systemDefault()));
        fragment.append(f).append(" == null ? null : "); //$NON-NLS-1$
        fragment.append("GregorianCalendar.from(").append(f).append(".atZone(ZoneId.systemDefault()))"); //$NON-NLS-1$ //$NON-NLS-2$
        return fragment;
    }

}
