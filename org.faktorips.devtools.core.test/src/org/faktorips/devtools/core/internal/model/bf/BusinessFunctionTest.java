/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.DependencyDetail;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IMethodCallBFE;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BusinessFunctionTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
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
        IBFElement bfe = bf.newEnd(new Point(1, 1));
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

        IBFElement bfe = bf.getBFElement("10");
        List<IControlFlow> out = bfe.getOutgoingControlFlow();
        IControlFlow cf = bf.getControlFlow("17");
        assertTrue(out.contains(cf));

        bfe = bf.getBFElement("15");
        out = bfe.getOutgoingControlFlow();
        cf = bf.getControlFlow("19");
        assertTrue(out.contains(cf));
        List<IControlFlow> in = bfe.getIncomingControlFlow();
        cf = bf.getControlFlow("18");
        assertTrue(in.contains(cf));
        cf = bf.getControlFlow("23");
        assertTrue(in.contains(cf));

        cf = bf.getControlFlow("17");
        IBFElement source = bf.getBFElement("10");
        assertEquals(source, cf.getSource());
        IBFElement target = bf.getBFElement("12");
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
        IBFElement end = bf.newEnd(new Point(10, 10));
        IBFElement start = bf.newStart(new Point(20, 20));
        IBFElement merge = bf.newMerge(new Point(30, 30));
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

    public void testGetParameterBFEs() throws Exception {
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

    public void testgetParameterBFE() throws Exception {
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

    public void testValidateStartOnlyOnce() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement start1 = bf.newStart(new Point(10, 10));
        MessageList msgList = bf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_START_SINGLE_OCCURRENCE));
        IBFElement start2 = bf.newStart(new Point(10, 10));
        msgList = bf.validate(ipsProject);

        MessageList list1 = msgList.getMessagesFor(start1);
        assertNotNull(list1.getMessageByCode(IBusinessFunction.MSGCODE_START_SINGLE_OCCURRENCE));

        MessageList list2 = msgList.getMessagesFor(start2);
        assertNotNull(list2.getMessageByCode(IBusinessFunction.MSGCODE_START_SINGLE_OCCURRENCE));
    }

    public void testValidateEndOnlyOnce() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement end1 = bf.newEnd(new Point(10, 10));
        MessageList msgList = bf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_END_SINGLE_OCCURRENCE));
        IBFElement end2 = bf.newEnd(new Point(10, 10));
        msgList = bf.validate(ipsProject);

        MessageList list1 = msgList.getMessagesFor(end1);
        assertNotNull(list1.getMessageByCode(IBusinessFunction.MSGCODE_END_SINGLE_OCCURRENCE));

        MessageList list2 = msgList.getMessagesFor(end2);
        assertNotNull(list2.getMessageByCode(IBusinessFunction.MSGCODE_END_SINGLE_OCCURRENCE));
    }

    public void testValidateBFElementNameCollision() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IActionBFE action1 = bf.newOpaqueAction(new Point(10, 10));
        action1.setName("action1");
        MessageList msgList = bf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_ELEMENT_NAME_COLLISION));

        IActionBFE action2 = bf.newOpaqueAction(new Point(10, 10));
        action2.setName("action1");
        msgList = bf.validate(ipsProject);

        MessageList list1 = msgList.getMessagesFor(action1);
        assertNotNull(list1.getMessageByCode(IBusinessFunction.MSGCODE_ELEMENT_NAME_COLLISION));

        MessageList list2 = msgList.getMessagesFor(action2);
        assertNotNull(list2.getMessageByCode(IBusinessFunction.MSGCODE_ELEMENT_NAME_COLLISION));

        IPolicyCmptType policy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        IMethod method = policy.newMethod();
        method.setName("action1");
        method.setDatatype(Datatype.STRING.getQualifiedName());
        method.setModifier(Modifier.PUBLIC);

        IParameterBFE parameter = bf.newParameter();
        parameter.setDatatype(policy.getQualifiedName());
        parameter.setName("policy");

        IActionBFE action3 = bf.newMethodCallAction(new Point(10, 10));
        action3.setTarget("policy");
        action3.setExecutableMethodName("action1");

        msgList = bf.validate(ipsProject);
        MessageList list3 = msgList.getMessagesFor(action3);
        assertNotNull(list3.getMessageByCode(IBusinessFunction.MSGCODE_ELEMENT_NAME_COLLISION));

        BusinessFunction bfAction1 = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType
                .getInstance(), "action1");

        IActionBFE action4 = bf.newBusinessFunctionCallAction(new Point(10, 10));
        action4.setTarget(bfAction1.getQualifiedName());

        msgList = bf.validate(ipsProject);
        MessageList list4 = msgList.getMessagesFor(action4);
        assertNotNull(list4.getMessageByCode(IBusinessFunction.MSGCODE_ELEMENT_NAME_COLLISION));

        // deleting the inline action should still result in a name conflict of the two remaining
        // actions
        action1.delete();
        action2.delete();

        msgList = bf.validate(ipsProject);
        list3 = msgList.getMessagesFor(action3);
        assertNotNull(list3.getMessageByCode(IBusinessFunction.MSGCODE_ELEMENT_NAME_COLLISION));

        list4 = msgList.getMessagesFor(action4);
        assertNotNull(list4.getMessageByCode(IBusinessFunction.MSGCODE_ELEMENT_NAME_COLLISION));

        // only one action remains the error needs to disappear
        action3.delete();
        msgList = bf.validate(ipsProject);
        list4 = msgList.getMessagesFor(action4);
        assertNull(list4.getMessageByCode(IBusinessFunction.MSGCODE_ELEMENT_NAME_COLLISION));
    }

    public void testValidateStartNodeMissing() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        MessageList msgList = bf.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_START_DEFINITION_MISSING));

        bf.newStart(new Point(10, 10));
        msgList = bf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_START_DEFINITION_MISSING));
    }

    public void testValidateEndNodeMissing() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        MessageList msgList = bf.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_END_DEFINITION_MISSING));

        bf.newEnd(new Point(10, 10));
        msgList = bf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_END_DEFINITION_MISSING));
    }

    public void testValidateNotConnected1() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement start = bf.newStart(new Point(10, 10));
        IBFElement end = bf.newEnd(new Point(10, 10));

        MessageList msgList = bf.validate(ipsProject);

        MessageList list1 = msgList.getMessagesFor(start);
        assertNotNull(list1.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        MessageList list2 = msgList.getMessagesFor(end);
        assertNotNull(list2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));

        IControlFlow cf = bf.newControlFlow();
        cf.setSource(start);
        cf.setTarget(end);

        msgList = bf.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNull(msgList.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));
    }

    public void testValidateNotConnected2() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");
        IBFElement start = bf.newStart(new Point(10, 10));
        IBFElement end = bf.newEnd(new Point(10, 10));
        IBFElement action1 = bf.newOpaqueAction(new Point(10, 10));
        IBFElement action2 = bf.newOpaqueAction(new Point(10, 10));
        IBFElement decision = bf.newDecision(new Point(10, 10));
        IBFElement merge = bf.newMerge(new Point(10, 10));

        // check unconnected elements
        MessageList msgList = bf.validate(ipsProject);
        MessageList msgAction1 = msgList.getMessagesFor(action1);
        assertNotNull(msgAction1.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNotNull(msgAction1.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        MessageList msgAction2 = msgList.getMessagesFor(action2);
        assertNotNull(msgAction2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNotNull(msgAction2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        MessageList msgDecision = msgList.getMessagesFor(decision);
        assertNotNull(msgDecision.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNotNull(msgDecision.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        MessageList msgMerge = msgList.getMessagesFor(merge);
        assertNotNull(msgMerge.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNotNull(msgMerge.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        // build graph uncompleted
        IControlFlow cf = bf.newControlFlow();
        cf.setSource(start);
        cf.setTarget(merge);

        cf = bf.newControlFlow();
        cf.setSource(merge);
        cf.setTarget(action1);

        cf = bf.newControlFlow();
        cf.setSource(action1);
        cf.setTarget(decision);

        cf = bf.newControlFlow();
        cf.setSource(decision);
        cf.setTarget(end);

        msgList = bf.validate(ipsProject);

        assertTrue(msgList.getMessagesFor(start).isEmpty());
        assertTrue(msgList.getMessagesFor(end).isEmpty());

        msgAction1 = msgList.getMessagesFor(action1);
        assertNull(msgAction1.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNull(msgAction1.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        msgMerge = msgList.getMessagesFor(merge);
        assertNull(msgMerge.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNull(msgMerge.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        msgDecision = msgList.getMessagesFor(decision);
        assertNull(msgDecision.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNull(msgDecision.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        msgAction2 = msgList.getMessagesFor(action2);
        assertNotNull(msgAction2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNotNull(msgAction2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        cf = bf.newControlFlow();
        cf.setSource(decision);
        cf.setTarget(action2);

        msgList = bf.validate(ipsProject);
        msgAction2 = msgList.getMessagesFor(action2);
        assertNull(msgAction2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNotNull(msgAction2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));

        cf = bf.newControlFlow();
        cf.setSource(action2);
        cf.setTarget(merge);

        msgList = bf.validate(ipsProject);
        msgAction2 = msgList.getMessagesFor(action2);
        assertNull(msgAction2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_START));
        assertNull(msgAction2.getMessageByCode(IBusinessFunction.MSGCODE_NOT_CONNECTED_WITH_END));
    }

    public void testDependsOn() throws Exception {
        BusinessFunction bf = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf");

        IDependency[] dependencies = bf.dependsOn();
        assertEquals(0, dependencies.length);

        BusinessFunction bf2 = (BusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(),
                "bf2");

        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(10, 10));
        action.setTarget(bf2.getQualifiedName());

        dependencies = bf.dependsOn();
        assertEquals(1, dependencies.length);
        assertEquals(IpsObjectDependency.createReferenceDependency(bf.getQualifiedNameType(), bf2
                .getQualifiedNameType()), dependencies[0]);

        List<IDependencyDetail> details = bf.getDependencyDetails(dependencies[0]);
        assertEquals(1, details.size());
        assertTrue(details.contains(new DependencyDetail(action, IMethodCallBFE.PROPERTY_TARGET)));

        action.delete();
        dependencies = bf.dependsOn();
        assertEquals(0, dependencies.length);

        IPolicyCmptType pcType = newPolicyCmptType(ipsProject, "policy");
        IMethod method = pcType.newMethod();
        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.setModifier(Modifier.PUBLIC);
        method.setName("calculate");

        IParameterBFE param = bf.newParameter();
        param.setDatatype(pcType.getQualifiedName());
        param.setName("policy");

        action = bf.newMethodCallAction(new Point(10, 10));
        dependencies = bf.dependsOn();
        assertEquals(0, dependencies.length);

        action.setTarget(param.getName());
        action.setExecutableMethodName(method.getName());
        dependencies = bf.dependsOn();
        assertEquals(1, dependencies.length);
        assertEquals(IpsObjectDependency.createReferenceDependency(bf.getQualifiedNameType(), pcType
                .getQualifiedNameType()), dependencies[0]);

        details = bf.getDependencyDetails(dependencies[0]);
        assertEquals(1, details.size());
        assertTrue(details.contains(new DependencyDetail(param, IParameterBFE.PROPERTY_DATATYPE)));
    }
}
