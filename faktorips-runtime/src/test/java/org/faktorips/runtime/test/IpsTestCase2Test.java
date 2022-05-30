/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsTestCase2Test {

    @Test
    public void testRun() {
        IpsTestResult result = new IpsTestResult();
        MyTestCase test = new MyTestCase("MyTest", "42", "42");
        MyListener listener = new MyListener();
        result.addListener(listener);
        result.run(test);
        assertEquals(1, listener.startedCount);
        assertEquals(1, listener.finishCount);
        assertEquals(0, listener.failureCount);
        assertEquals(test, listener.lastStarted);
        assertEquals(test, listener.lastFinished);
        assertNull(listener.lastFailure);

        result = new IpsTestResult();
        test = new MyTestCase("MyTest", "42", "43");
        listener = new MyListener();
        result.addListener(listener);
        result.run(test);
        assertEquals(1, listener.failureCount);
        IpsTestFailure failure = listener.lastFailure;
        assertEquals(test, failure.getTestCase());
        assertEquals("42", failure.getExpectedValue());
        assertEquals("43", failure.getActualValue());
        assertEquals("TestObject", failure.getTestObject());
        assertEquals("TestedAttribute", failure.getTestedAttribute());
    }

    @Test
    public void testGetExtensionAttribute() {
        MyTestCase test = new MyTestCase("MyTest", "42", "42");
        MyModelObject modelObj1 = new MyModelObject();
        MyModelObject modelObj2 = new MyModelObject();
        MyModelObject modelObj3 = new MyModelObject();

        test.addExtensionAttribute(modelObj1, "testAttribute1", "A");
        test.addExtensionAttribute(modelObj1, "testAttribute2", "B");
        test.addExtensionAttribute(modelObj2, "testAttribute3", "C");

        assertNotNull(test.getExtensionAttributeValue(modelObj1, "testAttribute1"));
        assertNotNull(test.getExtensionAttributeValue(modelObj1, "testAttribute2"));
        assertNotNull(test.getExtensionAttributeValue(modelObj2, "testAttribute3"));
        assertNull(test.getExtensionAttributeValue(modelObj1, "testAttribute3"));
        assertNull(test.getExtensionAttributeValue(modelObj3, "testAttribute3"));

        assertEquals("A", test.getExtensionAttributeValue(modelObj1, "testAttribute1"));
        assertEquals("B", test.getExtensionAttributeValue(modelObj1, "testAttribute2"));
        assertEquals("C", test.getExtensionAttributeValue(modelObj2, "testAttribute3"));
    }

    private class MyModelObject implements IModelObject {

        @Override
        public MessageList validate(IValidationContext context) {
            throw new RuntimeException();
        }

    }

}
