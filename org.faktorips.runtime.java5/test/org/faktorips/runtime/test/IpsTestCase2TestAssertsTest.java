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

import org.faktorips.values.Decimal;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Test ips test case 2 assert methods.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestCase2TestAssertsTest {

    /*
     * Dummy test case class to call the assert methods
     */
    private class IpsTestCaseAssert extends IpsTestCase2 {
        public IpsTestCaseAssert() {
            super("IpsTestCaseAssert");
        }

        @Override
        public void executeAsserts(IpsTestResult result) throws Exception {
            // do nothing
        }

        @Override
        public void executeBusinessLogic() throws Exception {
            // do nothing
        }

        @Override
        protected void initExpectedResultFromXml(Element resultEl) {
            // do nothing
        }

        @Override
        protected void initInputFromXml(Element inputEl) {
            // do nothing
        }
    }

    private MyListener listener;
    private IpsTestCaseAssert test;
    private IpsTestResult result;

    @Before
    public void setUp() throws Exception {
        listener = new MyListener();
        test = new IpsTestCaseAssert();
        result = new IpsTestResult();
        result.addListener(listener);
    }

    private final static String OBJECT = "obj";
    private final static String ATTRIBUTE = "attr";
    private final static String MESSAGE = "message";

    private static void assertFailure(IpsTestFailure failure) {
        assertEquals(OBJECT, failure.getTestObject());
        assertEquals(ATTRIBUTE, failure.getTestedAttribute());
        assertEquals(MESSAGE, failure.getMessage());
    }

    private static void assertFailureWithoutMessage(IpsTestFailure failure) {
        assertEquals(OBJECT, failure.getTestObject());
        assertEquals(ATTRIBUTE, failure.getTestedAttribute());
        assertEquals(null, failure.getMessage());
    }

    private static void assertFailureWithoutObjAttrMessage(IpsTestFailure failure) {
        assertEquals(null, failure.getTestObject());
        assertEquals(null, failure.getTestedAttribute());
        assertEquals(null, failure.getMessage());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertEquals(Object, Object,
     * IpsTestResult)'
     */
    /**
     * 
     */
    @Test
    public void testAssertEqualsObjectObjectIpsTestResult() {
        test.assertEquals("1", "1", result);
        assertEquals(0, listener.failureCount);
        test.assertEquals("1", "2", result);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutObjAttrMessage(listener.lastFailure);
        assertEquals("1", listener.lastFailure.getExpectedValue());
        assertEquals("2", listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertEquals(Object, Object,
     * IpsTestResult, String, String)'
     */@Test
    public void testAssertEqualsObjectObjectIpsTestResultStringString() {
        test.assertEquals("1", "1", result, OBJECT, ATTRIBUTE);
        assertEquals(0, listener.failureCount);
        test.assertEquals("1", "2", result, OBJECT, ATTRIBUTE);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutMessage(listener.lastFailure);
        assertEquals("1", listener.lastFailure.getExpectedValue());
        assertEquals("2", listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertEquals(Object, Object,
     * IpsTestResult, String, String, String)'
     */@Test
    public void testAssertEqualsObjectObjectIpsTestResultStringStringString() {
        test.assertEquals("1", "1", result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(0, listener.failureCount);
        test.assertEquals("1", "2", result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(1, listener.failureCount);
        assertFailure(listener.lastFailure);
        assertEquals("1", listener.lastFailure.getExpectedValue());
        assertEquals("2", listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertTrue(boolean, IpsTestResult)'
     */@Test
    public void testAssertTrueBooleanIpsTestResult() {
        test.assertTrue(true, result);
        assertEquals(0, listener.failureCount);
        test.assertTrue(false, result);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutObjAttrMessage(listener.lastFailure);
        assertEquals(Boolean.TRUE, listener.lastFailure.getExpectedValue());
        assertEquals(Boolean.FALSE, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertTrue(boolean, IpsTestResult,
     * String, String)'
     */@Test
    public void testAssertTrueBooleanIpsTestResultStringString() {
        test.assertTrue(true, result, OBJECT, ATTRIBUTE);
        assertEquals(0, listener.failureCount);
        test.assertTrue(false, result, OBJECT, ATTRIBUTE);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutMessage(listener.lastFailure);
        assertEquals(Boolean.TRUE, listener.lastFailure.getExpectedValue());
        assertEquals(Boolean.FALSE, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertTrue(boolean, IpsTestResult,
     * String, String, String)'
     */@Test
    public void testAssertTrueBooleanIpsTestResultStringStringString() {
        test.assertTrue(true, result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(0, listener.failureCount);
        test.assertTrue(false, result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(1, listener.failureCount);
        assertFailure(listener.lastFailure);
        assertEquals(Boolean.TRUE, listener.lastFailure.getExpectedValue());
        assertEquals(Boolean.FALSE, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertFalse(boolean, IpsTestResult)'
     */@Test
    public void testAssertFalseBooleanIpsTestResult() {
        test.assertFalse(false, result);
        assertEquals(0, listener.failureCount);
        test.assertFalse(true, result);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutObjAttrMessage(listener.lastFailure);
        assertEquals(Boolean.FALSE, listener.lastFailure.getExpectedValue());
        assertEquals(Boolean.TRUE, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertFalse(boolean, IpsTestResult,
     * String, String)'
     */@Test
    public void testAssertFalseBooleanIpsTestResultStringString() {
        test.assertFalse(false, result, OBJECT, ATTRIBUTE);
        assertEquals(0, listener.failureCount);
        test.assertFalse(true, result, OBJECT, ATTRIBUTE);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutMessage(listener.lastFailure);
        assertEquals(Boolean.FALSE, listener.lastFailure.getExpectedValue());
        assertEquals(Boolean.TRUE, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertFalse(boolean, IpsTestResult,
     * String, String, String)'
     */@Test
    public void testAssertFalseBooleanIpsTestResultStringStringString() {
        test.assertFalse(false, result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(0, listener.failureCount);
        test.assertFalse(true, result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(1, listener.failureCount);
        assertFailure(listener.lastFailure);
        assertEquals(Boolean.FALSE, listener.lastFailure.getExpectedValue());
        assertEquals(Boolean.TRUE, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertNull(Object, IpsTestResult)'
     */@Test
    public void testAssertNullObjectIpsTestResult() {
        test.assertNull(null, result);
        assertEquals(0, listener.failureCount);
        test.assertNull("notNull", result);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutObjAttrMessage(listener.lastFailure);
        assertEquals(null, listener.lastFailure.getExpectedValue());
        assertEquals("notNull", listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertNull(Object, IpsTestResult,
     * String, String)'
     */@Test
    public void testAssertNullObjectIpsTestResultStringString() {
        test.assertNull(null, result, OBJECT, ATTRIBUTE);
        assertEquals(0, listener.failureCount);
        test.assertNull("notNull", result, OBJECT, ATTRIBUTE);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutMessage(listener.lastFailure);
        assertEquals(null, listener.lastFailure.getExpectedValue());
        assertEquals("notNull", listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertNull(Object, IpsTestResult,
     * String, String, String)'
     */@Test
    public void testAssertNullObjectIpsTestResultStringStringString() {
        test.assertNull(null, result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(0, listener.failureCount);
        test.assertNull("notNull", result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(1, listener.failureCount);
        assertFailure(listener.lastFailure);
        assertEquals(null, listener.lastFailure.getExpectedValue());
        assertEquals("notNull", listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertNotNull(Object,
     * IpsTestResult)'
     */@Test
    public void testAssertNotNullObjectIpsTestResult() {
        test.assertNotNull("notNull", result);
        assertEquals(0, listener.failureCount);
        test.assertNotNull(null, result);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutObjAttrMessage(listener.lastFailure);
        assertEquals("!" + null, listener.lastFailure.getExpectedValue());
        assertEquals(null, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertNotNull(Object, IpsTestResult,
     * String, String)'
     */@Test
    public void testAssertNotNullObjectIpsTestResultStringString() {
        test.assertNotNull("notNull", result, OBJECT, ATTRIBUTE);
        assertEquals(0, listener.failureCount);
        test.assertNotNull(null, result, OBJECT, ATTRIBUTE);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutMessage(listener.lastFailure);
        assertEquals("!" + null, listener.lastFailure.getExpectedValue());
        assertEquals(null, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertNotNull(Object, IpsTestResult,
     * String, String, String)'
     */@Test
    public void testAssertNotNullObjectIpsTestResultStringStringString() {
        test.assertNotNull("notNull", result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(0, listener.failureCount);
        test.assertNotNull(null, result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(1, listener.failureCount);
        assertFailure(listener.lastFailure);
        assertEquals("!" + null, listener.lastFailure.getExpectedValue());
        assertEquals(null, listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertEqualsIgnoreScale(Decimal,
     * Decimal, IpsTestResult)'
     */@Test
    public void testAssertEqualsIgnoreScaleDecimalDecimalIpsTestResult() {
        test.assertEquals(Decimal.valueOf("2.00"), Decimal.valueOf("2.0"), result);
        assertEquals(0, listener.failureCount);
        test.assertEquals(Decimal.valueOf("2.00"), Decimal.valueOf("2.01"), result);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutObjAttrMessage(listener.lastFailure);
        assertEquals(Decimal.valueOf("2.00"), listener.lastFailure.getExpectedValue());
        assertEquals(Decimal.valueOf("2.01"), listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertEqualsIgnoreScale(Decimal,
     * Decimal, IpsTestResult, String, String)'
     */@Test
    public void testAssertEqualsIgnoreScaleDecimalDecimalIpsTestResultStringString() {
        test.assertEquals(Decimal.valueOf("2.00"), Decimal.valueOf("2.0"), result, OBJECT, ATTRIBUTE);
        assertEquals(0, listener.failureCount);
        test.assertEquals(Decimal.valueOf("2.00"), Decimal.valueOf("2.01"), result, OBJECT, ATTRIBUTE);
        assertEquals(1, listener.failureCount);
        assertFailureWithoutMessage(listener.lastFailure);
        assertEquals(Decimal.valueOf("2.00"), listener.lastFailure.getExpectedValue());
        assertEquals(Decimal.valueOf("2.01"), listener.lastFailure.getActualValue());
    }

    /*
     * Test method for 'org.faktorips.runtime.test.IpsTestCase2.assertEqualsIgnoreScale(Decimal,
     * Decimal, IpsTestResult, String, String, String)'
     */@Test
    public void testAssertEqualsIgnoreScaleDecimalDecimalIpsTestResultStringStringString() {
        test.assertEquals(Decimal.valueOf("2.00"), Decimal.valueOf("2.0"), result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(0, listener.failureCount);
        test.assertEquals(Decimal.valueOf("2.00"), Decimal.valueOf("2.01"), result, OBJECT, ATTRIBUTE, MESSAGE);
        assertEquals(1, listener.failureCount);
        assertFailure(listener.lastFailure);
        assertEquals(Decimal.valueOf("2.00"), listener.lastFailure.getExpectedValue());
        assertEquals(Decimal.valueOf("2.01"), listener.lastFailure.getActualValue());
    }

}
