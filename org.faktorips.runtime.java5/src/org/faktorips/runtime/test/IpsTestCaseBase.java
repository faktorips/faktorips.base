/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import org.faktorips.values.Decimal;

/**
 * 
 * @author Joerg Ortmann
 */
public abstract class IpsTestCaseBase extends IpsTest2 {

    public IpsTestCaseBase(String qName) {
        super(qName);
    }

    @Override
    public void run(IpsTestResult result) {
        result.run(this);
    }

    /**
     * Method to execute the business logic.
     */
    public abstract void executeBusinessLogic() throws Exception;

    /**
     * Method to execute several asserts and store the results in the given result object.
     */
    public abstract void executeAsserts(IpsTestResult result) throws Exception;

    /**
     * Asserts that two objects are equal. If they are not equal an ips test failure will be added
     * to the given result object.
     * 
     * @param expectedValue the expected value
     * @param actualValue the value which will be compared with the expected value
     * @param result result object to which the assert result will be added
     */
    protected void assertEquals(Object expectedValue, Object actualValue, IpsTestResult result) {
        assertEquals(expectedValue, actualValue, result, null, null);
    }

    /**
     * Asserts that two objects are equal. If they are not equal an ips test failure will be added
     * to the given result object. The given object and attribute specifies the tested field.
     * 
     * @param expectedValue the expected value
     * @param actualValue the value which will be compared with the expected value
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     */
    protected void assertEquals(Object expectedValue,
            Object actualValue,
            IpsTestResult result,
            String testObject,
            String attribute) {
        assertEquals(expectedValue, actualValue, result, testObject, attribute, null);
    }

    /**
     * Asserts that two objects are equal. If they are not equal an ips test failure will be added
     * to the given result object with the given message. The given object and attribute specifies
     * the tested field.
     * 
     * @param expectedValue the expected value
     * @param actualValue the value which will be compared with the expected value
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     * @param message an optional message test to describe the assert
     */
    protected void assertEquals(Object expectedValue,
            Object actualValue,
            IpsTestResult result,
            String testObject,
            String attribute,
            String message) {
        if (expectedValue == null) {
            if (actualValue == null) {
                return;
            }
        } else if (expectedValue.equals(actualValue)) {
            return;
        }
        fail(expectedValue, actualValue, result, testObject, attribute, message);
    }

    /**
     * Asserts that a condition is true. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param condition the condition which will be checked
     * @param result result object to which the assert result will be added
     */
    protected void assertTrue(boolean condition, IpsTestResult result) {
        assertTrue(condition, result, null, null, null);
    }

    /**
     * Asserts that a condition is true. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param condition the condition which will be checked
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     */
    protected void assertTrue(boolean condition, IpsTestResult result, String testObject, String attribute) {
        assertTrue(condition, result, testObject, attribute, null);
    }

    /**
     * Asserts that a condition is true. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param condition the condition which will be checked
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     * @param message an optional message test to describe the assert
     */
    protected void assertTrue(boolean condition,
            IpsTestResult result,
            String testObject,
            String attribute,
            String message) {
        if (!condition) {
            fail(Boolean.TRUE, Boolean.FALSE, result, testObject, attribute, message);
        }
    }

    /**
     * Asserts that a condition is false. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param condition the condition which will be checked
     * @param result result object to which the assert result will be added
     */
    protected void assertFalse(boolean condition, IpsTestResult result) {
        assertFalse(condition, result, null, null, null);
    }

    /**
     * Asserts that a condition is false. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param condition the condition which will be checked
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     */
    protected void assertFalse(boolean condition, IpsTestResult result, String testObject, String attribute) {
        assertFalse(condition, result, testObject, attribute, null);
    }

    /**
     * Asserts that a condition is false. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param condition the condition which will be checked
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     * @param message an optional message test to describe the assert
     */
    protected void assertFalse(boolean condition,
            IpsTestResult result,
            String testObject,
            String attribute,
            String message) {
        if (condition) {
            fail(Boolean.FALSE, Boolean.TRUE, result, testObject, attribute, message);
        }
    }

    /**
     * Asserts that an object is null. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param object the object which will be checked
     * @param result result object to which the assert result will be added
     */
    protected void assertNull(Object object, IpsTestResult result) {
        assertNull(object, result, null, null, null);
    }

    /**
     * Asserts that an object is null. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param object the object which will be checked
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     */
    protected void assertNull(Object object, IpsTestResult result, String testObject, String attribute) {
        assertNull(object, result, testObject, attribute, null);
    }

