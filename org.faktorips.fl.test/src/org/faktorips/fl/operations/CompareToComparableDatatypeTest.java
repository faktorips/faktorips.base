/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.joda.LocalDateHelper;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.functions.FunctionAbstractTest;
import org.faktorips.fl.functions.date.Date;
import org.junit.Test;

public class CompareToComparableDatatypeTest extends FunctionAbstractTest {

    private Date date;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        date = new Date("DATE", "");
        registerFunction(date);
        putDatatypeHelper(LocalDateDatatype.DATATYPE, new LocalDateHelper());
    }

    @Test
    public void testLessThan() throws Exception {
        CompilationResult<JavaCodeFragment> compile = getCompileResult(BinaryOperation.LESSER_THAN);
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals("Boolean.valueOf((new LocalDate(2014, 02, 01).compareTo(new LocalDate(2014, 02, 08)) < 0))",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
    }

    @Test
    public void testLessThanOrEqual() throws Exception {
        CompilationResult<JavaCodeFragment> compile = getCompileResult(BinaryOperation.LESSER_THAN_OR_EQUAL);
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals("Boolean.valueOf((new LocalDate(2014, 02, 01).compareTo(new LocalDate(2014, 02, 08)) <= 0))",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
    }

    @Test
    public void testGreaterThan() throws Exception {
        CompilationResult<JavaCodeFragment> compile = getCompileResult(BinaryOperation.GREATER_THAN);
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals("Boolean.valueOf((new LocalDate(2014, 02, 01).compareTo(new LocalDate(2014, 02, 08)) > 0))",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
    }

    @Test
    public void testGreaterThanOrEqual() throws Exception {
        CompilationResult<JavaCodeFragment> compile = getCompileResult(BinaryOperation.GREATER_THAN_OR_EQUAL);
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();

        assertEquals("Boolean.valueOf((new LocalDate(2014, 02, 01).compareTo(new LocalDate(2014, 02, 08)) >= 0))",
                compile.getCodeFragment().getSourcecode());
        assertThat(imports, hasItem("org.joda.time.LocalDate"));
    }

    private CompilationResult<JavaCodeFragment> getCompileResult(String operator) {
        getCompiler().setBinaryOperations(
                toArray(new CompareToComparableDatatype(operator, LocalDateDatatype.DATATYPE)));
        CompilationResult<JavaCodeFragment> compile = getCompiler().compile(
                "DATE(2014; 02; 01)" + operator + "DATE(2014; 02; 08)");
        return compile;
    }
}
