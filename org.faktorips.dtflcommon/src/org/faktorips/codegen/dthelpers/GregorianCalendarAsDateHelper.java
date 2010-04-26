/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

    /**
     * Overridden method.
     * 
     * @see org.faktorips.codegen.DatatypeHelper#newInstance(java.lang.String)
     */
    public JavaCodeFragment newInstance(String value) {
        if (StringUtils.isEmpty(value)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        GregorianCalendar date = (GregorianCalendar)new GregorianCalendarDatatype("", false).getValue(value);
        fragment.append("new ");
        fragment.appendClassName(GregorianCalendar.class);
        fragment.append('(');
        fragment.append(date.get(GregorianCalendar.YEAR));
        fragment.append(", ");
        fragment.append(date.get(GregorianCalendar.MONTH));
        fragment.append(", ");
        fragment.append(date.get(GregorianCalendar.DATE));
        fragment.append(')');
        return fragment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.codegen.dthelpers.AbstractDatatypeHelper#valueOfExpression(java.lang.String)
     */
    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (StringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(DateUtil.class);
        fragment.append(".parseIsoDateStringToGregorianCalendar(");
        fragment.append(expression);
        fragment.append(")");
        return fragment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.codegen.DatatypeHelper#nullExpression()
     */
    public JavaCodeFragment nullExpression() {
        return new JavaCodeFragment("null");
    }

    @Override
    protected JavaCodeFragment newSafeCopy(String expression) {
        // sample code: (GregorianCalendar) (calendar == null ? null : calendar.clone());
        JavaCodeFragment code = new JavaCodeFragment();
        code.append(expression);
        code.append(" == null ? null : ");
        code.append('(');
        code.appendClassName(GregorianCalendar.class);
        code.append(")");
        code.append(expression);
        code.append(".clone()");
        return code;
    }

}
