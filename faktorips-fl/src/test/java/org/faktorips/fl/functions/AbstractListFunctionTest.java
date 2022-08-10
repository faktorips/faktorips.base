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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.fl.AbstractCompilationResult;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.FunctionSignatures;
import org.faktorips.fl.JavaExprCompiler;
import org.faktorips.runtime.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractListFunctionTest {

    private static final String MY_DATATYPE = "myDatatype";

    @Mock
    private JavaExprCompiler compiler;

    @Mock
    private Datatype datatype;

    private AbstractListFunction abstractListFunction;

    @Mock
    private DatatypeHelper datatypeHelper;

    @Before
    public void setUp() {
        abstractListFunction = new AbstractListFunction("myFunction", "", FunctionSignatures.MinList) {

            @Override
            protected JavaCodeFragment generateReturnFallBackValueCall(Datatype datatype) {
                return new JavaCodeFragment();
            }
        };
        abstractListFunction.setCompiler(compiler);
        when(datatype.getName()).thenReturn(MY_DATATYPE);
    }

    @Test
    public void testGenerateFunctionCode_failDelegate() throws Exception {
        AbstractCompilationResult<JavaCodeFragment> listArgument = mockDelegateFunction(new CompilationResultImpl(
                Message.newError("", "")));

        CompilationResult<JavaCodeFragment> generateFunctionCode = abstractListFunction
                .generateFunctionCode(listArgument);

        assertTrue(generateFunctionCode.failed());
    }

    @Test
    public void testGenerateFunctionCode_successDelegate() throws Exception {
        AbstractCompilationResult<JavaCodeFragment> listArgument = mockDelegateFunction(new CompilationResultImpl());

        CompilationResult<JavaCodeFragment> generateFunctionCode = abstractListFunction
                .generateFunctionCode(listArgument);

        assertTrue(generateFunctionCode.successfull());
    }

    private AbstractCompilationResult<JavaCodeFragment> mockDelegateFunction(
            CompilationResult<JavaCodeFragment> delegateResult) {
        when(compiler.getDatatypeHelper(datatype)).thenReturn(datatypeHelper);
        when(datatypeHelper.getJavaClassName()).thenReturn("MyJavaClass");
        AbstractCompilationResult<JavaCodeFragment> listArgument = mock(CompilationResultImpl.class);
        ListOfTypeDatatype listDatatype = new ListOfTypeDatatype(datatype);
        when(listArgument.getDatatype()).thenReturn(listDatatype);
        CompilationResultImpl arg1Result = new CompilationResultImpl("currentResult", datatype);
        CompilationResultImpl arg2Result = new CompilationResultImpl("nextValue", datatype);
        when(
                compiler.getMatchingFunctionUsingConversion(new CompilationResultImpl[] { arg1Result, arg2Result },
                        new Datatype[] { datatype, datatype }, "myFunction")).thenReturn(delegateResult);
        when(listArgument.getCodeFragment()).thenReturn(new JavaCodeFragment("argumentCode"));
        return listArgument;
    }
}
