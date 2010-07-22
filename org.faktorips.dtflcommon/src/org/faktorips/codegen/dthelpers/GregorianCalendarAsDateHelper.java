/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.values.DateUtil;

/**
 * DatatypeHelper for datatype GregorianCalendarAsDate.
 */
public class GregorianCalendarAsDateHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public GregorianCalendarAsDateHelper() {
        super();
    }

    /**
     * Constructs a new helper for the given boolean datatype.
     * 
     * @throws IllegalArgumentException if datatype is <code>null</code>.
     */
    public GregorianCalendarAsDateHelper(GregorianCalendarAsDateDatatype datatype) {
        super(datatype);
    }

    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        GregorianCalendar date = (GregorianCalendar)new GregorianCalendarDatatype("", false).getValue(value); //$NON-NLS-1$
        fragment.append("new "); //$NON-NLS-1$
        fragment.appendClassName(GregorianCalendar.class);
        fragment.append('(');
        fragment.append(date.get(GregorianCalendar.YEAR));
        fragment.append(", "); //$NON-NLS-1$
        fragment.append(date.get(GregorianCalendar.MONTH));
        fragment.append(", "); //$NON-NLS-1$
        fragment.append(date.get(GregorianCalendar.DATE));
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(DateUtil.class);
        fragment.append(".parseIsoDateStringToGregorianCalendar("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(")"); //$NON-NLS-1$
        return fragment;
    }

    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null"); //$NON-NLS-1$
    }

    @Override
    protected JavaCodeFragment newSafeCopy(String expression) {
        // sample code: (GregorianCalendar) (calendar == null ? null : calendar.clone());
        JavaCodeFragment code = new JavaCodeFragment();
        code.append(expression);
        code.append(" == null ? null : "); //$NON-NLS-1$
        code.append('(');
        code.appendClassName(GregorianCalendar.class);
        code.append(")"); //$NON-NLS-1$
        code.append(expression);
        code.append(".clone()"); //$NON-NLS-1$
        return code;
    }

}
