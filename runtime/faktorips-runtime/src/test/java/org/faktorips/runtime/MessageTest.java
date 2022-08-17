/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.runtime.Message.Builder;
import org.faktorips.values.Money;
import org.junit.Test;

public class MessageTest extends XmlAbstractTestCase {

    private final String LINE_SEPARATOR = System.lineSeparator();

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
    public void testCopy_Map() {
        HashMap<ObjectProperty, ObjectProperty> invalidObjectPropertyMap = new HashMap<>();
        invalidObjectPropertyMap.put(new ObjectProperty("a", "a"), new ObjectProperty("b", "b"));

        Message msg = new Message("code", "text", Message.INFO);
        Message copy = Message.createCopy(msg, invalidObjectPropertyMap);
        assertEquals("code", msg.getCode());
        assertEquals("text", msg.getText());
        assertEquals(Message.INFO, msg.getSeverity());
        assertEquals(0, msg.getInvalidObjectProperties().size());

        Object oldObject = new Object();
        ObjectProperty op0 = new ObjectProperty(oldObject, "prop0");
        ObjectProperty op1 = new ObjectProperty(this, "prop1");
        ObjectProperty op2 = new ObjectProperty(oldObject, "prop2");
        msg = new Message("code", "text", Message.ERROR, op0, op1, op2);
        Object newObject = new Object();

        HashMap<ObjectProperty, ObjectProperty> objectPropertyMap = new HashMap<>();
        objectPropertyMap.put(op0, new ObjectProperty(newObject, "prop0"));
        objectPropertyMap.put(op2, new ObjectProperty(newObject, "prop2"));

        copy = Message.createCopy(msg, objectPropertyMap);
        List<ObjectProperty> ops = copy.getInvalidObjectProperties();
        assertEquals(3, ops.size());
        assertEquals(newObject, ops.get(0).getObject());
        assertEquals("prop0", ops.get(0).getProperty());
        assertEquals(this, ops.get(1).getObject());
        assertEquals("prop1", ops.get(1).getProperty());
        assertEquals(newObject, ops.get(2).getObject());
        assertEquals("prop2", ops.get(2).getProperty());
    }

