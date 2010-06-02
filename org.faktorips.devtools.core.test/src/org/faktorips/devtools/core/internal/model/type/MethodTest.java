/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class MethodTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IType type;
    private IMethod method;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Product");
        method = type.newMethod();
    }

    public void testGetSignatureString() {
        method.setName("calc");
        assertEquals("calc()", method.getSignatureString());

        method.newParameter("base.Vertrag", "vertrag");
        assertEquals("calc(base.Vertrag)", method.getSignatureString());

        method.newParameter("Integer", "zahlweise");
        assertEquals("calc(base.Vertrag, Integer)", method.getSignatureString());
    }

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

    public void testFindMethodBySignature() throws CoreException {
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

    /*
     * Class under test for Element toXml(Document)
     */
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

    public void testNewParameter() {
        IParameter param = method.newParameter();
        assertEquals(1, method.getParameters().length);
        assertEquals(param, method.getParameters()[0]);
        assertTrue(method.getIpsSrcFile().isDirty());
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.type.Method#setName(java.lang.String)}.
     */
    public void testSetName() {
        testPropertyAccessReadWrite(Method.class, IIpsElement.PROPERTY_NAME, method, "calcPremium");
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.type.Method#setDatatype(java.lang.String)}.
     */
    public void testSetDatatype() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_DATATYPE, method, "Integer");
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.type.Method#setAbstract(boolean)}.
     */
    public void testSetAbstract() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_ABSTRACT, method, Boolean.TRUE);
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.internal.model.type.Method#setModifier(org.faktorips.devtools.core.model.ipsobject.Modifier)}
     * .
     */
    public void testSetModifier() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_MODIFIER, method, Modifier.PUBLIC);
    }

    public void testIsSameSignature() {
        method.setName("calc");
        method.setDatatype("void");
        IParameter param0 = method.newParameter();
        param0.setName("p1");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p2");
        param1.setDatatype("Money");

        IMethod other = type.newMethod();
        other.setDatatype("Money");
        other.setName("calc");
        IParameter otherParam0 = other.newParameter();
        otherParam0.setName("x");
        otherParam0.setDatatype("Decimal");
        IParameter otherParam1 = other.newParameter();
        otherParam1.setName("y");
        otherParam1.setDatatype("Money");

        // ok case
        assertTrue(method.isSameSignature(other));

        // different name
        other.setName("differentName");
        assertFalse(method.isSameSignature(other));

        // different parameter type
        other.setName("calc"); // make names equals again
        assertTrue(method.isSameSignature(other)); // and test it
        otherParam1.setDatatype("int");
        assertFalse(method.isSameSignature(other));

        // different number of parameters
        other.newParameter();
        assertFalse(method.isSameSignature(other));
    }

    public void testOverrides() throws CoreException {
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

    public void testValidateMultipleParameterNames() throws CoreException {
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
        assertNotNull(msgList.getMessageByCode(IMethod.MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME));

        p2.setName("param2");
        msgList = method.validate(ipsProject);
        assertNull(msgList.getMessageByCode(IMethod.MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME));

    }

    public void testFindOverridingMethod() throws CoreException {
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

        IMethod result = thisMethod.findOverridingMethod(thisType, ipsProject);
        assertEquals(null, result);

        result = thisMethod.findOverridingMethod(superType, ipsProject);
        assertEquals(null, result);

        result = thisMethod.findOverridingMethod(subType, ipsProject);
        assertEquals(subMethod, result);

    }

    public void testFindOverriddenMethod() throws CoreException {
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

}
