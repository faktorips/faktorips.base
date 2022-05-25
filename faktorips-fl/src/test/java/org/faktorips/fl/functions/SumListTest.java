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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.JavaExprCompiler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SumListTest {
    private SumList sumList = new SumList("sum", "");
    private JavaCodeFragment argumentFragment = new JavaCodeFragment("valueList");
    private ListOfTypeDatatype datatype = new ListOfTypeDatatype(Datatype.DECIMAL);

    @Mock
    private CompilationResultImpl argumentCompilationResult;

    @Mock
    private JavaExprCompiler compiler;

    private DatatypeHelper helper = new DecimalHelper();

    @Test(expected = IllegalArgumentException.class)
    public void testCompile_NumberOfArgumentsZero() {
        sumList.compile(new CompilationResultImpl[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompile_NumberOfArgumentsMany() {
        sumList.compile(new CompilationResultImpl[2]);
    }

    @Test
    public void testCompile_Sum() {
        CompilationResultImpl arg1Result = new CompilationResultImpl("currentResult", Datatype.DECIMAL);
        CompilationResultImpl arg2Result = new CompilationResultImpl("nextValue", Datatype.DECIMAL);
        when(argumentCompilationResult.getCodeFragment()).thenReturn(argumentFragment);
        when(argumentCompilationResult.getDatatype()).thenReturn(datatype);
        sumList = spy(sumList);
        CompilationResultImpl fragment = new CompilationResultImpl(
                new JavaCodeFragment("currentResult.add(nextValue)"), Datatype.DECIMAL);
        doReturn(fragment).when(sumList).generateFunctionCall(arg1Result, arg2Result);

        doReturn(compiler).when(sumList).getCompiler();
        when(sumList.getCompiler().getDatatypeHelper(Datatype.DECIMAL)).thenReturn(helper);

        CompilationResultImpl[] argument = new CompilationResultImpl[] { argumentCompilationResult };

        CompilationResult<JavaCodeFragment> compile = sumList.compile(argument);
        assertNotNull(compile);

        assertEquals(
                "new FunctionWithListAsArgumentHelper<Decimal>(){\n@Override public Decimal getPreliminaryResult(Decimal currentResult, Decimal nextValue){return currentResult.add(nextValue);}\n@Override public Decimal getFallBackValue(){return Decimal.valueOf(\"0\");}}.getResult(valueList)",
                compile.getCodeFragment().getSourcecode());
    }

    @Test
    public void testValidateDatatype_invalidDatatype() {
        CompilationResult<JavaCodeFragment> result = sumList.validateBasicDatatype(Datatype.BOOLEAN);
        assertNotNull(result);
        assertEquals(SumList.MSG_CODE_SUM_INVALID_DATATYPE, result.getMessages().getMessage(0).getCode());
    }

    @Test
    public void testValidateDatatype_ValidDatatype() {
        CompilationResult<JavaCodeFragment> result = sumList.validateBasicDatatype(Datatype.DECIMAL);
        assertNull(result);
    }
}
