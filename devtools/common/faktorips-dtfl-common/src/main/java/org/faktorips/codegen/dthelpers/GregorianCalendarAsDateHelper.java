/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import java.util.GregorianCalendar;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.GregorianCalendarAsDateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.DateUtil;

/**
 * {@link DatatypeHelper} for {@link GregorianCalendarAsDateDatatype}.
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

    @Override
    public String getJavaClassName() {
        return GregorianCalendar.class.getName();
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (IpsStringUtils.isEmpty(value)) {
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
    public JavaCodeFragment getToStringExpression(String fieldName) {
        if (IpsStringUtils.isEmpty(fieldName)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(DateUtil.class);
        fragment.append(".gregorianCalendarToIsoDateString("); //$NON-NLS-1$
        fragment.append(fieldName);
        fragment.append(")"); //$NON-NLS-1$
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (IpsStringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(DateUtil.class);
        fragment.append(".parseIsoDateStringToGregorianCalendar("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(")"); //$NON-NLS-1$
        return fragment;
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
