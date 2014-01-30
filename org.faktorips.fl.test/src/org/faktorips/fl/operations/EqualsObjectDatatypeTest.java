/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.operations;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.fl.JavaExprCompilerAbstractTest;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class EqualsObjectDatatypeTest extends JavaExprCompilerAbstractTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(toArray(new EqualsObjectDatatype(AnyDatatype.INSTANCE)));
        compiler.setIdentifierResolver(new IdentifierResolver<JavaCodeFragment>() {

            public CompilationResult<JavaCodeFragment> compile(String identifier,
                    ExprCompiler<JavaCodeFragment> exprCompiler,
                    Locale locale) {
                if (identifier.equals("beTrue")) {
                    return new CompilationResultImpl("Boolean.TRUE", Datatype.BOOLEAN);
                } else if (identifier.equals("beFalse")) {
                    return new CompilationResultImpl("Boolean.FALSE", Datatype.BOOLEAN);
                } else {
                    return new CompilationResultImpl("null", Datatype.BOOLEAN);
                }
            }

        });
    }

    @Test
    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("beTrue=beTrue", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("beFalse=beFalse", Boolean.TRUE, Datatype.BOOLEAN);
    }

    @Test
    public void testDecimal() throws Exception {
        compiler.setBinaryOperations(toArray(new EqualsObjectDatatype(Datatype.DECIMAL, Datatype.DECIMAL)));
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1=2", false);
        execAndTestSuccessfull("1.0=1.0", true);
    }

    @Test
    public void testMoney() throws Exception {
        compiler.setBinaryOperations(toArray(new EqualsObjectDatatype(Datatype.MONEY, Datatype.MONEY)));
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1EUR=2EUR", false);
        execAndTestSuccessfull("1.23EUR=1.23EUR", true);
    }

    @Test
    public void testString() throws Exception {
        compiler.setBinaryOperations(toArray(new EqualsObjectDatatype(Datatype.STRING, Datatype.STRING)));
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("\"abc\" = \"cde\"", false);
        execAndTestSuccessfull("\"abc\" = \"abc\"", true);
    }

}
