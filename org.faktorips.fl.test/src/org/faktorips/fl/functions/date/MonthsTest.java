/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions.date;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.functions.FunctionAbstractTest;
import org.junit.Test;

public class MonthsTest extends FunctionAbstractTest {

    private Months months;
    private Date date;

    @Test
    public void testCompileJoda() throws Exception {
        months = new Months("MONTHS", "");
        date = new Date("DATE", "");
        registerFunction(months);
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new org.faktorips.codegen.dthelpers.joda.LocalDateHelper());

        CompilationResult<JavaCodeFragment> compile = getCompiler().compile(
                "MONTHS(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals(
                "Integer.valueOf(Months.monthsBetween(new LocalDate(2014, 02, 01), new LocalDate(2014, 03, 08)).getMonths())",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
        assertThat(imports, hasItem("org.joda.time.Months"));
    }

    @Test
    public void testCompileJava8() throws Exception {
        months = new Months("MONTHS", "");
        date = new Date("DATE", "");
        registerFunction(months);
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new org.faktorips.codegen.dthelpers.java8.LocalDateHelper(
                LocalDateDatatype.DATATYPE));

        CompilationResult<JavaCodeFragment> compile = getCompiler().compile(
                "MONTHS(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals(
                "Integer.valueOf(Period.between(LocalDate.of(2014, 02, 01), LocalDate.of(2014, 03, 08)).getMonths())",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("java.time.LocalDate"));
        assertThat(imports, hasItem("java.time.Period"));
    }

}
