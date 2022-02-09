/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.productcmpttype.ChangingOverTimePropertyValidator;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
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
    public void testValidateComputationMethodHasDifferentDatatype() {
        IProductCmptType productType = newProductCmptType(ipsProject, "TestProduct");
        pcType.setConfigurableByProductCmptType(true);
        pcType.setProductCmptType(productType.getQualifiedName());
        IMethod method = productType.newMethod();
        method.setName("calcPremium");

        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature(method.getSignatureString());
        attribute.setValueSetConfiguredByProduct(true);

        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList result = attribute.validate(ipsProject);
        assertNotNull(
                result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE));

        method.setDatatype(attribute.getDatatype());
        result = attribute.validate(ipsProject);
        assertNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE));
    }

    @Test
    public void testValidateComputationMethodNotSpecified() {
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature("");
        attribute.setValueSetConfiguredByProduct(true);

        MessageList result = attribute.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED));

        attribute.setComputationMethodSignature("calc()");
        result = attribute.validate(ipsProject);
        assertNull(result.getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED));
    }

    @Test
    public void testValidateComputationMethodDoesNotExist() {
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature("");
        attribute.setValueSetConfiguredByProduct(true);

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
    public void testFindComputationMethodSignature() {
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
    public void testFindOverwrittenAttribute() {
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
        assertNull(attribute.findOverwrittenAttribute(ipsProject));
        // this should not return a itself!
    }

    @Test
    public void testRemove() {
        attribute.delete();
        assertEquals(0, pcType.getPolicyCmptTypeAttributes().size());
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
        attribute.setValueSetConfiguredByProduct(true);
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
        assertTrue(attribute.isChangingOverTime());

        attribute.initFromXml((Element)nl.item(1));
        assertEquals("2", attribute.getId());
        assertNull(attribute.getDefaultValue());
        assertNotNull(attribute.getValueSet());
        assertEquals(EnumValueSet.class, attribute.getValueSet().getClass());
        assertFalse(attribute.isChangingOverTime());

        attribute.initFromXml((Element)nl.item(2));
        assertTrue(attribute.isOverwrite());
        assertTrue(attribute.isChangingOverTime());

        // legacy productRelevant
        attribute.initFromXml((Element)nl.item(3));
        assertTrue(attribute.isProductRelevant());
        assertTrue(attribute.isValueSetConfiguredByProduct());
        assertFalse(attribute.isRelevanceConfiguredByProduct());

        attribute.initFromXml((Element)nl.item(4));
        assertTrue(attribute.isProductRelevant());
        assertTrue(attribute.isValueSetConfiguredByProduct());
        assertFalse(attribute.isRelevanceConfiguredByProduct());

        attribute.initFromXml((Element)nl.item(5));
        assertTrue(attribute.isProductRelevant());
        assertFalse(attribute.isValueSetConfiguredByProduct());
        assertTrue(attribute.isRelevanceConfiguredByProduct());

        attribute.initFromXml((Element)nl.item(6));
        assertTrue(attribute.isProductRelevant());
        assertTrue(attribute.isValueSetConfiguredByProduct());
        assertTrue(attribute.isRelevanceConfiguredByProduct());

        assertFalse(attribute.isGenericValidationEnabled());

        attribute.initFromXml((Element)nl.item(7));
        assertTrue(attribute.isGenericValidationEnabled());
    }

    @Test
    public void testToXml() {
        attribute = pcType.newPolicyCmptTypeAttribute();
        // => id=1 as this is the type's 2nd attribute
        attribute.setName("age");
        attribute.setDatatype("decimal");
        attribute.setComputationMethodSignature("computePremium");
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("18");
        attribute.setOverwrite(false);
        attribute.setValueSetType(ValueSetType.RANGE);
        attribute.setCategory("foo");
        attribute.setChangingOverTime(false);
        IRangeValueSet set = (IRangeValueSet)attribute.getValueSet();
        set.setLowerBound("unten");
        set.setUpperBound("oben");
        set.setStep("step");
        Element element = attribute.toXml(newDocument());

        assertFalse(element.hasAttribute(IPolicyCmptTypeAttribute.PROPERTY_GENERIC_VALIDATION));
        IPolicyCmptTypeAttribute copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertEquals(attribute.getId(), copy.getId());
        assertEquals("age", copy.getName());
        assertEquals("decimal", copy.getDatatype());
        assertEquals("computePremium", copy.getComputationMethodSignature());
        assertTrue(copy.isProductRelevant());
        assertTrue(copy.isValueSetConfiguredByProduct());
        assertFalse(copy.isRelevanceConfiguredByProduct());
        assertFalse(copy.isOverwrite());
        assertFalse(copy.isGenericValidationEnabled());
        assertEquals(AttributeType.CONSTANT, copy.getAttributeType());
        assertEquals("18", copy.getDefaultValue());
        assertEquals("unten", ((IRangeValueSet)copy.getValueSet()).getLowerBound());
        assertEquals("oben", ((IRangeValueSet)copy.getValueSet()).getUpperBound());
        assertEquals("step", ((IRangeValueSet)copy.getValueSet()).getStep());
        assertEquals("foo", copy.getCategory());
        assertFalse(copy.isChangingOverTime());

        // Nun ein Attribut mit GenericEnumvalueset testen.
        attribute.setName("age");
        attribute.setDatatype("decimal");
        attribute.setValueSetConfiguredByProduct(false);
        attribute.setRelevanceConfiguredByProduct(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("18");
        attribute.setValueSetType(ValueSetType.ENUM);
        attribute.setGenericValidationEnabled(true);
        IEnumValueSet set2 = (IEnumValueSet)attribute.getValueSet();
        set2.addValue("a");
        set2.addValue("b");
        set2.addValue("x");

        element = attribute.toXml(newDocument());
        copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertEquals("age", attribute.getName());
        assertEquals("decimal", attribute.getDatatype());
        assertTrue(attribute.isProductRelevant());
        assertFalse(copy.isValueSetConfiguredByProduct());
        assertTrue(copy.isRelevanceConfiguredByProduct());
        assertTrue(copy.isGenericValidationEnabled());
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
    public void testValidateProductRelevant_ValueSetConfiguredByProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setValueSetConfiguredByProduct(true);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));

        pcType.setConfigurableByProductCmptType(false);
        ml = attribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    }

    @Test
    public void testValidateProductRelevant_RelevanceConfiguredByProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setRelevanceConfiguredByProduct(true);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));

        pcType.setConfigurableByProductCmptType(false);
        ml = attribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    }

    @Test
    public void testValidateProductRelevant_DerivedValueSet_ValueSetConfiguredByProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setValueSetType(ValueSetType.DERIVED);

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAttribute.MSGCODE_PRODUCT_RELEVANT_ATTRIBUTE_CAN_NOT_HAVE_DERIVED_VALUE_SET));

        attribute.setValueSetConfiguredByProduct(true);

        ml = attribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAttribute.MSGCODE_PRODUCT_RELEVANT_ATTRIBUTE_CAN_NOT_HAVE_DERIVED_VALUE_SET));
    }

    @Test
    public void testValidateProductRelevant_DerivedValueSet_RelevanceConfiguredByProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setValueSetType(ValueSetType.DERIVED);
        attribute.setName("derived");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = attribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAttribute.MSGCODE_PRODUCT_RELEVANT_ATTRIBUTE_CAN_NOT_HAVE_DERIVED_VALUE_SET));

        attribute.setRelevanceConfiguredByProduct(true);

        ml = attribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(
                        IPolicyCmptTypeAttribute.MSGCODE_PRODUCT_RELEVANT_ATTRIBUTE_CAN_NOT_HAVE_DERIVED_VALUE_SET));
    }

    @Test
    public void testValidateNothingToOverwrite() throws Exception {
        attribute.setName("name");

        MessageList ml = attribute.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));

        attribute.setOverwrite(true);
        ml = attribute.validate(ipsProject);
        assertThat(ml, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "sup.SuperType");
        IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute();
        superAttr.setName("name");
        pcType.setSupertype(supertype.getQualifiedName());

        ml = attribute.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_NOTHING_TO_OVERWRITE));
    }

    @Test
    public void testValidateOverwrittenAttributeHasDifferentType() throws Exception {
        attribute.setName("name");
        attribute.setDatatype("String");
        attribute.setOverwrite(true);
        attribute.setAttributeType(AttributeType.CHANGEABLE);

        MessageList ml = attribute.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE));

        IPolicyCmptType supertype = newPolicyCmptType(ipsProject, "sup.SuperType");
        pcType.setSupertype(supertype.getQualifiedName());
        IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);

        ml = attribute.validate(ipsProject);
        assertThat(ml, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE));

        attribute.setAttributeType(superAttr.getAttributeType());
        ml = attribute.validate(ipsProject);
        assertThat(ml, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_TYPE));
    }

    @Test
    public void testIsPolicyCmptTypeProperty() {
        assertTrue(attribute.isPolicyCmptTypeProperty());
    }

    @Test
    public void testIsPropertyFor() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(pcType.getQualifiedName());
        pcType.setConfigurableByProductCmptType(true);
        pcType.setProductCmptType(productCmptType.getQualifiedName());

        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IPropertyValue propertyValue = generation.newPropertyValue(attribute, IConfiguredDefault.class);

        assertTrue(attribute.isPropertyFor(propertyValue));
    }

    @Test
    public void testValidateDefaultValue() {
        EnumType enumType = newEnumType(ipsProject, "ExtensibleEnum");
        enumType.setExtensible(true);

        attribute.setName("name");
        attribute.setDatatype(enumType.getQualifiedName());
        attribute.setOverwrite(false);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDefaultValue("notNull");

        MessageList ml = attribute.validate(ipsProject);

        assertFalse(ml.isEmpty());
        assertTrue(ml.getText().contains(Messages.PolicyCmptTypeAttribute_msg_defaultValueExtensibleEnumType));
    }

    @Test
    public void testGetAllowedValueSetTypes_allowEnumValueSetForExtensibleEnumDatatypes_dependingOnProductRelevance()
            {
        EnumType enumType = newEnumType(ipsProject, "ExtensibleEnum");
        enumType.setExtensible(true);

        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype(enumType.getQualifiedName());

        assertTrue(attribute.getAllowedValueSetTypes(ipsProject).contains(ValueSetType.ENUM));

        attribute.setValueSetConfiguredByProduct(false);
        assertFalse(attribute.getAllowedValueSetTypes(ipsProject).contains(ValueSetType.ENUM));
    }

    @Test
    public void testValidateThis_illegalValueSets() {
        EnumType enumType = newEnumType(ipsProject, "ExtensibleEnum");
        enumType.setExtensible(true);
        attribute.setValueSetType(ValueSetType.ENUM);
        attribute.setDatatype(enumType.getQualifiedName());
        attribute.setName("enumAttr");
        pcType.setConfigurableByProductCmptType(true);

        attribute.setValueSetConfiguredByProduct(true);
        MessageList messageList = attribute.validate(ipsProject);
        assertEquals(0, messageList.size());

        attribute.setValueSetConfiguredByProduct(false);
        messageList = attribute.validate(ipsProject);
        assertEquals(1, messageList.size());
        assertEquals(IPolicyCmptTypeAttribute.MSGCODE_ILLEGAL_VALUESET_TYPE, messageList.getMessage(0).getCode());
    }

    @Test
    public void testInitChangingOverTimeDefault_Attribute_ChangingOverTimeSettingIsUnchanged_IfProductCmptType_IsNull() {
        IPolicyCmptTypeAttribute attribute = pcType.newPolicyCmptTypeAttribute();

        assertTrue(attribute.isChangingOverTime());
    }

    @Test
    public void testInitChangingOverTimeDefault_Attribute_isChangingOverTime_IfProductCmptType_IsChangingOverTime()
            {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(true);
        pcType.setProductCmptType("ProductType");

        IPolicyCmptTypeAttribute attribute = pcType.newPolicyCmptTypeAttribute();

        assertTrue(attribute.isChangingOverTime());
    }

    @Test
    public void testInitChangingOverTimeDefault_Attribute_isNotChangingOverTime_IfProductCmptType_IsNotChangingOverTime()
            {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(false);
        pcType.setProductCmptType("ProductType");

        IPolicyCmptTypeAttribute attribute = pcType.newPolicyCmptTypeAttribute();

        assertFalse(attribute.isChangingOverTime());
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsNull() {
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setName("attributeName");
        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfAttributeIsNotProductRelevant()
            {
        newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        attribute.setValueSetConfiguredByProduct(false);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfAttributeIsNotChangeble() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(false);
        pcType.setProductCmptType(productCmptType.getQualifiedName());
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setAttributeType(AttributeType.CONSTANT);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfOverwrittenAndNotChangeble() {
        IPolicyCmptType supertype = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "sup.SuperType");
        pcType.setSupertype(supertype.getQualifiedName());
        IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute("name");
        superAttr.setDatatype("Integer");
        superAttr.setValueSetConfiguredByProduct(true);
        superAttr.setChangingOverTime(false);
        attribute.setName("name");
        attribute.setDatatype("String");
        attribute.setOverwrite(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setChangingOverTime(true);

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, lacksMessageCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsChangingOverTimeAndAttributeIsNotProductRelevant()
            {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        productCmptType.setChangingOverTime(true);
        attribute.setValueSetConfiguredByProduct(false);
        attribute.setName("name");

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsChangingOverTimeAndAttributeIsProductRelevant()
            {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        attribute.setName("name");
        productCmptType.setChangingOverTime(true);
        attribute.setValueSetConfiguredByProduct(true);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsNotChangingOverTimeAndAttributeIsNotProductRelevant()
            {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        attribute.setName("name");
        productCmptType.setChangingOverTime(false);
        attribute.setValueSetConfiguredByProduct(false);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_ReturnMessage_IfProductCmptTypeIsNotChangingOverTimeAndAttributeIsProductRelevant()
            {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        attribute.setName("name");
        productCmptType.setChangingOverTime(false);
        attribute.setValueSetConfiguredByProduct(true);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertNotNull(
                ml.getMessageByCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testChangingOverTime_default() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(true);
        pcType.setProductCmptType("ProductType");
        attribute = pcType.newPolicyCmptTypeAttribute();
        attribute.setName("name");
        attribute.setValueSetConfiguredByProduct(true);

        assertTrue(attribute.isChangingOverTime());

        productCmptType.setChangingOverTime(false);
        attribute = pcType.newPolicyCmptTypeAttribute();

        assertFalse(attribute.isChangingOverTime());
    }

    @Test
    public void testValidate_OverwrittenAttribute_MissingSupertype() throws Exception {
        attribute.setName("name");
        attribute.setDatatype("String");
        attribute.setOverwrite(true);

        MessageList messages = attribute.validate(ipsProject);

        assertThat(messages, hasMessageCode(IAttribute.MSGCODE_NOTHING_TO_OVERWRITE));
        assertThat(messages, not(hasMessageCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE)));
    }

    @Test
    public void testValidate_OverwrittenAttribute_IncompatibleDatatype() throws Exception {
        PolicyCmptType supertype = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "sup.SuperType");
        pcType.setSupertype(supertype.getQualifiedName());
        IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute("name");
        superAttr.setDatatype("Boolean");
        attribute.setName("name");
        attribute.setDatatype("String");
        attribute.setOverwrite(true);

        MessageList messages = attribute.validate(ipsProject);

        assertThat(messages, hasMessageCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE));
    }

    @Test
    public void testValidate_OverwrittenAttribute_CompatibleDatatype() throws Exception {
        PolicyCmptType supertype = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "sup.SuperType");
        pcType.setSupertype(supertype.getQualifiedName());
        IPolicyCmptTypeAttribute superAttr = supertype.newPolicyCmptTypeAttribute("name");
        superAttr.setDatatype("Integer");
        attribute.setName("name");
        attribute.setDatatype("Integer");
        attribute.setOverwrite(true);

        MessageList messages = attribute.validate(ipsProject);

        assertThat(messages, not(hasMessageCode(IAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_INCOMPATIBLE_DATATYPE)));
    }

    @Test
    public void testValidate_AbstractNotConstant() throws Exception {
        EnumType abstractEnumType = newEnumType(ipsProject, "AbstractEnum");
        abstractEnumType.setAbstract(true);
        attribute.setDatatype(abstractEnumType.getQualifiedName());

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_CONSTANT_CANT_BE_ABSTRACT));
    }

    @Test
    public void testValidate_AbstractConstant() throws Exception {
        EnumType abstractEnumType = newEnumType(ipsProject, "AbstractEnum");
        abstractEnumType.setAbstract(true);
        attribute.setAttributeType(AttributeType.CONSTANT);
        attribute.setDatatype(abstractEnumType.getQualifiedName());

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_CONSTANT_CANT_BE_ABSTRACT));
    }

    @Test
    public void testValidate_AbstractProductRelevant_ValueSetConfiguredByProduct() throws Exception {
        EnumType abstractEnumType = newEnumType(ipsProject, "AbstractEnum");
        abstractEnumType.setAbstract(true);
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype(abstractEnumType.getQualifiedName());

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_ABSTRACT_CANT_BE_PRODUCT_RELEVANT));
    }

    @Test
    public void testValidate_AbstractProductRelevant_RelevanceConfiguredByProduct() throws Exception {
        EnumType abstractEnumType = newEnumType(ipsProject, "AbstractEnum");
        abstractEnumType.setAbstract(true);
        attribute.setRelevanceConfiguredByProduct(true);
        attribute.setDatatype(abstractEnumType.getQualifiedName());

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_ABSTRACT_CANT_BE_PRODUCT_RELEVANT));
    }

    @Test
    public void testValidate_AbstractNotProductRelevant() throws Exception {
        EnumType abstractEnumType = newEnumType(ipsProject, "AbstractEnum");
        abstractEnumType.setAbstract(true);
        attribute.setDatatype(abstractEnumType.getQualifiedName());

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_ABSTRACT_CANT_BE_PRODUCT_RELEVANT));
    }

    @Test
    public void testValidate_NonAbstractProductRelevant() throws Exception {
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setRelevanceConfiguredByProduct(true);

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_ABSTRACT_CANT_BE_PRODUCT_RELEVANT));
    }
}
