/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers.java8;

import java.time.format.DateTimeFormatter;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractTimeHelper;
import org.faktorips.datatype.joda.LocalDateTimeDatatype;
import org.faktorips.values.ObjectUtil;

public class LocalDateTimeHelper extends AbstractTimeHelper {

    private static final String JAVA_TIME_LOCAL_DATE_TIME = "java.time.LocalDateTime"; //$NON-NLS-1$

    public LocalDateTimeHelper(LocalDateTimeDatatype datatype) {
        super(datatype);
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return ParseHelper.parse(expression, JAVA_TIME_LOCAL_DATE_TIME);
    }

    @Override
    public String getJavaClassName() {
        return JAVA_TIME_LOCAL_DATE_TIME;
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        return new JavaCodeFragment().appendClassName(ObjectUtil.class).append(".isNull(").append(fieldName) //$NON-NLS-1$
                .append(")").append(" ? null : ") //$NON-NLS-1$ //$NON-NLS-2$
                .append(fieldName).append(".format(").appendClassName(DateTimeFormatter.class)
                .append(".ISO_LOCAL_DATE_TIME)");
    }
}
