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

package org.faktorips.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.faktorips.values.Money;
import org.junit.Test;

public class MessageTest extends XmlAbstractTestCase {

    private final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Test
    public void testCreateCopy() {
        Message msg = new Message("code", "text", Message.INFO);
        Message copy = Message.createCopy(msg, "a", "b");
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        assertEquals(0, msg.getInvalidObjectProperties().size());
        assertEquals(0, msg.getReplacementParameters().size());

        Object oldObject = new Object();
        ObjectProperty op0 = new ObjectProperty(oldObject, "prop0");
        ObjectProperty op1 = new ObjectProperty(this, "prop1");
        ObjectProperty op2 = new ObjectProperty(oldObject, "prop2");
        MsgReplacementParameter mp0 = new MsgReplacementParameter("0", "v0");
        MsgReplacementParameter mp1 = new MsgReplacementParameter("1", "v1");
        msg = new Message("code", "text", Message.ERROR, new ObjectProperty[] { op0, op1, op2 },
                new MsgReplacementParameter[] { mp0, mp1 });
        Object newObject = new Object();
        copy = Message.createCopy(msg, oldObject, newObject);
        List<ObjectProperty> ops = copy.getInvalidObjectProperties();
        assertEquals(3, ops.size());
        assertEquals(newObject, ops.get(0).getObject());
        assertEquals("prop0", ops.get(0).getProperty());
        assertEquals(this, ops.get(1).getObject());
        assertEquals("prop1", ops.get(1).getProperty());
        assertEquals(newObject, ops.get(2).getObject());
        assertEquals("prop2", ops.get(2).getProperty());
        assertEquals(msg.getReplacementParameters().size(), copy.getReplacementParameters().size());
        List<MsgReplacementParameter> mpsCpy = copy.getReplacementParameters();
        List<MsgReplacementParameter> mps = msg.getReplacementParameters();
        for (int i = 0; i < mps.size(); i++) {
            assertEquals(mps.get(i).getName(), mpsCpy.get(i).getName());
            assertEquals(mps.get(i).getValue(), mpsCpy.get(i).getValue());
        }
    }

    @Test
    public void testCopyConstructor() {
        ObjectProperty op0 = new ObjectProperty("0", "prop0");
        ObjectProperty op1 = new ObjectProperty("1", "prop1");
        MsgReplacementParameter mp0 = new MsgReplacementParameter("0", "v0");
        MsgReplacementParameter mp1 = new MsgReplacementParameter("1", "v1");
        Message msg = new Message("code", "text", Message.INFO, new ObjectProperty[] { op0, op1 },
                new MsgReplacementParameter[] { mp0, mp1 });

        Message msgCpy = new Message(msg);
        assertEquals(msg.getCode(), msgCpy.getCode());
        assertEquals(msg.getSeverity(), msgCpy.getSeverity());
        assertEquals(msg.getText(), msgCpy.getText());
        List<ObjectProperty> opsCpy = msgCpy.getInvalidObjectProperties();
        List<MsgReplacementParameter> mpsCpy = msgCpy.getReplacementParameters();
        List<ObjectProperty> ops = msg.getInvalidObjectProperties();
        List<MsgReplacementParameter> mps = msg.getReplacementParameters();
        assertEquals(ops.size(), opsCpy.size());
        assertEquals(mps.size(), mpsCpy.size());
        for (int i = 0; i < mps.size(); i++) {
            assertEquals(mps.get(i).getName(), mpsCpy.get(i).getName());
            assertEquals(mps.get(i).getValue(), mpsCpy.get(i).getValue());
        }
        for (int i = 0; i < ops.size(); i++) {
            assertEquals(ops.get(i).getIndex(), opsCpy.get(i).getIndex());
            assertEquals(ops.get(i).getObject(), opsCpy.get(i).getObject());
            assertEquals(ops.get(i).getProperty(), opsCpy.get(i).getProperty());
        }
    }

    @Test
    public void testMessage_StringStringint() {
        Message msg = new Message("code", "text", Message.INFO);
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        assertEquals(0, msg.getInvalidObjectProperties().size());
        assertEquals(0, msg.getReplacementParameters().size());
    }

    @Test
    public void testMessage_StringStringintObjectString() {
        Message msg = new Message("code", "text", Message.INFO, this, "property");
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        List<ObjectProperty> op = msg.getInvalidObjectProperties();
        assertEquals(1, op.size());
        assertEquals(this, op.get(0).getObject());
        assertEquals("property", op.get(0).getProperty());
    }

    @Test
    public void testMessage_StringStringintObjectStringArray() {
        Message msg = new Message("code", "text", Message.INFO, this, new String[] { "p1", "p2" });
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        List<ObjectProperty> op = msg.getInvalidObjectProperties();
        assertEquals(2, op.size());
        assertEquals(this, op.get(0).getObject());
        assertEquals("p1", op.get(0).getProperty());
        assertEquals(this, op.get(1).getObject());
        assertEquals("p2", op.get(1).getProperty());
    }

    @Test
    public void testMessage_StringStringintObjectPropertyArray() {
        ObjectProperty[] op = new ObjectProperty[] { new ObjectProperty("objectA", "pA"),
                new ObjectProperty("objectB", "pB") };
        Message msg = new Message("code", "text", Message.INFO, op);
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());

