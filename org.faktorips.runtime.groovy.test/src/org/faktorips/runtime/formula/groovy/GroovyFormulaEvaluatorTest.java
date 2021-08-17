/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula.groovy;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing the {@link GroovyFormulaEvaluator}
 *
 * @author dirmeier
 */
public class GroovyFormulaEvaluatorTest {

    private MyCmpt testCmpt;
    private MyCmptGeneration testGen;
    private Map<String, String> expressions;

    @Before
    public void setUp() {
        expressions = new LinkedHashMap<>();
        String formulaMethod = "public int add() {" + "return this.var1 + this.var2" + "}";
        expressions.put("add", formulaMethod);
        formulaMethod = "public String getString(String param) {" + "return param" + "}";
        expressions.put("getStringWithParam", formulaMethod);
        formulaMethod = "import org.faktorips.runtime.formula.groovy.GroovyFormulaEvaluatorTest.OtherClass;"
                + "public String getString() {" + "return new OtherClass().anyMethod()" + "}";
        expressions.put("getString", formulaMethod);
        testCmpt = new MyCmpt(new InMemoryRuntimeRepository(), "Test 2010-01", "Test", "2010-01");
        testGen = new MyCmptGeneration(testCmpt);
    }

    @Test
    public void testEvaluate_generation() {
        IFormulaEvaluator evaluator = new GroovyFormulaEvaluator(testGen, expressions);

        testGen.var1 = 1;
        testGen.var2 = 3;
        int result = (Integer)evaluator.evaluate("add");
        assertEquals(testGen.var1 + testGen.var2, result);

        testGen.var1 = -1;
        testGen.var2 = 3;
        result = (Integer)evaluator.evaluate("add");
        assertEquals(testGen.var1 + testGen.var2, result);

        assertGetString(evaluator);
    }

    @Test
    public void testEvaluate_ProductCmpt() {
        IFormulaEvaluator evaluator = new GroovyFormulaEvaluator(testCmpt, expressions);

        testCmpt.var1 = 1;
        testCmpt.var2 = 3;
        int result = (Integer)evaluator.evaluate("add");
        assertEquals(testCmpt.var1 + testCmpt.var2, result);

        testCmpt.var1 = -1;
        testCmpt.var2 = 3;
        result = (Integer)evaluator.evaluate("add");
        assertEquals(testCmpt.var1 + testCmpt.var2, result);

        assertGetString(evaluator);
    }

    private void assertGetString(IFormulaEvaluator evaluator) {
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

    private class MyCmptGeneration extends ProductComponentGeneration {

        private int var1;
        private int var2;

        public MyCmptGeneration(ProductComponent productCmpt) {
            super(productCmpt);
        }

    }

    private class MyCmpt extends ProductComponent {

        private int var1;
        private int var2;

        public MyCmpt(IRuntimeRepository repository, String id, String productKindId, String versionId) {
            super(repository, id, productKindId, versionId);
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }

    }

}
