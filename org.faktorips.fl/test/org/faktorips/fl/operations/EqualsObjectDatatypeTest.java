/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.fl.operations;

import java.util.Locale;

import org.faktorips.datatype.AnyDatatype;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.CompilerAbstractTest;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;

/**
 * 
 * @author Jan Ortmann
 */
public class EqualsObjectDatatypeTest extends CompilerAbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[] { new EqualsObjectDatatype(AnyDatatype.INSTANCE) });
        compiler.setIdentifierResolver(new IdentifierResolver() {

            public CompilationResult compile(String identifier, ExprCompiler exprCompiler, Locale locale) {
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

    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("beTrue=beTrue", Boolean.TRUE, Datatype.BOOLEAN);
        execAndTestSuccessfull("beFalse=beFalse", Boolean.TRUE, Datatype.BOOLEAN);
    }

    public void testDecimal() throws Exception {
        compiler.setBinaryOperations(new BinaryOperation[] { new EqualsObjectDatatype(Datatype.DECIMAL,
                Datatype.DECIMAL) });
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1=2", false);
        execAndTestSuccessfull("1.0=1.0", true);
    }

    public void testMoney() throws Exception {
        compiler
                .setBinaryOperations(new BinaryOperation[] { new EqualsObjectDatatype(Datatype.MONEY, Datatype.MONEY) });
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("1EUR=2EUR", false);
        execAndTestSuccessfull("1.23EUR=1.23EUR", true);
    }

    public void testString() throws Exception {
        compiler
                .setBinaryOperations(new BinaryOperation[] { new EqualsObjectDatatype(Datatype.STRING, Datatype.STRING) });
        compiler.setEnsureResultIsObject(false);
        execAndTestSuccessfull("\"abc\" = \"cde\"", false);
        execAndTestSuccessfull("\"abc\" = \"abc\"", true);
    }

}