        List<ObjectProperty> op2 = msg.getInvalidObjectProperties();
        op[0] = null; // make sure a defensive copy was made
        assertEquals(2, op2.size());
        assertEquals("objectA", op2.get(0).getObject());
        assertEquals("pA", op2.get(0).getProperty());
        assertEquals("objectB", op2.get(1).getObject());
        assertEquals("pB", op2.get(1).getProperty());
    }

    @Test
    public void testMessage_Message() {
        Message msg = new Message("code", "text", Message.ERROR);
        Message copy = new Message(msg);
        assertEquals(0, copy.getInvalidObjectProperties().size());
        assertEquals(0, copy.getReplacementParameters().size());

        List<MsgReplacementParameter> params = Arrays.asList(
                new MsgReplacementParameter("sumInsured", Money.euro(100)), new MsgReplacementParameter("minAge",
                        new Integer(18)));
        msg = new Message("code", "text", Message.ERROR, (ObjectProperty)null, params);
        copy = new Message(msg);
        List<MsgReplacementParameter> copyParams = copy.getReplacementParameters();
        assertEquals(2, copyParams.size());
        assertEquals(params.get(0), copyParams.get(0));
        assertEquals(params.get(1), copyParams.get(1));
    }

    @Test
    public void testToString() {
        Message msg = Message.newError("1", "blabla");
        String expected = "ERROR 1[]" + LINE_SEPARATOR + "blabla";
        assertEquals(expected, msg.toString());

        msg = new Message("code", "blabla", Message.INFO, "ObjectA", new String[] { "p1", "p2" });
        expected = "INFO code[ObjectA.p1, ObjectA.p2]" + LINE_SEPARATOR + "blabla";
        assertEquals(expected, msg.toString());
    }

    @Test
    public void testEqualsObject() {
        // different class
        Message msg = Message.newError("1", "blabla");
        assertFalse(msg.equals(this));

        // different code
        Message msg2 = Message.newError("2", "blabla");
        assertFalse(msg.equals(msg2));

        // different code, one code is null
        msg2 = Message.newError(null, "blabla");
        assertFalse(msg.equals(msg2));
        assertFalse(msg2.equals(msg));

        // different text
        msg2 = Message.newError("1", "bla");
        assertFalse(msg.equals(msg2));

        // different text, one text is null
        msg2 = Message.newError("1", null);
        assertFalse(msg.equals(msg2));
        assertFalse(msg2.equals(msg));

        // different severity
        msg2 = Message.newWarning("1", "blabla");
        assertFalse(msg.equals(msg2));

        // different referenced object properties (different number of properties)
        msg2 = new Message("1", "blabla", Message.ERROR, "object", "property");
        assertFalse(msg.equals(msg2));

        // no differences (no referenced object properties)
        msg2 = new Message("1", "blabla", Message.ERROR);
        assertTrue(msg.equals(msg2));

        // no difference, code is null
        msg = Message.newError(null, "bla");
        msg2 = Message.newError(null, "bla");
        assertTrue(msg.equals(msg2));

        // no difference, text is null
        msg = Message.newError("1", null);
        msg2 = Message.newError("1", null);
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

    @Test
    public void testGetNumOfInvalidObjectProperties() {
        Message msg = new Message("code", "text", Message.ERROR);
        assertEquals(0, msg.getNumOfInvalidObjectProperties());

        ObjectProperty[] props = new ObjectProperty[] { new ObjectProperty(this, "prop1"),
                new ObjectProperty(this, "prop2"), };
        msg = new Message("code", "text", Message.ERROR, props);

        assertEquals(2, msg.getNumOfInvalidObjectProperties());
    }

    @Test
    public void testGetNumOfReplacementParameters() {
        Message msg = new Message("code", "text", Message.ERROR);
        assertEquals(0, msg.getNumOfReplacementParameters());

        MsgReplacementParameter[] params = new MsgReplacementParameter[] {
                new MsgReplacementParameter("sumInsured", Money.euro(100)),
                new MsgReplacementParameter("minAge", new Integer(18)) };
        msg = new Message("code", "text", Message.ERROR, (ObjectProperty)null, params);

        assertEquals(2, msg.getNumOfReplacementParameters());
    }

    @Test
    public void testHasReplacementParameter() {
        Message msg = new Message("code", "text", Message.ERROR);
        assertFalse(msg.hasReplacementParameter("param"));

        MsgReplacementParameter[] params = new MsgReplacementParameter[] {
                new MsgReplacementParameter("sumInsured", Money.euro(100)),
                new MsgReplacementParameter("minAge", new Integer(18)) };
        msg = new Message("code", "text", Message.ERROR, (ObjectProperty)null, params);
        assertFalse(msg.hasReplacementParameter("param"));
        assertTrue(msg.hasReplacementParameter("sumInsured"));
        assertTrue(msg.hasReplacementParameter("minAge"));
    }

    @Test
    public void testGetReplacementValue() {
        Message msg = new Message("code", "text", Message.ERROR);
        assertNull(msg.getReplacementValue("param"));

        MsgReplacementParameter[] params = new MsgReplacementParameter[] {
                new MsgReplacementParameter("sumInsured", Money.euro(100)),
                new MsgReplacementParameter("minAge", new Integer(18)) };
        msg = new Message("code", "text", Message.ERROR, (ObjectProperty)null, params);
        assertNull(msg.getReplacementValue("param"));
        assertEquals(Money.euro(100), msg.getReplacementValue("sumInsured"));
        assertEquals(new Integer(18), msg.getReplacementValue("minAge"));
    }

}
