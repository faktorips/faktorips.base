/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions.joda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;

import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.functions.FunctionAbstractTest;
import org.junit.Test;

public class MonthsTest extends FunctionAbstractTest {

    private Months months;
    private Date date;

    @Test
    public void testCompile() throws Exception {

        months = new Months("MONTHS", "");
        date = new Date("DATE", "");
        registerFunction(months);
        registerFunction(date);

        CompilationResult<JavaCodeFragment> compile = compiler
                .compile("MONTHS(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals(
                "Integer.valueOf(Months.monthsBetween(new LocalDate(2014, 02, 01), new LocalDate(2014, 03, 08)).getMonths())",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
        assertThat(imports, hasItem("org.joda.time.Months"));
    }

}
