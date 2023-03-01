/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.method.Method;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Jan Ortmann
 */
public class MethodTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType type;
    private IMethod method;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Product");
        method = type.newMethod();
    }

    @Test
    public void testGetMethodBySignature() {
        assertNull(type.getMethod("calc()"));

        method.setName("calc");

        IMethod method2 = type.newMethod();
        method2.setName("calc");
        method2.newParameter("base.Vertrag", "vertrag");
        method2.newParameter("Integer", "i");

        assertEquals(method, type.getMethod("calc()"));
        assertEquals(method2, type.getMethod("calc(base.Vertrag, Integer)"));
        assertNull(type.getMethod("calc(base.Vertrag, String)"));
        assertNull(type.getMethod("unknown()"));
    }

    @Test
    public void testFindMethodBySignature() {
        assertNull(type.findMethod("calc()", ipsProject));

        method.setName("calc");

        IType supertype = newProductCmptType(ipsProject, "Supertype");
        type.setSupertype(supertype.getQualifiedName());
        IMethod method2 = type.newMethod();
        method2.setName("calc");
        method2.newParameter("base.Vertrag", "vertrag");
        method2.newParameter("Integer", "i");

        assertEquals(method, type.findMethod("calc()", ipsProject));
        assertEquals(method2, type.findMethod("calc(base.Vertrag, Integer)", ipsProject));
        assertNull(type.findMethod("calc(base.Vertrag, String)", ipsProject));
        assertNull(type.findMethod("unknown()", ipsProject));
    }

    @Test
    public void testInitFromXml() {
        Element docElement = getTestDocument().getDocumentElement();

        method.initFromXml(XmlUtil.getElement(docElement, "Method", 0));
        assertEquals("42", method.getId());
        assertEquals("calcPremium", method.getName());
        assertEquals("Money", method.getDatatype());
        assertEquals(Modifier.PUBLIC, method.getModifier());
        assertTrue(method.isAbstract());
        IParameter[] params = method.getParameters();
        assertEquals(2, params.length);
        assertEquals("p1", params[0].getName());
        assertEquals("Money", params[0].getDatatype());
        assertEquals("p2", params[1].getName());
        assertEquals("Decimal", params[1].getDatatype());

        method.initFromXml(XmlUtil.getElement(docElement, "Method", 1));
        assertEquals(0, method.getNumOfParameters());
    }

    @Test
    public void testDuplicateID() {
        Element docElement = getTestDocument().getDocumentElement();

        method.initFromXml(XmlUtil.getElement(docElement, "Method", 0));

        Element element = method.toXml(newDocument());

        String duplicateID = checkForDuplicateID(element);
        if (duplicateID != null) {
            fail("Duplicate ID: " + duplicateID);
        }
    }

    private String checkForDuplicateID(Element element) {
        Set<String> ids = new HashSet<>();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (!(node instanceof Element nodeElement)) {
                continue;
            }
            System.out.println(nodeElement.getNodeName());

            if (!nodeElement.hasAttribute("id")) {
                System.out.println("\tno id");
                continue;
            }
            String id = nodeElement.getAttribute("id");
            System.out.println("\tid: " + id);

            if (ids.contains(id)) {
                System.out.println("BINGO");
                return id + "(" + nodeElement + ")";
            } else {
                ids.add(id);
            }
        }
        return null;
    }

    @Test
    public void testToXmlDocument() {
        method = type.newMethod(); // => id=1, because it's the second method
        method.setName("getAge");
        method.setModifier(Modifier.PUBLIC);
        method.setDatatype("Decimal");
        method.setAbstract(true);
        IParameter param0 = method.newParameter();
        param0.setName("p0");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p1");
        param1.setDatatype("Money");

        Element element = method.toXml(newDocument());

        IMethod copy = type.newMethod();
        copy.initFromXml(element);
        IParameter[] copyParams = copy.getParameters();
        assertEquals(method.getId(), copy.getId());
        assertEquals("getAge", copy.getName());
        assertEquals("Decimal", copy.getDatatype());
        assertEquals(Modifier.PUBLIC, copy.getModifier());
        assertTrue(copy.isAbstract());
        assertEquals(2, copyParams.length);
        assertEquals("p0", copyParams[0].getName());
        assertEquals("Decimal", copyParams[0].getDatatype());
        assertEquals("p1", copyParams[1].getName());
        assertEquals("Money", copyParams[1].getDatatype());

    }

    @Test
    public void testSetAbstract() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_ABSTRACT, method, Boolean.TRUE);
    }

    @Test
    public void testOverrides() {
        method.setName("calc");
        method.setDatatype("void");
        IParameter param0 = method.newParameter();
        param0.setName("p1");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p2");
        param1.setDatatype("Money");

        IType otherType = newProductCmptType(ipsProject, "otherType");
        IMethod otherTypeMethod = otherType.newMethod();
        otherTypeMethod.setName("calc");
        otherTypeMethod.setDatatype("void");
        IParameter OtherParam0 = otherTypeMethod.newParameter();
        OtherParam0.setName("p1");
        OtherParam0.setDatatype("Decimal");
        IParameter OtherParam1 = otherTypeMethod.newParameter();
        OtherParam1.setName("p2");
        OtherParam1.setDatatype("Money");

        assertTrue(method.isSameSignature(otherTypeMethod));

        assertFalse(method.overrides(otherTypeMethod));
        assertFalse(otherTypeMethod.overrides(method));

        otherType.setSupertype(type.getQualifiedName());
        assertFalse(method.overrides(otherTypeMethod));
        assertTrue(otherTypeMethod.overrides(method));
    }

    @Test
    public void testValidate() throws Exception {
        IType pcType = newPolicyCmptType(ipsProject, "aType");
        method = pcType.newMethod();
        method.setModifier(Modifier.PUBLIC);
        method.setName("calculate");
        method.setDatatype("String");
        method.newParameter(Datatype.STRING.getName(), "strategy");
        method.newParameter(Datatype.INTEGER.getName(), "index");

        MessageList msgList = method.validate(ipsProject);
        assertTrue(msgList.isEmpty());

        method = pcType.newMethod();
        method.setModifier(Modifier.PUBLIC);
        method.setName("calculate");
        method.setDatatype("String");
        method.newParameter(Datatype.STRING.getName(), "strategy");
        method.newParameter(Datatype.INTEGER.getName(), "index");

        msgList = method.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethod.MSGCODE_DUBLICATE_SIGNATURE));
    }

    @Test
    public void testValidateInconsistentReturnType() throws Exception {
        IType pcType = newPolicyCmptType(ipsProject, "AType");
        method = pcType.newMethod();
        method.setModifier(Modifier.PUBLIC);
        method.setName("calculate");
        method.setDatatype("String");
        method.newParameter(Datatype.STRING.getName(), "strategy");
        method.newParameter(Datatype.INTEGER.getName(), "index");

        MessageList msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethod.MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE));

        IType superType = newPolicyCmptType(ipsProject, "SuperType");
        IMethod overridden = superType.newMethod();
        overridden.setModifier(Modifier.PUBLIC);
        overridden.setName("calculate");
        overridden.setDatatype("String");
        overridden.newParameter(Datatype.STRING.getName(), "strategy");
        overridden.newParameter(Datatype.INTEGER.getName(), "index");

        msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethod.MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE));

        pcType.setSupertype(superType.getQualifiedName());
        msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethod.MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE));

        method.setDatatype("int");
        msgList = method.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethod.MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE));

        // test, if the datatype in the supertype is invalid, the error message is still generated.
        overridden.setDatatype("unknownType");
        msgList = method.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethod.MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE));

        // if the datatype in the method itself is invalid, no message should be generated.
        method.setDatatype("unknown");
        msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethod.MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE));
    }

    @Test
    public void testValidateModifierOverriddenMethod() throws Exception {
        IType pcType = newPolicyCmptType(ipsProject, "AType");
        method = pcType.newMethod();
        method.setModifier(Modifier.PUBLISHED);
        method.setName("calculate");
        method.setDatatype("String");
        method.setAbstract(true);

        MessageList msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethod.MSGCODE_MODIFIER_NOT_EQUAL));

        IType superType = newPolicyCmptType(ipsProject, "SuperType");
        pcType.setSupertype(superType.getQualifiedName());
        IMethod overridden = superType.newMethod();
        overridden.setModifier(Modifier.PUBLISHED);
        overridden.setName("calculate");
        overridden.setDatatype("String");

        msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethod.MSGCODE_MODIFIER_NOT_EQUAL));

        overridden.setModifier(Modifier.PUBLIC);
        msgList = method.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethod.MSGCODE_MODIFIER_NOT_EQUAL));

        method.setModifier(Modifier.PUBLIC);
        msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethod.MSGCODE_MODIFIER_NOT_EQUAL));

        overridden.setModifier(Modifier.PUBLISHED);
        msgList = method.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IMethod.MSGCODE_MODIFIER_NOT_EQUAL));
    }

    @Test
    public void testValidateMultipleParameterNames() {
        IType pcType = newPolicyCmptType(ipsProject, "aType");
        method = pcType.newMethod();
        method.setModifier(Modifier.PUBLIC);
        method.setName("calculate");
        method.setDatatype("String");
        method.newParameter(Datatype.STRING.getName(), "param1");
        IParameter p2 = method.newParameter(Datatype.INTEGER.getName(), "param2");

        MessageList msgList = method.validate(ipsProject);
        assertTrue(msgList.isEmpty());

        p2.setName("param1");
        msgList = method.validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IBaseMethod.MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME));

        p2.setName("param2");
        msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IBaseMethod.MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME));

    }

    @Test
    public void testFindOverridingMethod() {
        IType superType = newPolicyCmptType(ipsProject, "superType");
        IType thisType = newPolicyCmptType(ipsProject, "thisType");
        thisType.setSupertype(superType.getQualifiedName());
        IType subType = newPolicyCmptType(ipsProject, "subType");
        subType.setSupertype(thisType.getQualifiedName());

        IMethod superMethod = superType.newMethod();
        IMethod thisMethod = thisType.newMethod();
        assertTrue(thisMethod.overrides(superMethod));
        IMethod subMethod = subType.newMethod();
        assertTrue(subMethod.overrides(thisMethod));

        IMethod result = thisMethod.findOverridingMethod(superType, ipsProject);
        assertEquals(null, result);

        result = thisMethod.findOverridingMethod(subType, ipsProject);
        assertEquals(subMethod, result);

        result = thisMethod.findOverridingMethod(thisType, ipsProject);
        assertEquals(null, result);

    }

    @Test
    public void testFindOverriddenMethod() {
        IType superSuperType = newPolicyCmptType(ipsProject, "superSuperType");
        IType superType = newPolicyCmptType(ipsProject, "superType");
        superType.setSupertype(superSuperType.getQualifiedName());
        IType thisType = newPolicyCmptType(ipsProject, "thisType");
        thisType.setSupertype(superType.getQualifiedName());

        IMethod superSuperMethod = superSuperType.newMethod();
        IMethod superMethod = superType.newMethod();
        assertTrue(superMethod.overrides(superSuperMethod));

        IMethod thisMethod = thisType.newMethod();
        assertTrue(thisMethod.overrides(superMethod));

        IMethod result = thisMethod.findOverriddenMethod(ipsProject);
        assertEquals(superMethod, result);

        result = superMethod.findOverriddenMethod(ipsProject);
        assertEquals(superSuperMethod, result);

        result = superSuperMethod.findOverriddenMethod(ipsProject);
        assertEquals(null, result);
    }

    @Test
    public void testGetDescriptionFromThisOrSuper() {
        IType superType = newPolicyCmptType(ipsProject, "superType");
        IType subType = newPolicyCmptType(ipsProject, "thisType");
        subType.setSupertype(superType.getQualifiedName());

        IMethod superMethod = superType.newMethod();
        superMethod.setDescriptionText(Locale.ENGLISH, "english description");
        IMethod subMethod = subType.newMethod();

        assertEquals("english description", subMethod.getDescriptionTextFromThisOrSuper(Locale.ENGLISH));

        subMethod.setDescriptionText(Locale.ENGLISH, "overwritten description");
        assertEquals("overwritten description",
                subMethod.getDescriptionTextFromThisOrSuper(Locale.ENGLISH));
    }

    @Test
    public void testGetLabelFromThisOrSuper() {
        IType superType = newPolicyCmptType(ipsProject, "superType");
        IType subType = newPolicyCmptType(ipsProject, "thisType");
        subType.setSupertype(superType.getQualifiedName());

        IMethod superMethod = superType.newMethod();
        superMethod.setLabelValue(Locale.ENGLISH, "english label");
        IMethod subMethod = subType.newMethod();

        assertEquals("english label", subMethod.getLabelValueFromThisOrSuper(Locale.ENGLISH));

        subMethod.setLabelValue(Locale.ENGLISH, "overwritten label");
        assertEquals("overwritten label",
                subMethod.getLabelValueFromThisOrSuper(Locale.ENGLISH));
    }
}
