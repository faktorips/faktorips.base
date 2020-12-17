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

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResultImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class NotTest extends FunctionAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        getCompiler().setEnsureResultIsObject(false);
        registerFunction(new Not("NOT", ""));
        registerFunction(new NotBoolean("NOT", ""));
        getCompiler().setIdentifierResolver((identifier, exprCompiler, locale) -> {
            if (identifier.equals("beTrue")) {
                return new CompilationResultImpl("Boolean.TRUE", Datatype.BOOLEAN);
            } else if (identifier.equals("beFalse")) {
                return new CompilationResultImpl("Boolean.FALSE", Datatype.BOOLEAN);
            } else {
                return new CompilationResultImpl("null", Datatype.BOOLEAN);
            }
        });
    }

    @Test
    public void testPrimitiveBoolean() throws Exception {
        execAndTestSuccessfull("NOT(FALSE)", true);
        execAndTestSuccessfull("NOT(TRUE)", false);
    }

    @Test
    public void testBoolean() throws Exception {
        execAndTestSuccessfull("NOT( beTrue )", Boolean.FALSE, Datatype.BOOLEAN);
        execAndTestSuccessfull("NOT( beFalse )", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("NOT( beNull )", null, Datatype.BOOLEAN);
    }

}
