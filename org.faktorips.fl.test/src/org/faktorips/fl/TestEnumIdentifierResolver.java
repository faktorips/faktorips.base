/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl;

import org.faktorips.codegen.JavaCodeFragment;

/**
 * 
 * @author Jan Ortmann
 */
public class TestEnumIdentifierResolver extends DefaultIdentifierResolver {

    /**
     * 
     */
    public TestEnumIdentifierResolver() {
        super();
        JavaCodeFragment codeMonth = new JavaCodeFragment();
        codeMonth.appendClassName(TestEnum.class);
        codeMonth.append(".MONTH");
        register("TestEnum.MONTH", codeMonth, TestEnumDatatype.INSTANCE);

        JavaCodeFragment codeYear = new JavaCodeFragment();
        codeYear.appendClassName(TestEnum.class);
        codeYear.append(".YEAR");
        register("TestEnum.YEAR", codeYear, TestEnumDatatype.INSTANCE);
    }

}
