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

package org.faktorips.devtools.core.internal.model.pctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PolicyCmptTypeAttributeTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder;
    private IIpsSrcFile ipsSrcFile;
    private PolicyCmptType pcType;
    private IPolicyCmptTypeAttribute attribute;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
        ipsRootFolder = ipsProject.getIpsPackageFragmentRoots()[0];
        ipsFolder = ipsRootFolder.createPackageFragment("products.folder", true, null);
        ipsSrcFile = ipsFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)ipsSrcFile.getIpsObject();
        attribute = pcType.newPolicyCmptTypeAttribute();
    }

    @Test
    public void testValidateComputationMethodHasDifferentDatatype() throws CoreException {
        IProductCmptType productType = newProductCmptType(ipsProject, "TestProduct");
        pcType.setConfigurableByProductCmptType(true);
        pcType.setProductCmptType(productType.getQualifiedName());
        IMethod method = productType.newMethod();
        method.setName("calcPremium");

        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature(method.getSignatureString());
        attribute.setProductRelevant(true);

        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList result = attribute.validate(ipsProject);
        assertNotNull(result
                .getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE));

        method.setDatatype(attribute.getDatatype());
        result = attribute.validate(ipsProject);
        assertNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE));
    }

    @Test
    public void testValidateComputationMethodNotSpecified() throws CoreException {
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature("");
        attribute.setProductRelevant(true);

        MessageList result = attribute.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED));

        attribute.setComputationMethodSignature("calc()");
        result = attribute.validate(ipsProject);
        assertNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED));
    }

    @Test
    public void testValidateComputationMethodDoesNotExist() throws CoreException {
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature("");
        attribute.setProductRelevant(true);

        MessageList result = attribute.validate(ipsProject);
        assertNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST));

        attribute.setComputationMethodSignature("calcPremium(TestPolicy)");
        result = attribute.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST));

        IProductCmptType productType = newProductCmptType(ipsProject, "TestProduct");
        pcType.setConfigurableByProductCmptType(true);
        pcType.setProductCmptType(productType.getQualifiedName());
        IMethod method = productType.newMethod();
        method.setName("calcPremium");
        result = attribute.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST));

        method.newParameter("TestPolicy", "policy");
        result = attribute.validate(ipsProject);
        assertNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST));
    }

    @Test
    public void testFindComputationMethodSignature() throws CoreException {
        assertNull(attribute.findComputationMethod(ipsProject));

        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature("calcPremium(TestPolicy)");

        IProductCmptType productType = newProductCmptType(ipsProject, "TestProduct");
        pcType.setConfigurableByProductCmptType(true);
        pcType.setProductCmptType(productType.getQualifiedName());
        assertNull(attribute.findComputationMethod(ipsProject));

        IMethod method = productType.newMethod();
        method.setName("calcPremium");
        assertNull(attribute.findComputationMethod(ipsProject));

        method.newParameter("TestPolicy", "policy");

        assertEquals(method, attribute.findComputationMethod(ipsProject));
    }

    @Test
    public void testComputationMethodSignature() {
        testPropertyAccessReadWrite(PolicyCmptTypeAttribute.class,
                IPolicyCmptTypeAttribute.PROPERTY_COMPUTATION_METHOD_SIGNATURE, attribute, "calcThis");
    }

    @Test
    public void testFindOverwrittenAttribute() throws CoreException {
        attribute.setName("a");
        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "Supertype");
        IPolicyCmptType supersupertype = newPolicyCmptType(ipsProject, "SuperSupertype");
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        assertNull(attribute.findOverwrittenAttribute(ipsProject));

        IPolicyCmptTypeAttribute aInSupertype = supersupertype.newPolicyCmptTypeAttribute();
        aInSupertype.setName("a");

        assertEquals(aInSupertype, attribute.findOverwrittenAttribute(ipsProject));

        // cycle in type hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());
        assertEquals(aInSupertype, attribute.findOverwrittenAttribute(ipsProject));

        aInSupertype.delete();
        assertNull(attribute.findOverwrittenAttribute(ipsProject)); // this should not return a
        // itself!
    }

    @Test
    public void testRemove() {
        attribute.delete();
        assertEquals(0, pcType.getPolicyCmptTypeAttributes().length);
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testSetDatatype() {
        attribute.setDatatype("Money");
        assertEquals("Money", attribute.getDatatype());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testSetComputed() {
        attribute.setProductRelevant(true);
        assertEquals(true, attribute.isProductRelevant());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        NodeList nl = root.getElementsByTagName("Attribute");
        attribute.initFromXml((Element)nl.item(0));
        assertEquals("42", attribute.getId());
        assertEquals("premium", attribute.getName());
        assertEquals("computePremium", attribute.getComputationMethodSignature());
        assertEquals("money", attribute.getDatatype());
        assertFalse(attribute.isProductRelevant());
        assertEquals(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL, attribute.getAttributeType());
        assertEquals("42EUR", attribute.getDefaultValue());
        assertNotNull(attribute.getValueSet());
        assertFalse(attribute.isOverwrite());

        attribute.initFromXml((Element)nl.item(1));
        assertEquals("2", attribute.getId());
        assertNull(attribute.getDefaultValue());
        assertNotNull(attribute.getValueSet());
        assertEquals(EnumValueSet.class, attribute.getValueSet().getClass());

        attribute.initFromXml((Element)nl.item(2));
        assertTrue(attribute.isOverwrite());
    }

    @Test
    public void testToXml() {
        attribute = pcType.newPolicyCmptTypeAttribute(); // => id=1 as this is the type's 2
        // attribute
        attribute.setName("age");
        attribute.setDatatype("decimal");
        attribute.setComputationMethodSignature("computePremium");
        attribute.setProductRelevant(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("18");
        attribute.setOverwrite(false);
        attribute.setValueSetType(ValueSetType.RANGE);
        RangeValueSet set = (RangeValueSet)attribute.getValueSet();
        set.setLowerBound("unten");
        set.setUpperBound("oben");
        set.setStep("step");
        Element element = attribute.toXml(newDocument());

        IPolicyCmptTypeAttribute copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertEquals(attribute.getId(), copy.getId());
        assertEquals("age", copy.getName());
        assertEquals("decimal", copy.getDatatype());
        assertEquals("computePremium", copy.getComputationMethodSignature());
        assertTrue(copy.isProductRelevant());
        assertFalse(copy.isOverwrite());
        assertEquals(AttributeType.CONSTANT, copy.getAttributeType());
        assertEquals("18", copy.getDefaultValue());
        assertEquals("unten", ((IRangeValueSet)copy.getValueSet()).getLowerBound());
        assertEquals("oben", ((IRangeValueSet)copy.getValueSet()).getUpperBound());
        assertEquals("step", ((IRangeValueSet)copy.getValueSet()).getStep());

        // Nun ein Attribut mit GenericEnumvalueset testen.
        attribute.setName("age");
        attribute.setDatatype("decimal");
        attribute.setProductRelevant(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("18");
        attribute.setValueSetType(ValueSetType.ENUM);
        EnumValueSet set2 = (EnumValueSet)attribute.getValueSet();
        set2.addValue("a");
        set2.addValue("b");
        set2.addValue("x");

        element = attribute.toXml(newDocument());
        copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertEquals("age", attribute.getName());
        assertEquals("decimal", attribute.getDatatype());
        assertTrue(attribute.isProductRelevant());
        assertEquals(AttributeType.CONSTANT, attribute.getAttributeType());
        assertEquals("18", attribute.getDefaultValue());
        String[] vekt = ((IEnumValueSet)copy.getValueSet()).getValues();
        assertEquals("a", vekt[0]);
        assertEquals("b", vekt[1]);
        assertEquals("x", vekt[2]);

        // and now an attribute which overwrites
        attribute.setOverwrite(true);
        element = attribute.toXml(newDocument());
        copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertTrue(attribute.isOverwrite());
    }

    @Test
    public void testValidate_productRelevant() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setProductRelevant(true);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));

        pcType.setConfigurableByProductCmptType(false);
        ml = attribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    }

    @Test
    public void testValidate_nothingToOverwrite() throws Exception {
        attribute.setName("name");

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));

        attribute.setOverwrite(true);
        ml = attribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "sup.SuperType");
        IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute();
        superAttr.setName("name");
        pcType.setSupertype(supertype.getQualifiedName());

        ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentType() throws Exception {
        attribute.setName("name");
        attribute.setDatatype("String");
        attribute.setOverwrite(true);
        attribute.setAttributeType(AttributeType.CHANGEABLE);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE));

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "sup.SuperType");
        pcType.setSupertype(supertype.getQualifiedName());
        IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);

        ml = attribute.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE));

        attribute.setAttributeType(superAttr.getAttributeType());
        ml = attribute.validate(ipsProject);
        assertNull(ml.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE));
    }
}
