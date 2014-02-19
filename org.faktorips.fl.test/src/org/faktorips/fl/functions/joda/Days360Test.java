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
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.functions.FunctionAbstractTest;
import org.junit.Test;

public class Days360Test extends FunctionAbstractTest {

    private Days360 days;
    private Date date;

    @Test
    public void testCompile() throws Exception {
        days = new Days360("DAYS360", "");
        date = new Date("DATE", "");
        registerFunction(days);
        registerFunction(date);

        CompilationResult<JavaCodeFragment> compile = compiler
                .compile("DAYS360(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();
        String resultingSourcecode = "Integer.valueOf(((new LocalDate(2014, 03, 08).getYear() - new LocalDate(2014, 02, 01).getYear()) * 360 + (new LocalDate(2014, 03, 08).getMonthOfYear() - new LocalDate(2014, 02, 01).getMonthOfYear()) * 30 + (Math.min(new LocalDate(2014, 03, 08).getDayOfMonth(), 30) - Math.min(new LocalDate(2014, 02, 01).getDayOfMonth(), 30))))";

        assertEquals(resultingSourcecode, compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
    }
}
