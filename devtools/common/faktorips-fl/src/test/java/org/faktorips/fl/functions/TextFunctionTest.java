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

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TextFunctionTest extends FunctionAbstractTest {

    @Mock
    private CompilationResultImpl argumentCompilationResult;

    @Mock
    private DatatypeHelper helper;

    private TextFunction textFunc = new TextFunction("TEXT", "");

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(textFunc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompile_NumberOfArgumentsMany() {
        textFunc.compile(new CompilationResultImpl[2]);
    }

    @Test
    public void testCompile_DatatypeHelper() throws Exception {
        execAndTestSuccessfull("TEXT(3)", "3", Datatype.STRING);
        execAndTestSuccessfull("TEXT(\"test\")", "test", Datatype.STRING);
        execAndTestSuccessfull("TEXT(true)", "true", Datatype.STRING);
    }
}
