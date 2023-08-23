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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractTimeHelper;
import org.faktorips.datatype.joda.LocalTimeDatatype;
import org.faktorips.values.ObjectUtil;

public class LocalTimeHelper extends AbstractTimeHelper {

    private static final String JAVA_TIME_LOCAL_TIME = "java.time.LocalTime"; //$NON-NLS-1$

    public LocalTimeHelper(LocalTimeDatatype d) {
        super(d);
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return ParseHelper.parse(expression, JAVA_TIME_LOCAL_TIME);
    }

    @Override
    public String getJavaClassName() {
        return JAVA_TIME_LOCAL_TIME;
    }

    @Override
    public JavaCodeFragment getToStringExpression(String fieldName) {
        return new JavaCodeFragment().appendClassName(ObjectUtil.class).append(".isNull(").append(fieldName) //$NON-NLS-1$
                .append(")").append(" ? null : ") //$NON-NLS-1$ //$NON-NLS-2$
                .append(fieldName).append(".format(").appendClassName(DateTimeFormatter.class)
                .append(".ofPattern(\"HH:mm:ss\"))");
    }
}
