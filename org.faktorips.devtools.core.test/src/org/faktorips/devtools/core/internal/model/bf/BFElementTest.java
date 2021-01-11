/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BFElementTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private TestContentsChangeListener listener;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        listener = new TestContentsChangeListener();
        ipsProject.getIpsModel().addChangeListener(listener);
    }

    @Test
    public void testInitFromXml() throws Exception {
        Document doc = getTestDocument();
        IBusinessFunction bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");

        IBFElement bfe = new BFElement(bf, "1");
        NodeList nl = doc.getDocumentElement().getElementsByTagName(IBFElement.XML_TAG);
        bfe.initFromXml((Element)nl.item(0));
        assertEquals("10", bfe.getId());
        assertEquals(BFElementType.START, bfe.getType());
        assertEquals(new Dimension(30, 30), bfe.getSize());
        assertEquals(new Point(245, 51), bfe.getLocation());
    }

    @Test
    public void testtoXml() throws Exception {
        IBusinessFunction bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newOpaqueAction(new Point(10, 10));
        bfe.setLocation(new Point(14, 14));
        bfe.setSize(new Dimension(100, 100));
        Document doc = getDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement(IBusinessFunction.XML_TAG));
        Element domEl = bfe.toXml(doc);

        IBusinessFunction bf2 = (IBusinessFunction)newIpsObject(ipsProject,
                BusinessFunctionIpsObjectType.getInstance(), "bf2");
        IActionBFE action = new ActionBFE(bf2, "1");
        action.initFromXml(domEl);
        assertEquals(bfe.getLocation(), action.getLocation());
        assertEquals(bfe.getSize(), action.getSize());
        assertEquals(bfe.getType(), action.getType());
    }

    @Test
    public void testAddIncomingControlFlow() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow in = bf.newControlFlow();
        bfe.addIncomingControlFlow(in);
        assertEquals(in, bfe.getIncomingControlFlow().get(0));
        assertTrue(listener.getIpsObjectParts().contains(bfe));
        assertTrue(listener.getEventTypes().contains(ContentChangeEvent.TYPE_PROPERTY_CHANGED));
        assertEquals(in.getTarget(), bfe);
        assertNull(in.getSource());

        IControlFlow cf = bf.newControlFlow();
        assertFalse(bfe.getIncomingControlFlow().contains(cf));
    }

    @Test
    public void testAddOutgoingControlFlow() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow out = bf.newControlFlow();
        bfe.addOutgoingControlFlow(out);
        assertEquals(out, bfe.getOutgoingControlFlow().get(0));
        assertTrue(listener.getIpsObjectParts().contains(bfe));
        assertTrue(listener.getEventTypes().contains(ContentChangeEvent.TYPE_PROPERTY_CHANGED));
        assertEquals(bfe, out.getSource());
        assertNull(out.getTarget());

        IControlFlow cf = bf.newControlFlow();
        assertFalse(bfe.getOutgoingControlFlow().contains(cf));
    }

    @Test
    public void testRemoveIncomingControlFlow() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow in = bf.newControlFlow();
        bfe.addIncomingControlFlow(in);
        assertEquals(in, bfe.getIncomingControlFlow().get(0));

        listener.clear();
        IControlFlow cf = bf.newControlFlow();
        assertFalse(bfe.removeIncomingControlFlow(cf));
        assertEquals(in, bfe.getIncomingControlFlow().get(0));
        assertFalse(listener.getIpsObjectParts().contains(bfe));

        listener.clear();
        assertTrue(bfe.removeIncomingControlFlow(in));
        assertTrue(bfe.getIncomingControlFlow().isEmpty());
        assertTrue(listener.getIpsObjectParts().contains(bfe));
    }

    @Test
    public void testRemoveOutgoingControlFlow() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow out = bf.newControlFlow();
        bfe.addOutgoingControlFlow(out);
        assertEquals(out, bfe.getOutgoingControlFlow().get(0));

        listener.clear();
        IControlFlow cf = bf.newControlFlow();
        assertFalse(bfe.removeIncomingControlFlow(cf));
        assertEquals(out, bfe.getOutgoingControlFlow().get(0));
        assertFalse(listener.getIpsObjectParts().contains(bfe));

        listener.clear();
        assertTrue(bfe.removeOutgoingControlFlow(out));
        assertTrue(bfe.getOutgoingControlFlow().isEmpty());
        assertTrue(listener.getIpsObjectParts().contains(bfe));
    }

    @Test
    public void testGetAllControlFlows() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newOpaqueAction(new Point(10, 10));
        IControlFlow out = bf.newControlFlow();
        IControlFlow in = bf.newControlFlow();
        bfe.addOutgoingControlFlow(out);
        bfe.addIncomingControlFlow(in);
        assertTrue(bfe.getAllControlFlows().contains(out));
        assertTrue(bfe.getAllControlFlows().contains(in));
    }

    @Test
    public void testGetBusinessFunction() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newMerge(new Point(10, 10));
        assertEquals(bf, bfe.getBusinessFunction());

    }

    @Test
    public void testSetLocation() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newOpaqueAction(new Point(10, 10));
        listener.clear();
        bfe.setLocation(new Point(10, 10));
        assertTrue(listener.getIpsObjectParts().isEmpty());
        assertEquals(new Point(10, 10), bfe.getLocation());

        bfe.setLocation(new Point(20, 20));
        assertTrue(listener.getIpsObjectParts().contains(bfe));
        assertTrue(listener.getEventTypes().contains(ContentChangeEvent.TYPE_PROPERTY_CHANGED));
        assertEquals(new Point(20, 20), bfe.getLocation());

        listener.clear();
        bfe.setLocation(null);
        assertTrue(listener.getIpsObjectParts().contains(bfe));
        assertNull(bfe.getLocation());

        listener.clear();
        bfe.setLocation(new Point(20, 20));
        assertTrue(listener.getIpsObjectParts().contains(bfe));
        assertEquals(new Point(20, 20), bfe.getLocation());
    }

    @Test
    public void testSetSize() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newOpaqueAction(new Point(10, 10));

        listener.clear();
        bfe.setSize(new Dimension(20, 20));
        assertTrue(listener.getIpsObjectParts().contains(bfe));
        assertTrue(listener.getEventTypes().contains(ContentChangeEvent.TYPE_PROPERTY_CHANGED));
        assertEquals(new Dimension(20, 20), bfe.getSize());

        listener.clear();
        bfe.setSize(null);
        assertTrue(listener.getIpsObjectParts().contains(bfe));
        assertNull(bfe.getSize());

        listener.clear();
        bfe.setSize(new Dimension(20, 20));
        assertTrue(listener.getIpsObjectParts().contains(bfe));
        assertEquals(new Dimension(20, 20), bfe.getSize());
    }
}
