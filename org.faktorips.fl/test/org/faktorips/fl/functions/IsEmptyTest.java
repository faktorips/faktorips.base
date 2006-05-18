/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.fl.functions;


/**
 * 
 * @author Jan Ortmann
 */
public class IsEmptyTest extends FunctionAbstractTest {

    protected void setUp() throws Exception {
        super.setUp();
        compiler.setEnsureResultIsObject(false);
        registerFunction(new IsEmpty("ISEMPTY", ""));
    }
    
    public void testDecimal() throws Exception {
        execAndTestSuccessfull("ISEMPTY(1.0)", false);
        registerFunction(new DecimalNullFct());
        execAndTestSuccessfull("ISEMPTY(DECIMALNULL())", true);
    }
    
    public void testMoney() throws Exception {
        execAndTestSuccessfull("ISEMPTY(10EUR)", false);
        registerFunction(new MoneyNullFct());
        execAndTestSuccessfull("ISEMPTY(MONEYNULL())", true);
    }

    public void testString() throws Exception {
        execAndTestSuccessfull("ISEMPTY(\"a\")", false);
    }

    public void testInt() throws Exception {
        execAndTestSuccessfull("ISEMPTY(1)", false);
    }
    
    public void testBoolean() throws Exception {
        registerFunction(new BooleanFct("TRUEOBJ", Boolean.TRUE));
        registerFunction(new BooleanFct("BOOLEANNULL", null));
        execAndTestSuccessfull("ISEMPTY(TRUEOBJ())", false);
        execAndTestSuccessfull("ISEMPTY(BOOLEANNULL())", true);
    }

    public void testPrimitiveBoolean() throws Exception {
        execAndTestSuccessfull("ISEMPTY(true)", false);
    }
    
}
