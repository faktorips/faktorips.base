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

import java.util.Date;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.values.DateUtil;
import org.faktorips.values.ObjectUtil;
import org.faktorips.valueset.DefaultRange;

/**
 * {@link DatatypeHelper} for {@link DateDatatype}.
 * 
 * 
 * @author Peter Erzberger
 */
public class DateHelper extends AbstractDatatypeHelper {

    /**
     * Constructs a new helper.
     */
    public DateHelper() {
        super();
    }

    public DateHelper(DateDatatype datatype) {
        super(datatype);
    }

    @Override
    public String getJavaClassName() {
        return Date.class.getName();
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        if (IpsStringUtils.isEmpty(fieldName)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(ObjectUtil.class);
        fragment.append(".isNull("); //$NON-NLS-1$
        fragment.append(fieldName);
        fragment.append(")"); //$NON-NLS-1$
        fragment.append(" ? null : "); //$NON-NLS-1$
        fragment.appendClassName(DateUtil.class);
        fragment.append(".dateToIsoDateString("); //$NON-NLS-1$
        fragment.append(fieldName);
        fragment.append(')');
        return fragment;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        if (IpsStringUtils.isEmpty(expression)) {
            return nullExpression();
        }
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendClassName(DateUtil.class);
        fragment.append(".parseIsoDateStringToDate("); //$NON-NLS-1$
        fragment.append(expression);
        fragment.append(')');
        return fragment;
    }

    @Override
    public JavaCodeFragment newInstance(String value) {
        if (value == null) {
            return valueOfExpression(value);
        }
        StringBuilder sb = new StringBuilder();
        sb.append('"').append(value).append('"');
        return valueOfExpression(sb.toString());
    }

    @Override
    public String getRangeJavaClassName(boolean useTypesafeCollections) {
        return DefaultRange.class.getName() + "<" + Date.class.getName() + ">"; //$NON-NLS-1$//$NON-NLS-2$
    }

    @Override
    public JavaCodeFragment newRangeInstance(JavaCodeFragment lowerBoundExp,
            JavaCodeFragment upperBoundExp,
            JavaCodeFragment stepExp,
            JavaCodeFragment containsNullExp,
            boolean useTypesafeCollections) {

        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendClassName(getRangeJavaClassName(useTypesafeCollections));
        frag.append(".valueOf("); //$NON-NLS-1$
        frag.append(lowerBoundExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(upperBoundExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(stepExp);
        frag.append(", "); //$NON-NLS-1$
        frag.append(containsNullExp);
        frag.append(")"); //$NON-NLS-1$
        return frag;
    }

}
