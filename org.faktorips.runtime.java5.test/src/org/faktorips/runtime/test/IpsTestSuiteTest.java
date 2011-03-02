/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsTestSuiteTest {

    @Test
    public void testGetTest() {
        IpsTestSuite suite = new IpsTestSuite("suite");
        IpsTestSuite suite2 = new IpsTestSuite("suite.suite2");
        IpsTestCase2 case1 = new MyTestCase("suite.case1");
        IpsFormulaTestCase formulaTest = new MyFormulaTestCase("suite.formulaTest1");
        suite.addTest(suite2);
        suite.addTest(case1);
        suite.addTest(formulaTest);
        assertEquals(suite2, suite.getTest("suite2"));
        assertEquals(case1, suite.getTest("case1"));
    }

    @Test
    public void testRun() {
        IpsTestSuite suite = new IpsTestSuite("suite");
        MyTestCase test1 = new MyTestCase("suite.Test1", "42", "42");
        MyTestCase test2 = new MyTestCase("suite.Test2", "42", "42");
        IpsTestSuite suite2 = new IpsTestSuite("suite.a");
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(suite2);
        MyTestCase test3 = new MyTestCase("suite.a.Test3", "42", "42");
        MyTestCase test4 = new MyTestCase("suite.a.Test4", "42", "42");
        MyFormulaTestCase test5 = new MyFormulaTestCase("suite.a.Test5");
        suite2.addTest(test3);
        suite2.addTest(test4);
        suite2.addTest(test5);

        IpsTestResult result = new IpsTestResult();
        MyListener listener = new MyListener();
        result.addListener(listener);
        result.run(suite);
        assertEquals(5, listener.startedCount);
        assertEquals(5, listener.finishCount);
        assertEquals(0, listener.failureCount);
        assertEquals(test5, listener.lastStarted);
        assertEquals(test5, listener.lastFinished);
        assertNull(listener.lastFailure);

        MyTestCase test6 = new MyTestCase("suite.Test6", "42", "43");
        MyTestCase test7 = new MyTestCase("suite.a.Test7", "42", "43");
        MyFormulaTestCase test8 = new MyFormulaTestCase("suite.a.Test8");
        test8.addDummyFailures(3);
        suite.addTest(test6);
        suite2.addTest(test7);
        suite2.addTest(test8);
        result = new IpsTestResult();
        listener = new MyListener();
        result.addListener(listener);
        result.run(suite);
        assertEquals(8, listener.startedCount);
        assertEquals(8, listener.finishCount);
        assertEquals(5, listener.failureCount);
    }

    @Test
    public void testCountTestCases() {
        IpsTestSuite root = new IpsTestSuite("root");
        MyTestCase test0 = new MyTestCase("suite.Test0", "42", "42");
        root.addTest(test0);

        IpsTestSuite suite = new IpsTestSuite("suite");
        root.addTest(suite);
        MyTestCase test1 = new MyTestCase("suite.Test1", "42", "42");
        MyTestCase test2 = new MyTestCase("suite.Test2", "42", "42");
        IpsTestSuite suite2 = new IpsTestSuite("suite.a");
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(suite2);
        MyTestCase test3 = new MyTestCase("suite.a.Test3", "42", "42");
        MyTestCase test4 = new MyTestCase("suite.a.Test4", "42", "42");
        suite2.addTest(test3);
        suite2.addTest(test4);

        assertEquals(5, root.countTestCases());
    }

}
