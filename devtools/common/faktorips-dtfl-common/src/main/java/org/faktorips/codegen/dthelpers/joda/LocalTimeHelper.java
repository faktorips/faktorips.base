/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.joda;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.values.ObjectUtil;

/**
 * {@link DatatypeHelper} for {@link LocalTimeDatatype}.
 */
public class LocalTimeHelper extends BaseJodaDatatypeHelper {

    public static final String ORG_JODA_TIME_LOCAL_TIME = "org.joda.time.LocalTime"; //$NON-NLS-1$

    private static final String PARSE_METHOD = "toLocalTime"; //$NON-NLS-1$

    private static final String TIME_NO_MILLIS = "timeNoMillis"; //$NON-NLS-1$

    public LocalTimeHelper() {
        super(ORG_JODA_TIME_LOCAL_TIME, PARSE_METHOD);
    }

    public LocalTimeHelper(LocalTimeDatatype d) {
        super(d, ORG_JODA_TIME_LOCAL_TIME, PARSE_METHOD);
    }

    @Override
    protected void appendToStringParameter(JavaCodeFragment fragment) {
        fragment.appendClassName(ORG_JODA_TIME_FORMAT_ISO_DATE_TIME_FORMAT);
        fragment.append('.');
        fragment.append(TIME_NO_MILLIS);
        fragment.append("()"); //$NON-NLS-1$
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        return new JavaCodeFragment().appendClassName(ObjectUtil.class).append(".isNull(").append(fieldName) //$NON-NLS-1$
                .append(")").append(" ? null : ") //$NON-NLS-1$ //$NON-NLS-2$
                .appendClassName(ORG_JODA_TIME_FORMAT_ISO_DATE_TIME_FORMAT).append(".hourMinuteSecond().print(")
                .append(fieldName).append(")");
    }
}
