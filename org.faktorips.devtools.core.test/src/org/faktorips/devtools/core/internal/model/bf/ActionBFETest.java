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

package org.faktorips.devtools.core.internal.model.bf;

import org.eclipse.draw2d.geometry.Point;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IActionBFE;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IParameterBFE;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ActionBFETest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private TestContentsChangeListener listener;
    private IBusinessFunction bf;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        listener = new TestContentsChangeListener();
        ipsProject.getIpsModel().addChangeListener(listener);
        bf = (IBusinessFunction)newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf");

    }

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

    public void testInitPropertiesFromXml() {
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        action.initFromXml((Element)getTestDocument().getDocumentElement().getElementsByTagName(IActionBFE.XML_TAG)
                .item(0));
        assertEquals("aTarget", action.getTarget());
        assertEquals("aMethod", action.getExecutableMethodName());
    }

    public void testGetExecutableMethodName() {
        IActionBFE actionBFE = bf.newOpaqueAction(new Point(10, 10));
        listener.clear();
        actionBFE.setExecutableMethodName("aMethod");
        assertEquals("aMethod", actionBFE.getExecutableMethodName());
        assertTrue(listener.getIpsObjectParts().contains(actionBFE));
    }

    public void testGetTarget() {
        IActionBFE actionBFE = bf.newOpaqueAction(new Point(10, 10));
        listener.clear();
        actionBFE.setTarget("aTarget");
        assertEquals("aTarget", actionBFE.getTarget());
        assertTrue(listener.getIpsObjectParts().contains(actionBFE));
    }

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

    public void testValidateTargetNotSpecified() throws Exception {
        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));

        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_NOT_SPECIFIED));

        actionBFE.setTarget("policy");
        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_NOT_SPECIFIED));
    }

    public void testValidateTargetDoesNotExist() throws Exception {
        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        actionBFE.setTarget("p");
        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_DOES_NOT_EXIST));

        IParameterBFE p = bf.newParameter();
        p.setName("p");
        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_DOES_NOT_EXIST));
    }

    public void testValidateTargetNotValidType() throws Exception {

        IParameterBFE p = bf.newParameter();
        p.setName("p");
        p.setDatatype(Datatype.STRING.getQualifiedName());

        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        actionBFE.setExecutableMethodName("aMethod");
        actionBFE.setTarget("p");
        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_NOT_VALID_TYPE));

        IPolicyCmptType policy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "policy");
        p.setDatatype(policy.getQualifiedName());
        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_NOT_VALID_TYPE));

    }

    public void testValidateMethodNotSpecified() throws Exception {

        IParameterBFE p = bf.newParameter();
        p.setName("p");

        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        actionBFE.setTarget("p");

        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_METHOD_NOT_SPECIFIED));

        actionBFE.setExecutableMethodName("aMethod");
        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_METHOD_NOT_SPECIFIED));

        actionBFE.setExecutableMethodName("end");
        msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_NAME_NOT_VALID));
    }

    public void testValidateMethodDoesNotExist() throws Exception {
        IPolicyCmptType policy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "policy");

        IParameterBFE p = bf.newParameter();
        p.setName("p");
        p.setDatatype(policy.getQualifiedName());

        IActionBFE actionBFE = bf.newMethodCallAction(new Point(10, 10));
        actionBFE.setTarget("p");
        actionBFE.setExecutableMethodName("aMethod");

        MessageList msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

        IMethod method = policy.newMethod();
        method.setName("aMethod");
        method.setDatatype(Datatype.STRING.getQualifiedName());
        method.setModifier(Modifier.PUBLIC);

        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

        method.delete();
        msgList = actionBFE.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

        IPolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "superPolicy");
        policy.setSupertype(superPolicy.getQualifiedName());

        method = superPolicy.newMethod();
        method.setName("aMethod");
        method.setDatatype(Datatype.STRING.getQualifiedName());
        method.setModifier(Modifier.PUBLIC);

        msgList = actionBFE.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

    }

    public void testValidateInlineAction() throws Exception {
        // check name specified
        IActionBFE action = bf.newOpaqueAction(new Point(10, 10));
        MessageList msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_NAME_NOT_SPECIFIED));

        action.setName("action1");
        msgList = action.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_NAME_NOT_SPECIFIED));

        // check valid name
        msgList = action.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_NAME_NOT_VALID));

        action.setName("action1-");
        msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_NAME_NOT_VALID));

        action.setName("execute");
        msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_NAME_NOT_VALID));
    }

    public void testValidateBusinessFunctionCallAction() throws Exception {

        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(10, 10));
        MessageList msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_NOT_SPECIFIED));

        action.setTarget("bf2");
        msgList = action.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_NOT_SPECIFIED));

        msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_DOES_NOT_EXIST));

        newIpsObject(ipsProject, BusinessFunctionIpsObjectType.getInstance(), "bf2");
        msgList = action.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IActionBFE.MSGCODE_TARGET_DOES_NOT_EXIST));

        action.setTarget("start");
        msgList = action.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IActionBFE.MSGCODE_NAME_NOT_VALID));
    }

    public void testGetReferencedBFQualifiedName() {
        IActionBFE action = bf.newBusinessFunctionCallAction(new Point(10, 10));
        action.setTarget("aTarget");
        assertEquals("aTarget", action.getReferencedBfQualifiedName());

        action = bf.newOpaqueAction(new Point(10, 10));
        action.setTarget("aTarget");
        assertNull(action.getReferencedBfQualifiedName());
    }

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
