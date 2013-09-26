/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.fl.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MinMaxListTest {

    private MinMaxList maxList = new MinMaxList("max", "", true);
    private JavaCodeFragment argumentFragment = new JavaCodeFragment("valueList");
    private ListOfTypeDatatype datatype = new ListOfTypeDatatype(Datatype.DECIMAL);

    @Mock
    CompilationResultImpl argumentCompilationResult;

    @Test(expected = IllegalArgumentException.class)
    public void testCompile_NumberOfArgumentsZero() {
        maxList.compile(new CompilationResultImpl[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompile_NumberOfArgumentsMany() {
        maxList.compile(new CompilationResultImpl[2]);
    }

    @Test
    public void testCompile_Max() {
        CompilationResultImpl arg1Result = new CompilationResultImpl("currentResult", Datatype.DECIMAL);
        CompilationResultImpl arg2Result = new CompilationResultImpl("nextValue", Datatype.DECIMAL);
        when(argumentCompilationResult.getCodeFragment()).thenReturn(argumentFragment);
        when(argumentCompilationResult.getDatatype()).thenReturn(datatype);
        maxList = spy(maxList);
        JavaCodeFragment fragment = new JavaCodeFragment("currentResult.max(nextValue)");
        doReturn(fragment).when(maxList).generateFunctionCall(arg1Result, arg2Result);

        CompilationResultImpl[] argument = new CompilationResultImpl[] { argumentCompilationResult };

        CompilationResult<JavaCodeFragment> compile = maxList.compile(argument);
        assertNotNull(compile);

        assertEquals(
                "new FunctionWithListAsArgumentHelper<Decimal>(){\n@Override public Decimal getPreliminaryResult(Decimal currentResult, Decimal nextValue){return currentResult.max(nextValue);}\n@Override public Decimal getFallBackValue(){throw new IllegalArgumentException(\"List argument is empty or null\");}}.getResult(valueList)",
                compile.getCodeFragment().getSourcecode());
    }
}
