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
import org.faktorips.values.Money;

/**
 *
 */
public class AddMoneyMoneyTest extends CompilerAbstractTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        compiler.setBinaryOperations(new BinaryOperation[] { new AddMoneyMoney() });
    }

    public void test() throws Exception {
        execAndTestSuccessfull("3.50EUR + 7.45EUR", Money.valueOf("10.95EUR"), Datatype.MONEY);
    }

    public void testLhsError() throws Exception {
        execAndTestFail("a a + 8.30EUR", ExprCompiler.SYNTAX_ERROR);
    }

    public void testRhsError() throws Exception {
        execAndTestFail("8.10EUR + a a", ExprCompiler.SYNTAX_ERROR);
    }

}
