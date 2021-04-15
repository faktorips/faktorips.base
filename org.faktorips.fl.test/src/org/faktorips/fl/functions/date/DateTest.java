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

public class DateTest extends FunctionAbstractTest {

    @Test
    public void testCompileJoda() throws Exception {
        Date date = new Date("DATE", "");
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new org.faktorips.codegen.dthelpers.joda.LocalDateHelper());

        CompilationResult<JavaCodeFragment> compile = getCompiler().compile("DATE(2014; 02; 01)");
        Set<String> importDeclaration = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals("new LocalDate(2014, 02, 01)", compile.getCodeFragment().getSourcecode());
        assertThat(importDeclaration, hasItem("org.joda.time.LocalDate"));
    }

    @Test
    public void testCompileJava8() throws Exception {
        Date date = new Date("DATE", "");
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new org.faktorips.codegen.dthelpers.java8.LocalDateHelper(
                LocalDateDatatype.DATATYPE));

        CompilationResult<JavaCodeFragment> compile = getCompiler().compile("DATE(2014; 02; 01)");
        Set<String> importDeclaration = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals("LocalDate.of(2014, 02, 01)", compile.getCodeFragment().getSourcecode());
        assertThat(importDeclaration, hasItem("java.time.LocalDate"));
    }

}
