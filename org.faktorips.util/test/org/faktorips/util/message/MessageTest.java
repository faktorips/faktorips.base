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

package org.faktorips.util.message;

import junit.framework.TestCase;

import org.apache.commons.lang.SystemUtils;

public class MessageTest extends TestCase {

    public void testCopy() {
        Message msg = new Message("code", "text", Message.INFO);
        Message copy = Message.createCopy(msg, "a", "b");
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        assertEquals(0, msg.getInvalidObjectProperties().length);

        Object oldObject = new Object();
        ObjectProperty op0 = new ObjectProperty(oldObject, "prop0");
        ObjectProperty op1 = new ObjectProperty(this, "prop1");
        ObjectProperty op2 = new ObjectProperty(oldObject, "prop2");
        msg = new Message("code", "text", Message.ERROR, new ObjectProperty[] { op0, op1, op2 });
        Object newObject = new Object();
        copy = Message.createCopy(msg, oldObject, newObject);
        ObjectProperty[] ops = copy.getInvalidObjectProperties();
        assertEquals(3, ops.length);
        assertEquals(newObject, ops[0].getObject());
        assertEquals("prop0", ops[0].getProperty());
        assertEquals(this, ops[1].getObject());
        assertEquals("prop1", ops[1].getProperty());
        assertEquals(newObject, ops[2].getObject());
        assertEquals("prop2", ops[2].getProperty());
    }

    public void testMessage_StringStringint() {
        Message msg = new Message("code", "text", Message.INFO);
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        assertEquals(0, msg.getInvalidObjectProperties().length);
    }

    public void testMessage_StringStringintObjectString() {
        Message msg = new Message("code", "text", Message.INFO, this, "property");
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        ObjectProperty[] op = msg.getInvalidObjectProperties();
        assertEquals(1, op.length);
        assertEquals(this, op[0].getObject());
        assertEquals("property", op[0].getProperty());
    }

    public void testMessage_StringStringintObjectStringArray() {
        Message msg = new Message("code", "text", Message.INFO, this, new String[] { "p1", "p2" });
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        ObjectProperty[] op = msg.getInvalidObjectProperties();
        assertEquals(2, op.length);
        assertEquals(this, op[0].getObject());
        assertEquals("p1", op[0].getProperty());
        assertEquals(this, op[1].getObject());
        assertEquals("p2", op[1].getProperty());
    }

    public void testMessage_StringStringintObjectPropertyArray() {
        ObjectProperty[] op = new ObjectProperty[] { new ObjectProperty("objectA", "pA"),
                new ObjectProperty("objectB", "pB") };
        Message msg = new Message("code", "text", Message.INFO, op);
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());

        ObjectProperty[] op2 = msg.getInvalidObjectProperties();
        op[0] = null; // make sure a defensive copy was made
        assertEquals(2, op2.length);
        assertEquals("objectA", op2[0].getObject());
        assertEquals("pA", op2[0].getProperty());
        assertEquals("objectB", op2[1].getObject());
        assertEquals("pB", op2[1].getProperty());
    }

    /*
     * Class under test for String toString()
     */
    public void testToString() {
        Message msg = Message.newError("1", "blabla");
        String expected = "ERROR 1[]" + SystemUtils.LINE_SEPARATOR + "blabla";
        assertEquals(expected, msg.toString());

        msg = new Message("code", "blabla", Message.INFO, "ObjectA", new String[] { "p1", "p2" });
        expected = "INFO code[ObjectA.p1, ObjectA.p2]" + SystemUtils.LINE_SEPARATOR + "blabla";
        assertEquals(expected, msg.toString());
    }

    /*
     * Class under test for boolean equals(Object)
     */
    public void testEqualsObject() {
        // differnet class
        Message msg = Message.newError("1", "blabla");
        assertFalse(msg.equals(this));

        // diferent code
        Message msg2 = Message.newError("2", "blabla");
        assertFalse(msg.equals(msg2));

        // different text
        msg2 = Message.newError("1", "bla");
        assertFalse(msg.equals(msg2));

        // different severity
        msg2 = Message.newWarning("1", "blabla");
        assertFalse(msg.equals(msg2));

        // different referenced object properties (different number of properties)
        msg2 = new Message("1", "blabla", Message.ERROR, "object", "property");
        assertFalse(msg.equals(msg2));

        // no differences (no referenced object properties)
        msg2 = new Message("1", "blabla", Message.ERROR);
        assertTrue(msg.equals(msg2));

        // different referenced object properties (different object)
        ObjectProperty[] op = new ObjectProperty[] { new ObjectProperty("objectA", "pA"),
                new ObjectProperty("objectB", "pB") };
        msg = new Message("1", "blabla", Message.ERROR, op);
        msg2 = new Message("1", "blabla", Message.ERROR, "objectA", new String[] { "pA", "pB" });
        assertFalse(msg.equals(msg2));

        // different referenced object properties (different property)
        ObjectProperty[] op2 = new ObjectProperty[] { new ObjectProperty("objectA", "pA"),
                new ObjectProperty("objectB", "pDifferent") };
        msg2 = new Message("1", "blabla", Message.ERROR, op2);
        assertFalse(msg.equals(msg2));

        // no differences (with referenced object properties)
        msg2 = new Message("1", "blabla", Message.ERROR, op);
        assertTrue(msg.equals(msg2));

    }

}
