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

package org.faktorips.runtime.internal.formula.groovy;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.faktorips.runtime.internal.formula.IFormulaEvaluator;

/**
 * Testing the {@link GroovyEvaluator}
 * 
 * @author dirmeier
 */
public class GroovyEvaluatorTest extends TestCase {

    private int var1, var2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testEvaluate() {
        List<String> expressions = new ArrayList<String>();
        String formulaMethod = "public int add() {" + "return this.var1 + this.var2" + "}";
        expressions.add(formulaMethod);
        formulaMethod = "public String getString(String param) {" + "return param" + "}";
        expressions.add(formulaMethod);
        formulaMethod = "import org.faktorips.runtime.internal.formula.groovy.GroovyEvaluatorTest.OtherClass;"
                + "public String getString() {" + "return new OtherClass().anyMethod()" + "}";
        expressions.add(formulaMethod);
        IFormulaEvaluator evaluator = new GroovyEvaluator(this, expressions);

        var1 = 1;
        var2 = 3;
        int result = (Integer)evaluator.evaluate("add");
        assertEquals(var1 + var2, result);

        var1 = -1;
        var2 = 3;
        result = (Integer)evaluator.evaluate("add");
        assertEquals(var1 + var2, result);

        String testString = "asd";
        assertEquals(testString, evaluator.evaluate("getString", testString));

        testString = null;
        assertEquals(testString, evaluator.evaluate("getString", testString));

        assertEquals("abc", evaluator.evaluate("getString"));
    }

    public static class OtherClass {

        public String anyMethod() {
            return "abc";
        }

    }

}
