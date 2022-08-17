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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractTimeHelper;
import org.faktorips.datatype.joda.MonthDayDatatype;

public class MonthDayHelper extends AbstractTimeHelper {

    private static final String JAVA_TIME_MONTH_DAY = "java.time.MonthDay"; //$NON-NLS-1$

    public MonthDayHelper(MonthDayDatatype d) {
        super(d);
    }

    @Override
    public String getJavaClassName() {
        return JAVA_TIME_MONTH_DAY;
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return ParseHelper.parse(expression, JAVA_TIME_MONTH_DAY);
    }

}
