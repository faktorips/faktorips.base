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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockitoSession;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

public class TextFunctionTest extends FunctionAbstractTest {


    @Mock
    private CompilationResultImpl argumentCompilationResult;

    @Mock
    private DatatypeHelper helper;

    private MockitoSession mockito;

    private TextFunction textFunc = new TextFunction("TEXT", "");

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        mockito = mockitoSession()
                .initMocks(this)
                .strictness(Strictness.STRICT_STUBS)
                .startMocking();
        super.setUp();
        registerFunction(textFunc);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testCompile_NumberOfArgumentsMany() {
        assertThrows(IllegalArgumentException.class, () -> textFunc.compile(new CompilationResultImpl[2]));
    }

    @Test
    public void testCompile_DatatypeHelper() throws Exception {
        execAndTestSuccessfull("TEXT(3)", "3", Datatype.STRING);
        execAndTestSuccessfull("TEXT(\"test\")", "test", Datatype.STRING);
        execAndTestSuccessfull("TEXT(true)", "true", Datatype.STRING);
    }
}
