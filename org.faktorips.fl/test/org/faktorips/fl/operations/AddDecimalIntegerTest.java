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

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.fl.CompilerAbstractTest;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.values.Decimal;

/**
 *
 */
public class AddDecimalIntegerTest extends CompilerAbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[] { new AddDecimalInteger() });
    }

    public void testSuccessfull() throws Exception {
        execAndTestSuccessfull("3.5 + 4", Decimal.valueOf("7.5"), Datatype.DECIMAL);
    }

    public void testLhsError() throws Exception {
        execAndTestFail("a a + 8", ExprCompiler.SYNTAX_ERROR);
    }

    public void testRhsError() throws Exception {
        execAndTestFail("8.1 + a a", ExprCompiler.SYNTAX_ERROR);
    }

}
