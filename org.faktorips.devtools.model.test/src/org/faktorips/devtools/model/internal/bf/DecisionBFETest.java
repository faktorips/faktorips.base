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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.model.bf.IDecisionBFE;
import org.faktorips.devtools.model.bf.IMethodCallBFE;
import org.faktorips.devtools.model.bf.IParameterBFE;
import org.faktorips.devtools.model.bf.Location;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Deprecated
public class DecisionBFETest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private TestContentsChangeListener listener;
    private org.faktorips.devtools.model.bf.IBusinessFunction bf;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        listener = new TestContentsChangeListener();
        ipsProject.getIpsModel().addChangeListener(listener);
        bf = (org.faktorips.devtools.model.bf.IBusinessFunction)newIpsObject(ipsProject,
                BusinessFunctionIpsObjectType.getInstance(), "bf");
    }

    @Test
    public void testPropertiesToXml() throws Exception {
        IDecisionBFE decision = bf.newDecision(new Location(10, 10));
        decision.setDatatype(Datatype.INTEGER.getQualifiedName());
        Document doc = getDocumentBuilder().newDocument();
        Element el = decision.toXml(doc);
        IDecisionBFE loadedDecision = new DecisionBFE(bf, "0");
        loadedDecision.initFromXml(el);
        assertEquals(Datatype.INTEGER.getQualifiedName(), loadedDecision.getDatatype());
    }

    @Test
    public void testInitPropertiesFromXml() {
        Document doc = getTestDocument();
        NodeList nl = doc.getElementsByTagName(IDecisionBFE.XML_TAG);
        Element el = (Element)nl.item(0);
        IDecisionBFE decision = new DecisionBFE(bf, "0");
        decision.initFromXml(el);
        assertEquals(Datatype.STRING.getQualifiedName(), decision.getDatatype());
    }

    @Test
    public void testValidateThis() throws Exception {
        IDecisionBFE decision = bf.newDecision(new Location(10, 10));
        // by default the inital datatype is set to boolean. To test that it is not specified it has
        // to be set to null
        decision.setDatatype(null);
        MessageList msgList = decision.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IDecisionBFE.MSGCODE_DATATYPE_NOT_SPECIFIED));

        decision.setDatatype(Datatype.STRING.getQualifiedName());
        msgList = decision.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IDecisionBFE.MSGCODE_DATATYPE_NOT_SPECIFIED));
        assertNull(msgList.getMessageByCode(IDecisionBFE.MSGCODE_DATATYPE_DOES_NOT_EXIST));

        decision.setDatatype("abc");
        msgList = decision.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IDecisionBFE.MSGCODE_DATATYPE_DOES_NOT_EXIST));

        decision.setDatatype(Datatype.PRIMITIVE_INT.getName());
        msgList = decision.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IDecisionBFE.MSGCODE_DATATYPE_ONLY_NONE_PRIM_VALUEDATATYPE));
    }

    @Test
    public void testValidateThisMethodCallDecision() throws Exception {
        IDecisionBFE decision = bf.newMethodCallDecision(new Location(10, 10));
        MessageList msgList = decision.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_SPECIFIED));

        decision.setTarget("aPolicy");
        msgList = decision.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_SPECIFIED));
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_DOES_NOT_EXIST));

        IPolicyCmptType aPolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "aPolicy");
        IParameterBFE param = bf.newParameter();
        param.setDatatype(aPolicy.getQualifiedName());
        param.setName("aPolicy");

        msgList = decision.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_DOES_NOT_EXIST));
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_NOT_SPECIFIED));

        decision.setExecutableMethodName("aMethod");
        msgList = decision.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_NOT_SPECIFIED));
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

        IMethod method = aPolicy.newMethod();
        method.setName("aMethod");
        method.setDatatype(Datatype.INTEGER.getQualifiedName());

        msgList = decision.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_METHOD_DOES_NOT_EXIST));

        param.setDatatype(Datatype.INTEGER.getQualifiedName());
        msgList = decision.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethodCallBFE.MSGCODE_TARGET_NOT_VALID_TYPE));

        // check that is it is a method call decision the validations for the regular decisions are
        // never called
        decision = bf.newMethodCallDecision(new Location(10, 10));
        msgList = decision.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IDecisionBFE.MSGCODE_DATATYPE_NOT_SPECIFIED));
    }

    @Test
    public void testFindDatatypeMethodCallDecision() {

    }

    @Test
    public void testSetDatatype() {
        listener.clear();
        IDecisionBFE decision = bf.newDecision(new Location(10, 10));
        decision.setDatatype(Datatype.STRING.getQualifiedName());
        assertTrue(listener.getIpsObjectParts().contains(decision));
    }

    @Test
    public void testFindDatatype() throws Exception {
        IDecisionBFE decision = bf.newDecision(new Location(10, 10));
        decision.setDatatype(Datatype.STRING.getQualifiedName());
        assertEquals(Datatype.STRING, decision.findDatatype(ipsProject));
        decision.setDatatype("abc");
        assertNull(decision.findDatatype(ipsProject));
    }

}
