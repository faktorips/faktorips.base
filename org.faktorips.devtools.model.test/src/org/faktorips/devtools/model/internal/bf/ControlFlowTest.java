/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.bf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.model.bf.IActionBFE;
import org.faktorips.devtools.model.bf.IBFElement;
import org.faktorips.devtools.model.bf.IBusinessFunction;
import org.faktorips.devtools.model.bf.IControlFlow;
import org.faktorips.devtools.model.bf.IDecisionBFE;
import org.faktorips.devtools.model.bf.Location;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ControlFlowTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private TestContentsChangeListener listener;
    private IBusinessFunction bf;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        listener = new TestContentsChangeListener();
        ipsProject.getIpsModel().addChangeListener(listener);
        bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");

    }

    @Test
    public void testToXml() throws Exception {
        IControlFlow cf = bf.newControlFlow();
        cf.addBendpoint(0, new Location(10, 10));
        cf.addBendpoint(1, new Location(20, 20));
        cf.addBendpoint(2, new Location(30, 30));

        Document doc = getDocumentBuilder().newDocument();
        Element el = cf.toXml(doc);

        bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf2");
        cf = new ControlFlow(bf, "1");
        cf.initFromXml(el);
        List<Location> bps = cf.getBendpoints();
        assertEquals(3, bps.size());
        assertTrue(bps.contains(new Location(10, 10)));
        assertTrue(bps.contains(new Location(20, 20)));
        assertTrue(bps.contains(new Location(30, 30)));
    }

    @Test
    public void testInitFromXmlElement() {
        Document doc = getTestDocument();
        NodeList nl = doc.getDocumentElement().getElementsByTagName(IControlFlow.XML_TAG);
        IControlFlow cf = new ControlFlow(bf, "1");
        cf.initFromXml((Element)nl.item(0));
        List<Location> bendpoints = cf.getBendpoints();
        assertEquals(2, bendpoints.size());
        Location bp = bendpoints.get(0);
        assertEquals(new Location(423, 566), bp);
        bp = bendpoints.get(1);
        assertEquals(new Location(426, 256), bp);
    }

    @Test
    public void testSetBendpoint() {
        IControlFlow cf = bf.newControlFlow();
        cf.addBendpoint(0, new Location(10, 10));
        cf.addBendpoint(1, new Location(20, 20));
        cf.addBendpoint(2, new Location(30, 30));
        List<Location> bps = cf.getBendpoints();
        assertEquals(3, bps.size());
        assertEquals(new Location(10, 10), bps.get(0));
        assertEquals(new Location(20, 20), bps.get(1));
        assertEquals(new Location(30, 30), bps.get(2));

        listener.clear();
        cf.setBendpoint(1, new Location(25, 25));
        assertEquals(new Location(25, 25), bps.get(1));
        assertTrue(listener.getIpsObjectParts().contains(cf));

        listener.clear();
        cf.setBendpoint(2, new Location(35, 35));
        assertEquals(new Location(35, 35), bps.get(2));
        assertTrue(listener.getIpsObjectParts().contains(cf));

        IControlFlow cf2 = bf.newControlFlow();
        listener.clear();
        cf2.setBendpoint(4, null);
        assertTrue(cf2.getBendpoints().isEmpty());
        assertTrue(listener.getIpsObjectParts().isEmpty());
    }

    @Test
    public void testAddBendpoint() {
        IControlFlow cf = bf.newControlFlow();
        listener.clear();
        cf.addBendpoint(0, new Location(10, 10));
        cf.addBendpoint(1, new Location(20, 20));
        cf.addBendpoint(2, new Location(30, 30));
        List<Location> bps = cf.getBendpoints();
        assertEquals(3, bps.size());
        assertEquals(3, listener.getIpsObjectParts().size());
        assertTrue(listener.getIpsObjectParts().contains(cf));

        assertEquals(new Location(10, 10), bps.get(0));
        assertEquals(new Location(20, 20), bps.get(1));
        assertEquals(new Location(30, 30), bps.get(2));

        IControlFlow cf2 = bf.newControlFlow();
        listener.clear();
        cf2.addBendpoint(0, null);
        assertTrue(cf2.getBendpoints().isEmpty());
        assertTrue(listener.getIpsObjectParts().isEmpty());
    }

    @Test
    public void testRemoveBendpoint() {
        IControlFlow cf = bf.newControlFlow();
        cf.addBendpoint(0, new Location(10, 10));
        cf.addBendpoint(1, new Location(20, 20));
        cf.addBendpoint(2, new Location(30, 30));
        List<Location> bps = cf.getBendpoints();
        assertEquals(3, bps.size());
        listener.clear();
        cf.removeBendpoint(1);
        bps = cf.getBendpoints();
        assertEquals(2, bps.size());
        assertEquals(new Location(10, 10), bps.get(0));
        assertEquals(new Location(30, 30), bps.get(1));
        assertTrue(listener.getIpsObjectParts().contains(cf));
    }

    @Test
    public void testGetBusinessFunction() {
        IControlFlow cf = bf.newControlFlow();
        assertEquals(bf, cf.getBusinessFunction());
    }

    @Test
    public void testSetSource() {
        IBFElement source = bf.newDecision(new Location(1, 1));
        IControlFlow cf = bf.newControlFlow();

        listener.clear();
        cf.setSource(null);
        assertNull(cf.getSource());
        assertTrue(listener.getIpsObjectParts().isEmpty());

        listener.clear();
        cf.setSource(source);
        assertEquals(source, cf.getSource());
        assertTrue(listener.getIpsObjectParts().contains(source));
        assertEquals(cf, source.getOutgoingControlFlow().get(0));

        listener.clear();
        cf.setSource(source);
        assertFalse(listener.getIpsObjectParts().contains(source));
        assertEquals(1, source.getOutgoingControlFlow().size());

        listener.clear();
        cf.setSource(null);
        assertTrue(listener.getIpsObjectParts().contains(source));
        assertTrue(source.getOutgoingControlFlow().isEmpty());
    }

    @Test
    public void testSetTarget() {
        IBFElement target = bf.newDecision(new Location(1, 1));
        IControlFlow cf = bf.newControlFlow();

        listener.clear();
        cf.setTarget(null);
        assertNull(cf.getTarget());
        assertTrue(listener.getIpsObjectParts().isEmpty());

        listener.clear();
        cf.setTarget(target);
        assertEquals(target, cf.getTarget());
        assertTrue(listener.getIpsObjectParts().contains(target));
        assertEquals(cf, target.getIncomingControlFlow().get(0));

        listener.clear();
        cf.setTarget(target);
        assertFalse(listener.getIpsObjectParts().contains(target));
        assertEquals(1, target.getIncomingControlFlow().size());

        listener.clear();
        cf.setTarget(null);
        assertTrue(listener.getIpsObjectParts().contains(target));
        assertTrue(target.getIncomingControlFlow().isEmpty());
    }

    @Test
    public void testValidateValue() throws Exception {
        IDecisionBFE decisionBFE = bf.newDecision(new Location(10, 10));
        decisionBFE.setDatatype(Datatype.INTEGER.getQualifiedName());
        IActionBFE actionBFE = bf.newOpaqueAction(new Location(10, 10));
        IControlFlow cf = bf.newControlFlow();
        cf.setSource(decisionBFE);
        cf.setTarget(actionBFE);
        MessageList msgList = cf.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IControlFlow.MSGCODE_VALUE_NOT_SPECIFIED));

        cf.setConditionValue("abc");
        msgList = cf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IControlFlow.MSGCODE_VALUE_NOT_SPECIFIED));
        assertNotNull(msgList.getMessageByCode(IControlFlow.MSGCODE_VALUE_NOT_VALID));

        cf.setConditionValue("100");
        msgList = cf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IControlFlow.MSGCODE_VALUE_NOT_VALID));
    }

    @Test
    public void testValidateDuplicateValue() throws Exception {
        IDecisionBFE decisionBFE = bf.newDecision(new Location(10, 10));
        decisionBFE.setDatatype(Datatype.INTEGER.getQualifiedName());

        IActionBFE action1 = bf.newOpaqueAction(new Location(10, 10));
        IControlFlow cf = bf.newControlFlow();
        cf.setSource(decisionBFE);
        cf.setTarget(action1);
        cf.setConditionValue("1");

        IActionBFE action2 = bf.newOpaqueAction(new Location(10, 10));
        IControlFlow cf2 = bf.newControlFlow();
        cf2.setSource(decisionBFE);
        cf2.setTarget(action2);
        cf2.setConditionValue("2");

        IActionBFE action3 = bf.newOpaqueAction(new Location(10, 10));
        cf = bf.newControlFlow();
        cf.setSource(decisionBFE);
        cf.setTarget(action3);
        cf.setConditionValue("3");

        MessageList msgList = bf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IControlFlow.MSGCODE_DUBLICATE_VALUES));

        cf2.setConditionValue("1");
        msgList = bf.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IControlFlow.MSGCODE_DUBLICATE_VALUES));
    }
}
