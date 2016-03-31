/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.datatype.joda.LocalDateDatatype;

public class LocalDateHelper extends AbstractTimeHelper {

    private static final String JAVA_TIME_LOCAL_DATE = "java.time.LocalDate"; //$NON-NLS-1$

    public LocalDateHelper(LocalDateDatatype datatype) {
        super(datatype);
    }

    @Override
    protected JavaCodeFragment valueOfExpression(String expression) {
        return ParseHelper.parse(expression, JAVA_TIME_LOCAL_DATE);
    }

    @Override
    public String getJavaClassName() {
        return JAVA_TIME_LOCAL_DATE;
    }

}
