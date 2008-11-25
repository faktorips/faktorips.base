/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.bf.BusinessFunction;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BusinessFunctionTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
    }

    public void testGetIpsObjectType() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        assertEquals(bf.getIpsObjectType(), BusinessFunctionIpsObjectType.getInstance());
    }

    public void testNewPartClass() throws CoreException {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement bfe = bf.newSimpleBFElement(BFElementType.END, new Point(1, 1));
        assertEquals(bfe.getType(), BFElementType.END);
    }

    public void testInitFromXmlElement() throws Exception {
        Document doc = getTestDocument();
        IBusinessFunction bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        
        bf.initFromXml(doc.getDocumentElement());

        assertEquals(new Dimension(100, 150), bf.getParameterRectangleSize());
        assertEquals(new Point(10, 20), bf.getParameterRectangleLocation());
        
        List<IBFElement> elements = bf.getBFElements();
        assertEquals(9, elements.size());
        List<IControlFlow> controlFlows = bf.getControlFlows();
        assertEquals(7, controlFlows.size());

        IBFElement bfe = bf.getBFElement(10);
        List<IControlFlow> out = bfe.getOutgoingControlFlow();
        IControlFlow cf = bf.getControlFlow(17);
        assertTrue(out.contains(cf));

        bfe = bf.getBFElement(15);
        out = bfe.getOutgoingControlFlow();
        cf = bf.getControlFlow(19);
        assertTrue(out.contains(cf));
        List<IControlFlow> in = bfe.getIncomingControlFlow();
        cf = bf.getControlFlow(18);
        assertTrue(in.contains(cf));
        cf = bf.getControlFlow(23);
        assertTrue(in.contains(cf));
        
        cf = bf.getControlFlow(17);
        IBFElement source = bf.getBFElement(10);
        assertEquals(source, cf.getSource());
        IBFElement target = bf.getBFElement(12);
        assertEquals(target, cf.getTarget());

    }

    public void testToXml() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(1, 1));
        IActionBFE methodCallAction = bf.newMethodCallAction(new Point(2, 2));
        IActionBFE opaqueAction = bf.newOpaqueAction(new Point(3, 3));
        IBFElement decision = bf.newDecision(new Point(4, 4));
        IBFElement parameter = bf.newParameter();
        IBFElement end = bf.newSimpleBFElement(BFElementType.END, new Point(10, 10));
        IBFElement start = bf.newSimpleBFElement(BFElementType.START, new Point(20, 20));
        IBFElement merge = bf.newSimpleBFElement(BFElementType.MERGE, new Point(30, 30));
        IControlFlow cf1 = bf.newControlFlow();
        IControlFlow cf2 = bf.newControlFlow();
        Document doc = getDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement(IBusinessFunction.XML_TAG));
        Element element = bf.toXml(doc);
        BusinessFunction bf2 = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf2");
        bf2.initFromXml(element);
        assertEquals(action, bf.getBFElement(action.getId()));
        assertEquals(methodCallAction, bf.getBFElement(methodCallAction.getId()));
        assertEquals(opaqueAction, bf.getBFElement(opaqueAction.getId()));
        assertEquals(decision, bf.getBFElement(decision.getId()));
        assertEquals(parameter, bf.getBFElement(parameter.getId()));
        assertEquals(end, bf.getBFElement(end.getId()));
        assertEquals(start, bf.getBFElement(start.getId()));
        assertEquals(merge, bf.getBFElement(merge.getId()));
        assertEquals(cf1, bf.getControlFlow(cf1.getId()));
        assertEquals(cf2, bf.getControlFlow(cf2.getId()));
    }

    public void testGetBFElementById() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(1, 1));
        IActionBFE methodCallAction = bf.newMethodCallAction(new Point(2, 2));
        IActionBFE opaqueAction = bf.newOpaqueAction(new Point(3, 3));
        assertEquals(action, bf.getBFElement(action.getId()));
        assertEquals(methodCallAction, bf.getBFElement(methodCallAction.getId()));
        assertEquals(opaqueAction, bf.getBFElement(opaqueAction.getId()));
    }

    public void testGetBFElements() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(1, 1));
        IActionBFE methodCallAction = bf.newMethodCallAction(new Point(2, 2));
        IActionBFE opaqueAction = bf.newOpaqueAction(new Point(3, 3));
        List<IBFElement> bfes = bf.getBFElements();
        assertTrue(bfes.contains(action));
        assertTrue(bfes.contains(methodCallAction));
        assertTrue(bfes.contains(opaqueAction));
    }

    public void testGetControlFlows() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
        "bf");
        IControlFlow cf1 = bf.newControlFlow();
        IControlFlow cf2 = bf.newControlFlow();
        IControlFlow cf3 = bf.newControlFlow();
        assertTrue(bf.getControlFlows().contains(cf1));
        assertTrue(bf.getControlFlows().contains(cf2));
        assertTrue(bf.getControlFlows().contains(cf3));
    }

    public void testGetControlFlowById() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
        "bf");
        IControlFlow cf1 = bf.newControlFlow();
        IControlFlow cf2 = bf.newControlFlow();
        IControlFlow cf3 = bf.newControlFlow();
        assertEquals(cf1, bf.getControlFlow(cf1.getId()));
        assertEquals(cf2, bf.getControlFlow(cf2.getId()));
        assertEquals(cf3, bf.getControlFlow(cf3.getId()));
    }

    public void testGetParameterBFEs() throws Exception{
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
        "bf");
        IParameterBFE p1 = bf.newParameter();
        IParameterBFE p2 = bf.newParameter();
        IParameterBFE p3 = bf.newParameter();
        List<IParameterBFE> pList = bf.getParameterBFEs();
        assertEquals(3, pList.size());
        assertTrue(pList.contains(p1));
        assertTrue(pList.contains(p2));
        assertTrue(pList.contains(p3));
    }
    
    public void testgetParameterBFE() throws Exception{
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
        "bf");
        IParameterBFE p1 = bf.newParameter();
        p1.setName("p1");
        IParameterBFE p2 = bf.newParameter();
        p2.setName("p2");
        IParameterBFE p3 = bf.newParameter();
        p3.setName("p3");
        assertEquals(p1, bf.getParameterBFE("p1"));
        assertEquals(p2, bf.getParameterBFE("p2"));
        assertEquals(p3, bf.getParameterBFE("p3"));
    }
}
