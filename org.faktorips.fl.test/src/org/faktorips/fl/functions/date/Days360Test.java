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

public class Days360Test extends FunctionAbstractTest {

    private Days360 days;
    private Date date;

    @Test
    public void testCompileJoda() throws Exception {
        days = new Days360("DAYS360", "");
        date = new Date("DATE", "");
        registerFunction(days);
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new org.faktorips.codegen.dthelpers.joda.LocalDateHelper());

        CompilationResult<JavaCodeFragment> compile = getCompiler().compile(
                "DAYS360(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();
        String resultingSourcecode = "Integer.valueOf(((new LocalDate(2014, 03, 08).get(DateTimeFieldType.year()) - new LocalDate(2014, 02, 01).get(DateTimeFieldType.year())) * 360 + (new LocalDate(2014, 03, 08).get(DateTimeFieldType.monthOfYear()) - new LocalDate(2014, 02, 01).get(DateTimeFieldType.monthOfYear())) * 30 + (Math.min(new LocalDate(2014, 03, 08).get(DateTimeFieldType.dayOfMonth()), 30) - Math.min(new LocalDate(2014, 02, 01).get(DateTimeFieldType.dayOfMonth()), 30))))";

        assertEquals(resultingSourcecode, compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
        assertThat(imports, hasItem("org.joda.time.DateTimeFieldType"));
    }

    @Test
    public void testCompileJava8() throws Exception {
        days = new Days360("DAYS360", "");
        date = new Date("DATE", "");
        registerFunction(days);
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new org.faktorips.codegen.dthelpers.java8.LocalDateHelper(
                LocalDateDatatype.DATATYPE));

        CompilationResult<JavaCodeFragment> compile = getCompiler().compile(
                "DAYS360(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();
        String resultingSourcecode = "Integer.valueOf(((LocalDate.of(2014, 03, 08).get(ChronoField.YEAR) - LocalDate.of(2014, 02, 01).get(ChronoField.YEAR)) * 360 + (LocalDate.of(2014, 03, 08).get(ChronoField.MONTH_OF_YEAR) - LocalDate.of(2014, 02, 01).get(ChronoField.MONTH_OF_YEAR)) * 30 + (Math.min(LocalDate.of(2014, 03, 08).get(ChronoField.DAY_OF_MONTH), 30) - Math.min(LocalDate.of(2014, 02, 01).get(ChronoField.DAY_OF_MONTH), 30))))";

        assertEquals(resultingSourcecode, compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("java.time.LocalDate"));
        assertThat(imports, hasItem("java.time.temporal.ChronoField"));
    }
}