    @Test
    public void testCreateCopyWithMarkers() {

        Set<IMarker> markers = new HashSet<>();
        markers.add(mock(IMarker.class));
        Message msg = new Message("code", "text", Message.INFO, Collections.<ObjectProperty> emptyList(),
                Collections.<MsgReplacementParameter> emptyList(), markers);
        Message copy = Message.createCopy(msg, "a", "b");

        assertEquals("code", copy.getCode());
        assertEquals("text", copy.getText());
        assertEquals(Message.INFO, copy.getSeverity());
        assertEquals(0, copy.getInvalidObjectProperties().size());
        assertEquals(0, copy.getReplacementParameters().size());
        assertEquals(1, copy.getMarkers().size());

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
        Message msg = new Message("code", "text", Message.INFO, this, "p1", "p2");
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
        ObjectProperty[] op = { new ObjectProperty("objectA", "pA"),
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

    @Test(expected = NullPointerException.class)
    public void testMessage_NPE() {
        Message.error("text").invalidObjectWithProperties((Object)null);
    }

    @Test
    public void testMessage_Message() {
        Message msg = new Message("code", "text", Message.ERROR);
        Message copy = new Message(msg);
        assertEquals(0, copy.getInvalidObjectProperties().size());
        assertEquals(0, copy.getReplacementParameters().size());

        List<MsgReplacementParameter> params = Arrays.asList(new MsgReplacementParameter("sumInsured", Money.euro(100)),
                new MsgReplacementParameter("minAge", Integer.valueOf(18)));
        msg = new Message("code", "text", Message.ERROR, new ObjectProperty("objectA", "pA"), params);
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

        msg = new Message("code", "blabla", Message.INFO, "ObjectA", "p1", "p2");
        expected = "INFO code[ObjectA.p1, ObjectA.p2]" + LINE_SEPARATOR + "blabla";
        assertEquals(expected, msg.toString());
    }

    @SuppressWarnings("unlikely-arg-type")
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
        msg2 = Message.error("blblbla").code("1").invalidObjectWithProperties("object", "property").create();

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
        ObjectProperty[] op = { new ObjectProperty("objectA", "pA"),
                new ObjectProperty("objectB", "pB") };
        msg = new Message("1", "blabla", Message.ERROR, op);
        msg2 = new Message("1", "blabla", Message.ERROR, "objectA", "pA", "pB");
        assertFalse(msg.equals(msg2));

        // different referenced object properties (different property)
        ObjectProperty[] op2 = { new ObjectProperty("objectA", "pA"),
                new ObjectProperty("objectB", "pDifferent") };
        msg2 = new Message("1", "blabla", Message.ERROR, op2);
        assertFalse(msg.equals(msg2));

        // no differences (with referenced object properties)
        msg2 = new Message("1", "blabla", Message.ERROR, op);
        assertTrue(msg.equals(msg2));
    }

    @Test
    public void testEqualsObject_code_null() {
        Message m1 = new Message(null, "Message with null code", Message.ERROR);
        Message m2 = new Message("some.code", "Message with code", Message.ERROR);

        assertThat(m1.equals(m1), is(true));
        assertThat(m1.equals(m2), is(false));
        assertThat(m2.equals(m1), is(false));

        m1 = new Message(null, null, Message.INFO);
        m2 = new Message(null, null, Message.INFO);
        assertThat(m1.equals(m2), is(true));
    }

    @Test
    public void testEqualsObject_text_null() {
        Message m1 = new Message(null, null, Message.INFO);
        Message m2 = new Message(null, null, Message.INFO);

        assertThat(m1.equals(m2), is(true));
    }

    @Test
    public void testGetNumOfInvalidObjectProperties() {
        Message msg = new Message("code", "text", Message.ERROR);
        assertEquals(0, msg.getNumOfInvalidObjectProperties());

        ObjectProperty[] props = { new ObjectProperty(this, "prop1"),
                new ObjectProperty(this, "prop2"), };
        msg = new Message("code", "text", Message.ERROR, props);

        assertEquals(2, msg.getNumOfInvalidObjectProperties());
    }

    @Test
    public void testGetNumOfReplacementParameters() {
        Message msg = new Message("code", "text", Message.ERROR);
        assertEquals(0, msg.getNumOfReplacementParameters());

        MsgReplacementParameter[] params = {
                new MsgReplacementParameter("sumInsured", Money.euro(100)),
                new MsgReplacementParameter("minAge", Integer.valueOf(18)) };
        msg = new Message("code", "text", Message.ERROR, new ObjectProperty("objectA", "pA"), params);

        assertEquals(2, msg.getNumOfReplacementParameters());
    }

    @Test
    public void testGetNumOfReplacementParameters_defensiveCopy() {
        MsgReplacementParameter msgReplacementParameter = new MsgReplacementParameter("sumInsured", Money.euro(100));
        MsgReplacementParameter[] params = { msgReplacementParameter };

        Message msg = Message.error("text").code("code").replacements(params).create();
        params[0] = null;

        assertEquals(1, msg.getNumOfReplacementParameters());
        assertThat(msg.getReplacementParameters(), hasItem(msgReplacementParameter));
    }

    @Test
    public void testHasReplacementParameter() {
        Message msg = new Message("code", "text", Message.ERROR);
        assertFalse(msg.hasReplacementParameter("param"));

        MsgReplacementParameter[] params = {
                new MsgReplacementParameter("sumInsured", Money.euro(100)),
                new MsgReplacementParameter("minAge", Integer.valueOf(18)) };
        msg = new Message("code", "text", Message.ERROR, new ObjectProperty("objectA", "pA"), params);
        assertFalse(msg.hasReplacementParameter("param"));
        assertTrue(msg.hasReplacementParameter("sumInsured"));
        assertTrue(msg.hasReplacementParameter("minAge"));
    }

    @Test
    public void testGetReplacementValue() {
        Message msg = new Message("code", "text", Message.ERROR);
        assertNull(msg.getReplacementValue("param"));

        MsgReplacementParameter[] params = {
                new MsgReplacementParameter("sumInsured", Money.euro(100)),
                new MsgReplacementParameter("minAge", Integer.valueOf(18)) };
        msg = new Message("code", "text", Message.ERROR, new ObjectProperty("objectA", "pA"), params);
        assertNull(msg.getReplacementValue("param"));
        assertEquals(Money.euro(100), msg.getReplacementValue("sumInsured"));
        assertEquals(Integer.valueOf(18), msg.getReplacementValue("minAge"));
    }

    @Test
    public void testError() {
        Message message = Message.error("text").code("1").create();
        assertEquals(Severity.ERROR, message.getSeverity());
    }

    @Test
    public void testInfo() {
        Message message = Message.info("text").code("1").create();
        assertEquals(Severity.INFO, message.getSeverity());
    }

    @Test
    public void testWarning() {
        Message message = Message.warning("text").code("1").create();
        assertEquals(Severity.WARNING, message.getSeverity());
    }

    @Test
    public void testHasMarker() {
        IMarker marker1 = mock(IMarker.class);
        IMarker marker2 = mock(IMarker.class);
        IMarker marker3 = mock(IMarker.class);
        Set<IMarker> markers = new HashSet<>();
        markers.add(marker1);
        markers.add(marker2);

        Message message = Message.warning("text").code("1").markers(markers).create();

        assertTrue(message.hasMarker(marker1));
        assertTrue(message.hasMarker(marker2));
        assertFalse(message.hasMarker(marker3));
    }

    @Test
    public void testHasMarkers() {
        // markers not null but empty
        Message message = new Message("text", "code", null, null, null, null);
        assertFalse(message.hasMarkers());

        message = Message.warning("text").code("1").markers().create();
        assertFalse(message.hasMarkers());
        // 1 marker
        message = Message.warning("text").code("1").markers(mock(IMarker.class)).create();
        assertTrue(message.hasMarkers());
        // 2 markers
        Set<IMarker> markers = new HashSet<>();
        markers.add(mock(IMarker.class));
        markers.add(mock(IMarker.class));
        message = Message.warning("text").code("1").markers(markers).create();
        assertTrue(message.hasMarkers());
    }

    @Test
    public void testGetMarkers() {
        Message message = new Message("text", "code", null, null, null, null);
        assertTrue(message.getMarkers().isEmpty());

        message = Message.warning("text").code("1").markers().create();
        assertTrue(message.getMarkers().isEmpty());

        message = Message.warning("text").code("1").markers(mock(IMarker.class)).create();
        assertEquals(1, message.getMarkers().size());

        Set<IMarker> markers = new HashSet<>();
        markers.add(mock(IMarker.class));
        markers.add(mock(IMarker.class));
        message = Message.warning("text").code("1").markers(markers).create();
        assertEquals(2, message.getMarkers().size());
    }

    @Test
    public void testGetMarkers_defensiveCopy() {
        IMarker testMarker = mock(IMarker.class);
        IMarker[] markerArray = { testMarker };
        Message message = Message.warning("text").code("1").markers(markerArray).create();

        markerArray[0] = null;

        assertEquals(1, message.getMarkers().size());
        assertEquals(testMarker, message.getMarkers().iterator().next());
    }

    @Test
    public void testInvalideObjects_OneInvalideObjectProperty() {
        ObjectProperty invalidObjectProperty = new ObjectProperty(this, "prop1");
        Message message = Message.error("messageText").invalidObjects(invalidObjectProperty).create();

        assertEquals(1, message.getNumOfInvalidObjectProperties());
        assertEquals("prop1", message.getInvalidObjectProperties().get(0).getProperty());
    }

    @Test
    public void testInvalideObjects_ArrayOfInvalideObjectProperties() {
        ObjectProperty[] objectProperties = { new ObjectProperty(this, "prop1"), new ObjectProperty(this, "prop2") };
        Message message = Message.error("messageText").invalidObjects(objectProperties).create();

        assertEquals(2, message.getNumOfInvalidObjectProperties());
        assertEquals("prop1", message.getInvalidObjectProperties().get(0).getProperty());
        assertEquals("prop2", message.getInvalidObjectProperties().get(1).getProperty());
    }

    @Test
    public void testInvalideObjects_InvalideObjectPropertiesAsVarArgs() {
        Object object1 = new Object();
        Object object2 = new Object();
        Message message = Message.error("messageText")
                .invalidObjects(new ObjectProperty(object1, "prop1"), new ObjectProperty(object2, "prop2")).create();

        assertEquals(2, message.getNumOfInvalidObjectProperties());
        assertEquals(object1, message.getInvalidObjectProperties().get(0).getObject());
        assertEquals("prop1", message.getInvalidObjectProperties().get(0).getProperty());
        assertEquals(object2, message.getInvalidObjectProperties().get(1).getObject());
        assertEquals("prop2", message.getInvalidObjectProperties().get(1).getProperty());
    }

    @Test
    public void testInvalideObjects_InvalideObjectPropertyAsVarArgs() {
        Object object1 = new Object();
        Message message = Message.error("messageText").invalidObjects(new ObjectProperty(object1, "prop1")).create();

        assertEquals(1, message.getNumOfInvalidObjectProperties());
        assertEquals(object1, message.getInvalidObjectProperties().get(0).getObject());
        assertEquals("prop1", message.getInvalidObjectProperties().get(0).getProperty());
    }

    @Test
    public void testInvalideObjects_InvalideObject() {
        Object object1 = new Object();
        Message message = Message.error("messageText").invalidObjectWithProperties(object1).create();

        assertEquals(1, message.getNumOfInvalidObjectProperties());
        assertEquals(object1, message.getInvalidObjectProperties().get(0).getObject());
    }

    @Test
    public void testInvalideObjects_ArrayListOfInvalideObjectProperties() {
        List<ObjectProperty> objectProperties = Arrays.asList(new ObjectProperty(this, "prop1"),
                new ObjectProperty(this, "prop2"));
        Message message = Message.error("messageText").invalidObjects(objectProperties).create();

        assertEquals(2, message.getNumOfInvalidObjectProperties());
        assertEquals("prop1", message.getInvalidObjectProperties().get(0).getProperty());
        assertEquals("prop2", message.getInvalidObjectProperties().get(1).getProperty());
    }

    @Test
    public void testInvalideObjectWithProperties() {
        Object object1 = new Object();
        Message message = Message.error("messageText").invalidObjectWithProperties(object1, "prop1", "prop2").create();

        assertEquals(2, message.getNumOfInvalidObjectProperties());
        assertEquals(object1, message.getInvalidObjectProperties().get(0).getObject());
        assertEquals(object1, message.getInvalidObjectProperties().get(1).getObject());
        assertEquals("prop1", message.getInvalidObjectProperties().get(0).getProperty());
        assertEquals("prop2", message.getInvalidObjectProperties().get(1).getProperty());
    }

    @Test
    public void testInvalidObjectWithProperties_WithoutProperties() {
        Message message = Message.error("messageText").invalidObjectWithProperties(new Object()).create();

        assertEquals(1, message.getNumOfInvalidObjectProperties());
    }

    @Test
    public void testBuilderWithMessage() {
        Set<IMarker> markers = new HashSet<>();
        markers.add(mock(IMarker.class));

        Message message = Message.error("messageText") //
                .code("TestCode") //
                .invalidObjectWithProperties(new Object()) //
                .replacements(new MsgReplacementParameter("ReplaceName", "ReplaceValue")) //
                .markers(markers) //
                .create();

        Message messageCreated = new Builder(message).create();

        assertEquals(message, messageCreated);
    }

    @Test
    public void testBuilderWithMessageAndAddObjectProperty() {
        Set<IMarker> markers = new HashSet<>();
        markers.add(mock(IMarker.class));

        Message message = Message.error("messageText") //
                .code("TestCode") //
                .invalidObjectWithProperties("O1")
                .replacements(new MsgReplacementParameter("ReplaceName", "ReplaceValue")) //
                .markers(markers) //
                .create();

        List<ObjectProperty> invalidObjectProperties = Arrays.asList(new ObjectProperty("O2"),
                new ObjectProperty("O3"));
        Message messageCreated = new Builder(message).invalidObjects(invalidObjectProperties).create();

        assertEquals(3, messageCreated.getNumOfInvalidObjectProperties());
        assertEquals("O1", messageCreated.getInvalidObjectProperties().get(0).getObject());
        assertEquals("O2", messageCreated.getInvalidObjectProperties().get(1).getObject());
        assertEquals("O3", messageCreated.getInvalidObjectProperties().get(2).getObject());
        assertEquals(message.getText(), messageCreated.getText());
        assertEquals(message.getCode(), messageCreated.getCode());
        assertEquals(message.getSeverity(), messageCreated.getSeverity());
        assertEquals(1, messageCreated.getNumOfReplacementParameters());
        assertTrue(messageCreated.hasMarkers());
    }

}
