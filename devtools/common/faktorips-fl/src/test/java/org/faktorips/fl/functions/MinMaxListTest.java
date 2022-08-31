/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.JavaExprCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MinMaxListTest {

    private MinMaxList maxList;
    private JavaCodeFragment argumentFragment;
    private ListOfTypeDatatype datatype;
    private CompilationResultImpl[] argument;

    @Mock
    private CompilationResultImpl argumentCompilationResult;

    @Before
    public void setUp() {
        maxList = new MinMaxList("max", "", true);
    }

    public void spyFunctionWithDatatype(Datatype elementDatatype) {
        argumentFragment = new JavaCodeFragment("valueList");
        argument = new CompilationResultImpl[] { argumentCompilationResult };

        datatype = new ListOfTypeDatatype(elementDatatype);
        CompilationResultImpl arg1Result = new CompilationResultImpl("currentResult", elementDatatype);
        CompilationResultImpl arg2Result = new CompilationResultImpl("nextValue", elementDatatype);

        when(argumentCompilationResult.getCodeFragment()).thenReturn(argumentFragment);
        when(argumentCompilationResult.getDatatype()).thenReturn(datatype);
        maxList = spy(maxList);
        CompilationResultImpl fragment = new CompilationResultImpl(
                new JavaCodeFragment("currentResult.max(nextValue)"), Datatype.DECIMAL);
        doReturn(fragment).when(maxList).generateFunctionCall(arg1Result, arg2Result);
        maxList.setCompiler(new JavaExprCompiler());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompile_NumberOfArgumentsZero() {
        spyFunctionWithDatatype(Datatype.DECIMAL);

        maxList.compile(new CompilationResultImpl[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompile_NumberOfArgumentsMany() {
        spyFunctionWithDatatype(Datatype.DECIMAL);

        maxList.compile(new CompilationResultImpl[2]);
    }

    @Test
    public void testCompile_Max() {
        spyFunctionWithDatatype(Datatype.DECIMAL);

        CompilationResult<JavaCodeFragment> compile = maxList.compile(argument);

        assertNotNull(compile);
        assertEquals(
                "new FunctionWithListAsArgumentHelper<Decimal>(){\n@Override public Decimal getPreliminaryResult(Decimal currentResult, Decimal nextValue){return currentResult.max(nextValue);}\n@Override public Decimal getFallBackValue(){throw new IllegalArgumentException(\"List argument is empty or null\");}}.getResult(valueList)",
                compile.getCodeFragment().getSourcecode());
    }

    @Test
    public void testCompile_invalidElementDatatype() {
        spyFunctionWithDatatype(Datatype.VOID);

        CompilationResult<JavaCodeFragment> compResult = maxList.compile(argument);
        assertNotNull(compResult);
        assertTrue(compResult.failed());
    }

    @Test
    public void testCompile_comparableElementDatatype() {
        spyFunctionWithDatatype(Datatype.BIG_DECIMAL);

        CompilationResult<JavaCodeFragment> compResult = maxList.compile(argument);
        assertNotNull(compResult);
        assertFalse(compResult.failed());
    }

    @Test
    public void testValidateBasicDatatype_comparableElementDatatype() {
        spyFunctionWithDatatype(Datatype.BIG_DECIMAL);

        CompilationResult<JavaCodeFragment> compResult = maxList.validateBasicDatatype(Datatype.DOUBLE);
        assertNull(compResult);
    }

    @Test
    public void testValidateBasic_invalidElementDatatype() {
        CompilationResult<JavaCodeFragment> compResult = maxList.validateBasicDatatype(Datatype.VOID);
        assertNotNull(compResult);
        assertTrue(compResult.failed());
    }
}
