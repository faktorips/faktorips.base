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

public class YearsTest extends FunctionAbstractTest {

    private Years years;
    private Date date;

    @Test
    public void testCompileJoda() throws Exception {
        years = new Years("YEARS", "");
        date = new Date("DATE", "");
        registerFunction(years);
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new org.faktorips.codegen.dthelpers.joda.LocalDateHelper());

        CompilationResult<JavaCodeFragment> compile = getCompiler().compile(
                "YEARS(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals(
                "Integer.valueOf(Years.yearsBetween(new LocalDate(2014, 02, 01), new LocalDate(2014, 03, 08)).getYears())",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
        assertThat(imports, hasItem("org.joda.time.Years"));
    }

    @Test
    public void testCompileJava8() throws Exception {
        years = new Years("YEARS", "");
        date = new Date("DATE", "");
        registerFunction(years);
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new org.faktorips.codegen.dthelpers.java8.LocalDateHelper(
                LocalDateDatatype.DATATYPE));

        CompilationResult<JavaCodeFragment> compile = getCompiler().compile(
                "YEARS(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals(
                "Integer.valueOf(Period.between(LocalDate.of(2014, 02, 01), LocalDate.of(2014, 03, 08)).getYears())",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("java.time.LocalDate"));
        assertThat(imports, hasItem("java.time.Period"));
    }

}
