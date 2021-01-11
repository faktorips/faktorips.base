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
import org.faktorips.datatype.joda.LocalDateDatatype;

public class LocalDateToGregorianCalendarCg extends AbstractSingleConversionCg {

    static final String JAVA_TIME_ZONE_ID = "java.time.ZoneId"; //$NON-NLS-1$

    public LocalDateToGregorianCalendarCg() {
        super(LocalDateDatatype.DATATYPE, Datatype.GREGORIAN_CALENDAR);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment f) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.addImport(JAVA_TIME_ZONE_ID);
        fragment.addImport(GregorianCalendar.class.getName());
        // f == null ? null : GregorianCalendar.from(f.atStartOfDay(ZoneId.systemDefault()));
        fragment.append(f).append(" == null ? null : "); //$NON-NLS-1$
        fragment.append("GregorianCalendar.from(").append(f).append(".atStartOfDay(ZoneId.systemDefault()))"); //$NON-NLS-1$ //$NON-NLS-2$
        return fragment;
    }

}
