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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockitoSession;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

public class AbstractListFunctionTest {


    private static final String MY_DATATYPE = "myDatatype";

    @Mock
    private JavaExprCompiler compiler;

    @Mock
    private Datatype datatype;

    private AbstractListFunction abstractListFunction;

    @Mock
    private DatatypeHelper datatypeHelper;

    private MockitoSession mockito;

    @BeforeEach
    public void setUp() {
        mockito = mockitoSession()
                .initMocks(this)
                .strictness(Strictness.STRICT_STUBS)
                .startMocking();
        abstractListFunction = new AbstractListFunction("myFunction", "", FunctionSignatures.MinList) {

            @Override
            protected JavaCodeFragment generateReturnFallBackValueCall(Datatype datatype) {
                return new JavaCodeFragment();
            }
        };
        abstractListFunction.setCompiler(compiler);
        lenient().when(datatype.getName()).thenReturn(MY_DATATYPE);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
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
        lenient().when(compiler.getDatatypeHelper(datatype)).thenReturn(datatypeHelper);
        lenient().when(datatypeHelper.getJavaClassName()).thenReturn("MyJavaClass");
        AbstractCompilationResult<JavaCodeFragment> listArgument = mock(CompilationResultImpl.class);
        ListOfTypeDatatype listDatatype = new ListOfTypeDatatype(datatype);
        when(listArgument.getDatatype()).thenReturn(listDatatype);
        CompilationResultImpl arg1Result = new CompilationResultImpl("currentResult", datatype);
        CompilationResultImpl arg2Result = new CompilationResultImpl("nextValue", datatype);
        when(
                compiler.getMatchingFunctionUsingConversion(new CompilationResultImpl[] { arg1Result, arg2Result },
                        new Datatype[] { datatype, datatype }, "myFunction")).thenReturn(delegateResult);
        lenient().when(listArgument.getCodeFragment()).thenReturn(new JavaCodeFragment("argumentCode"));
        return listArgument;
    }
}
