/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.joda.BaseJodaDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateDatatype;

public class LocalDateToGregorianCalendarCg extends AbstractSingleConversionCg {

    public LocalDateToGregorianCalendarCg() {
        super(LocalDateDatatype.DATATYPE, Datatype.GREGORIAN_CALENDAR);
    }

    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        // JodaUtil.toGregorianCalendar(fromValue)
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(BaseJodaDatatypeHelper.ORG_FAKTORIPS_UTIL_JODA_UTIL) //
                .append(".toGregorianCalendar(").append(fromValue).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
        return fragment;
    }

}
