/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IMethodCallBFE;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ActionBFETest extends AbstractIpsPluginTest {

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
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");

    }

    @Test
    public void testPropertiesToXml() throws Exception {
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        action.setExecutableMethodName("aMethod");
        action.setTarget("aTarget");
        Document doc = getDocumentBuilder().newDocument();
        Element el = action.toXml(doc);
        IActionBFE action2 = bf.newOpaqueAction(new Point(10, 10));
        action2.initFromXml(el);
        assertEquals(action.getExecutableMethodName(), action2.getExecutableMethodName());
        assertEquals(action.getTarget(), action2.getTarget());
    }

    @Test
    public void testInitPropertiesFromXml() {
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        action.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName(IActionBFE.XML_TAG)
                .item(0));
        assertEquals("aTarget", action.getTarget());
        assertEquals("aMethod", action.getExecutableMethodName());
    }

    @Test
    public void testGetExecutableMethodName() {
        IActionBFE actionBFE = bf.newOpaqueAction(new Point(10, 10));
        listener.clear();
        actionBFE.setExecutableMethodName("aMethod");
        assertEquals("aMethod", actionBFE.getExecutableMethodName());
        assertTrue(listener.getIpsObjectParts().contains(actionBFE));
    }

    @Test
    public void testGetTarget() {
        IActionBFE actionBFE = bf.newOpaqueAction(new Point(10, 10));
        listener.clear();
        actionBFE.setTarget("aTarget");
        assertEquals("aTarget", actionBFE.getTarget());
        assertTrue(listener.getIpsObjectParts().contains(actionBFE));
    }

    @Test
    public void testFindTarget() throws Exception {
        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        IParameterBFE parameter = bf.newParameter();
        parameter.setName("p1");
        actionBFE.setTarget("p1");
        assertEquals(parameter, actionBFE.getParameter());

        IBusinessFunction bf2 = (IBusinessFunction)newIpsObject(ipsProject,
                BusinessFunctionIpsObjectType.getInstance(), "bf2");
        actionBFE = bf.newBusinessFunctionCallAction(new Point(10, 10));
        actionBFE.setTarget("bf2");
        assertEquals(bf2, actionBFE.findReferencedBusinessFunction());
    }

    @Test
    public void testValidateTargetNotSpecified() throws Exception {
        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));

        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_SPECIFIED));

        actionBFE.setTarget("policy");
        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_SPECIFIED));
    }

    @Test
    public void testValidateTargetDoesNotExist() throws Exception {
        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        actionBFE.setTarget("p");
        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_DOES_NOT_EXIST));

        IParameterBFE p = bf.newParameter();
        p.setName("p");
        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_DOES_NOT_EXIST));
    }

    @Test
    public void testValidateTargetNotValidType() throws Exception {

        IParameterBFE p = bf.newParameter();
        p.setName("p");
        p.setDatatype(Datatype.STRING.getQualifiedName());

        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        actionBFE.setExecutableMethodName("aMethod");
        actionBFE.setTarget("p");
        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_VALID_TYPE));

        IPolicyCmptType policy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "policy");
        p.setDatatype(policy.getQualifiedName());
        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_VALID_TYPE));

    }

    @Test
    public void testValidateMethodNotSpecified() throws Exception {

        IParameterBFE p = bf.newParameter();
        p.setName("p");

        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        actionBFE.setTarget("p");

        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_NOT_SPECIFIED));

        actionBFE.setExecutableMethodName("aMethod");
        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_NOT_SPECIFIED));

        actionBFE.setExecutableMethodName("end");
        msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBFElement.MSGCODE_NAME_NOT_VALID));
    }

    @Test
    public void testValidateMethodDoesNotExist() throws Exception {
        IPolicyCmptType policy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "policy");

        IParameterBFE p = bf.newParameter();
        p.setName("p");
        p.setDatatype(policy.getQualifiedName());

        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        actionBFE.setTarget("p");
        actionBFE.setExecutableMethodName("aMethod");

        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

        IMethod method = policy.newMethod();
        method.setName("aMethod");
        method.setDatatype(Datatype.STRING.getQualifiedName());
        method.setModifier(Modifier.PUBLIC);

        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

        method.delete();
        msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

        IPolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "superPolicy");
        policy.setSupertype(superPolicy.getQualifiedName());

        method = superPolicy.newMethod();
        method.setName("aMethod");
        method.setDatatype(Datatype.STRING.getQualifiedName());
        method.setModifier(Modifier.PUBLIC);

        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

    }

    @Test
    public void testValidateInlineAction() throws Exception {
        // check name specified
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        MessageList msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBFElement.MSGCODE_NAME_NOT_SPECIFIED));

        action.setName("action1");
        msgList = action.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBFElement.MSGCODE_NAME_NOT_SPECIFIED));

        // check valid name
        msgList = action.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBFElement.MSGCODE_NAME_NOT_VALID));

        action.setName("action1-");
        msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBFElement.MSGCODE_NAME_NOT_VALID));

        action.setName("execute");
        msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBFElement.MSGCODE_NAME_NOT_VALID));
    }

    @Test
    public void testValidateBusinessFunctionCallAction() throws Exception {

        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(10, 10));
        MessageList msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_SPECIFIED));

        action.setTarget("bf2");
        msgList = action.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_SPECIFIED));

        msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_DOES_NOT_EXIST));

        newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf2");
        msgList = action.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_DOES_NOT_EXIST));

        action.setTarget("start");
        msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBFElement.MSGCODE_NAME_NOT_VALID));
    }

    @Test
    public void testGetReferencedBFQualifiedName() {
        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(10, 10));
        action.setTarget("aTarget");
        assertEquals("aTarget", action.getReferencedBfQualifiedName());

        action = bf.newOpaqueAction(new Point(10, 10));
        action.setTarget("aTarget");
        assertNull(action.getReferencedBfQualifiedName());
    }

    @Test
    public void testGetReferencedBFUnqualifiedName() {
        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(10, 10));

        action.setTarget("a.b.c.target");
        assertEquals("target", action.getReferencedBfUnqualifedName());

        action.setTarget("");
        assertEquals("", action.getReferencedBfUnqualifedName());

        action = bf.newOpaqueAction(new Point(10, 10));
        action.setTarget("a.b.c.target");
        assertNull(action.getReferencedBfUnqualifedName());
    }

    @Test
    public void testGetDisplayString() {
        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(10, 10));
        action.setTarget("aTarget");
        assertEquals("aTarget", action.getDisplayString());

        action = bf.newMethodCallAction(new Point(10, 10));
        action.setTarget("policy");
        action.setExecutableMethodName("aMethod");
        assertEquals("policy:aMethod", action.getDisplayString());

        action = bf.newOpaqueAction(new Point(10, 10));
        action.setName("action");
        assertEquals("action", action.getDisplayString());
    }
}