    /**
     * Asserts that an object is null. If it isn't an ips test failure will be added to the given
     * result object.
     * 
     * @param object the object which will be checked
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     * @param message an optional message test to describe the assert
     * 
     */
    protected void assertNull(Object object, IpsTestResult result, String testObject, String attribute, String message) {
        if (object != null) {
            fail(null, object, result, testObject, attribute, message);
        }
    }

    /**
     * Asserts that an object is not null. If it isn't an ips test failure will be added to the
     * given result object.
     * 
     * @param object the object which will be checked
     * @param result result object to which the assert result will be added
     */
    protected void assertNotNull(Object object, IpsTestResult result) {
        assertNotNull(object, result, null, null, null);
    }

    /**
     * Asserts that an object is not null. If it isn't an ips test failure will be added to the
     * given result object.
     * 
     * @param object the object which will be checked
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     */
    protected void assertNotNull(Object object, IpsTestResult result, String testObject, String attribute) {
        assertNotNull(object, result, testObject, attribute, null);
    }

    /**
     * Asserts that an object is not null. If it isn't an ips test failure will be added to the
     * given result object.
     * 
     * @param object the object which will be checked
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object which will be checked. If the test contains several instances with the same
     *            name then an index (starting with 0) must be added to the testObject string
     *            separated by "#" (e.g. TestObject#0, TestObject#1, ...). If the test object is a
     *            child object then the complete path to the object must be given, the path elements
     *            must be separated by "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute which will be checked, if the test object doesn't support attributes
     *            then this parameter could be <code>null</code>
     * @param message an optional message test to describe the assert
     */
    protected void assertNotNull(Object object,
            IpsTestResult result,
            String testObject,
            String attribute,
            String message) {
        if (object == null) {
            fail("!" + null, object, result, testObject, attribute, message);
        }
    }

    /**
     * Asserts that an object is equal with ignored scale (e.g. 2.0 and 2.00 are equal with ignored
     * scale). If it isn't an ips test failure will be added to the given result object.
     * 
     * @deprecated Decimal class already ignores the scale in it's equals method.
     */
    @Deprecated
    protected void assertEqualsIgnoreScale(Decimal expectedValue, Decimal actualValue, IpsTestResult result) {
        assertEqualsIgnoreScale(expectedValue, actualValue, result, null, null, null);
    }

    /**
     * Asserts that an object is equal with ignored scale (e.g. 2.0 and 2.00 are equal with ignored
     * scale). If it isn't an ips test failure will be added to the given result object.
     * 
     * @deprecated Decimal class already ignores the scale in it's equals method.
     */
    @Deprecated
    protected void assertEqualsIgnoreScale(Decimal expectedValue,
            Decimal actualValue,
            IpsTestResult result,
            String object,
            String attribute) {
        assertEqualsIgnoreScale(expectedValue, actualValue, result, object, attribute, null);
    }

    /**
     * Asserts that an object is equal with ignored scale (e.g. 2.0 and 2.00 are equal with ignored
     * scale). If it isn't an ips test failure will be added to the given result object.
     * 
     * @deprecated Decimal class already ignores the scale in it's equals method.
     */
    @Deprecated
    protected void assertEqualsIgnoreScale(Decimal expectedValue,
            Decimal actualValue,
            IpsTestResult result,
            String object,
            String attribute,
            String message) {
        if (expectedValue == null) {
            if (actualValue == null) {
                return;
            }
        } else if (expectedValue.equalsIgnoreScale(actualValue)) {
            return;
        }
        fail(expectedValue, actualValue, result, object, attribute, message);
    }

    /**
     * Adds a new failure to the given result.
     * 
     * @param expectedValue the expected value
     * @param actualValue the actuel value
     * @param result result object to which the assert result will be added
     * @param testObject name of the test object in the test case type definition, identifies the
     *            object. If the test contains several instances with the same name then an index
     *            (starting with 0) must be added to the testObject string separated by "#" (e.g.
     *            TestObject#0, TestObject#1, ...). If the test object is a child object then the
     *            complete path to the object must be given, the path elements must be separated by
     *            "." (e.g. RootTestObject#0.ParentObject#0.ChildObject#0)
     * @param attribute name of the test attribute in the test case type definition, identifies the
     *            attribute, if the test object doesn't support attributes then this parameter could
     *            be <code>null</code>
     * @param message an optional message test to describe the failure
     */
    protected void fail(Object expectedValue,
            Object actualValue,
            IpsTestResult result,
            String testObject,
            String attribute,
            String message) {
        IpsTestFailure failure = new IpsTestFailure(this, expectedValue, actualValue, testObject, attribute, message);
        result.addFailure(failure);
    }

}
