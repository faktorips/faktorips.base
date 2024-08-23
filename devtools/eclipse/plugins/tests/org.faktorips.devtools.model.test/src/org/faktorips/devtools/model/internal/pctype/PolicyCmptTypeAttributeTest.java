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
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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
import org.faktorips.devtools.model.pctype.IValidationRule;
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

        assertThat(result, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE));

        method.setDatatype(attribute.getDatatype());
        result = attribute.validate(ipsProject);

        assertThat(result,
                lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_MEHTOD_HAS_DIFFERENT_DATATYPE));
    }

    @Test
    public void testValidateComputationMethodNotSpecified() {
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature("");
        attribute.setValueSetConfiguredByProduct(true);

        MessageList result = attribute.validate(ipsProject);

        assertThat(result, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED));

        attribute.setComputationMethodSignature("calc()");
        result = attribute.validate(ipsProject);

        assertThat(result, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_NOT_SPECIFIED));
    }

    @Test
    public void testValidateComputationMethodDoesNotExist() {
        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature("");
        attribute.setValueSetConfiguredByProduct(true);

        MessageList result = attribute.validate(ipsProject);
        assertThat(result, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST));

        attribute.setComputationMethodSignature("calcPremium(TestPolicy)");
        result = attribute.validate(ipsProject);

        assertThat(result, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST));

        IProductCmptType productType = newProductCmptType(ipsProject, "TestProduct");
        pcType.setConfigurableByProductCmptType(true);
        pcType.setProductCmptType(productType.getQualifiedName());
        IMethod method = productType.newMethod();
        method.setName("calcPremium");
        result = attribute.validate(ipsProject);

        assertThat(result, hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST));

        method.newParameter("TestPolicy", "policy");
        result = attribute.validate(ipsProject);

        assertThat(result, lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_COMPUTATION_METHOD_DOES_NOT_EXIST));
    }

    @Test
    public void testFindComputationMethodSignature() {
        assertThat(attribute.findComputationMethod(ipsProject), is(nullValue()));

        attribute.setAttributeType(AttributeType.DERIVED_ON_THE_FLY);
        attribute.setName("premium");
        attribute.setComputationMethodSignature("calcPremium(TestPolicy)");

        IProductCmptType productType = newProductCmptType(ipsProject, "TestProduct");
        pcType.setConfigurableByProductCmptType(true);
        pcType.setProductCmptType(productType.getQualifiedName());

        assertThat(attribute.findComputationMethod(ipsProject), is(nullValue()));

        IMethod method = productType.newMethod();
        method.setName("calcPremium");

        assertThat(attribute.findComputationMethod(ipsProject), is(nullValue()));

        method.newParameter("TestPolicy", "policy");

        assertThat(attribute.findComputationMethod(ipsProject), is(method));
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

        assertThat(attribute.findOverwrittenAttribute(ipsProject), is(nullValue()));

        IPolicyCmptTypeAttribute aInSupertype = supersupertype.newPolicyCmptTypeAttribute();
        aInSupertype.setName("a");

        assertThat(attribute.findOverwrittenAttribute(ipsProject), is(aInSupertype));

        // cycle in type hierarchy
        supersupertype.setSupertype(pcType.getQualifiedName());

        assertThat(attribute.findOverwrittenAttribute(ipsProject), is(aInSupertype));

        aInSupertype.delete();

        assertThat(attribute.findOverwrittenAttribute(ipsProject), is(nullValue()));
        // this should not return a itself!
    }

    @Test
    public void testRemove() {
        attribute.delete();

        assertThat(pcType.getPolicyCmptTypeAttributes().size(), is(0));
        assertThat(ipsSrcFile.isDirty(), is(true));
    }

    @Test
    public void testSetDatatype() {
        attribute.setDatatype("Money");

        assertThat(attribute.getDatatype(), is("Money"));
        assertThat(ipsSrcFile.isDirty(), is(true));
    }

    @Test
    public void testSetComputed() {
        attribute.setValueSetConfiguredByProduct(true);

        assertThat(attribute.isProductRelevant(), is(true));
        assertThat(ipsSrcFile.isDirty(), is(true));
    }

    @Test
    public void testInitFromXml() {
        Document doc = getTestDocument();
        Element root = doc.getDocumentElement();
        NodeList nl = root.getElementsByTagName("Attribute");
        attribute.initFromXml((Element)nl.item(0));

        assertThat(attribute.getId(), is("42"));
        assertThat(attribute.getName(), is("premium"));
        assertThat(attribute.getComputationMethodSignature(), is("computePremium"));
        assertThat(attribute.getDatatype(), is("money"));
        assertThat(attribute.isProductRelevant(), is(false));
        assertThat(attribute.getAttributeType(), is(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL));
        assertThat(attribute.getDefaultValue(), is("42EUR"));
        assertThat(attribute.getValueSet(), is(not(nullValue())));
        assertThat(attribute.isOverwrite(), is(false));
        assertThat(attribute.isChangingOverTime(), is(true));

        attribute.initFromXml((Element)nl.item(1));
        assertThat(attribute.getId(), is("2"));
        assertThat(attribute.getDefaultValue(), is(nullValue()));
        assertThat(attribute.getValueSet(), is(not(nullValue())));
        assertThat(attribute.getValueSet().getClass(), is(EnumValueSet.class));
        assertThat(attribute.isChangingOverTime(), is(false));

        attribute.initFromXml((Element)nl.item(2));
        assertThat(attribute.isOverwrite(), is(true));
        assertThat(attribute.isChangingOverTime(), is(true));

        // legacy productRelevant
        attribute.initFromXml((Element)nl.item(3));
        assertThat(attribute.isProductRelevant(), is(true));
        assertThat(attribute.isValueSetConfiguredByProduct(), is(true));
        assertThat(attribute.isRelevanceConfiguredByProduct(), is(false));

        attribute.initFromXml((Element)nl.item(4));
        assertThat(attribute.isProductRelevant(), is(true));
        assertThat(attribute.isValueSetConfiguredByProduct(), is(true));
        assertThat(attribute.isRelevanceConfiguredByProduct(), is(false));

        attribute.initFromXml((Element)nl.item(5));
        assertThat(attribute.isProductRelevant(), is(true));
        assertThat(attribute.isValueSetConfiguredByProduct(), is(false));
        assertThat(attribute.isRelevanceConfiguredByProduct(), is(true));

        attribute.initFromXml((Element)nl.item(6));
        assertThat(attribute.isProductRelevant(), is(true));
        assertThat(attribute.isValueSetConfiguredByProduct(), is(true));
        assertThat(attribute.isRelevanceConfiguredByProduct(), is(true));

        assertThat(attribute.isGenericValidationEnabled(), is(false));

        attribute.initFromXml((Element)nl.item(7));
        assertThat(attribute.isGenericValidationEnabled(), is(true));
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

        assertThat(element.hasAttribute(IPolicyCmptTypeAttribute.PROPERTY_GENERIC_VALIDATION), is(false));
        IPolicyCmptTypeAttribute copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);

        assertThat(copy.getId(), is(attribute.getId()));
        assertThat(copy.getName(), is("age"));
        assertThat(copy.getDatatype(), is("decimal"));
        assertThat(copy.getComputationMethodSignature(), is("computePremium"));
        assertThat(copy.isProductRelevant(), is(true));
        assertThat(copy.isValueSetConfiguredByProduct(), is(true));
        assertThat(copy.isRelevanceConfiguredByProduct(), is(false));
        assertThat(copy.isOverwrite(), is(false));
        assertThat(copy.isGenericValidationEnabled(), is(false));
        assertThat(copy.getAttributeType(), is(AttributeType.CONSTANT));
        assertThat(copy.getDefaultValue(), is("18"));
        assertThat(((IRangeValueSet)copy.getValueSet()).getLowerBound(), is("unten"));
        assertThat(((IRangeValueSet)copy.getValueSet()).getUpperBound(), is("oben"));
        assertThat(((IRangeValueSet)copy.getValueSet()).getStep(), is("step"));
        assertThat(copy.getCategory(), is("foo"));
        assertThat(copy.isChangingOverTime(), is(false));

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
        assertThat(attribute.getName(), is("age"));
        assertThat(attribute.getDatatype(), is("decimal"));
        assertThat(attribute.isProductRelevant(), is(true));
        assertThat(copy.isValueSetConfiguredByProduct(), is(false));
        assertThat(copy.isRelevanceConfiguredByProduct(), is(true));
        assertThat(copy.isGenericValidationEnabled(), is(true));
        assertThat(attribute.getAttributeType(), is(AttributeType.CONSTANT));
        assertThat(attribute.getDefaultValue(), is("18"));
        String[] vekt = ((IEnumValueSet)copy.getValueSet()).getValues();
        assertThat(vekt[0], is("a"));
        assertThat(vekt[1], is("b"));
        assertThat(vekt[2], is("x"));

        // and now an attribute which overwrites
        attribute.setOverwrite(true);
        element = attribute.toXml(newDocument());
        copy = pcType.newPolicyCmptTypeAttribute();
        copy.initFromXml(element);
        assertThat(attribute.isOverwrite(), is(true));
    }

    @Test
    public void testValidateProductRelevant_ValueSetConfiguredByProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setValueSetConfiguredByProduct(true);

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml,
                lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));

        pcType.setConfigurableByProductCmptType(false);
        ml = attribute.validate(ipsProject);

        assertThat(ml,
                hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    }

    @Test
    public void testValidateProductRelevant_RelevanceConfiguredByProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setRelevanceConfiguredByProduct(true);

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml,
                lacksMessageCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));

        pcType.setConfigurableByProductCmptType(false);
        ml = attribute.validate(ipsProject);

        assertThat(ml,
                hasMessageCode(IPolicyCmptTypeAttribute.MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT));
    }

    @Test
    public void testValidateProductRelevant_DerivedValueSet_ValueSetConfiguredByProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setValueSetType(ValueSetType.DERIVED);
        attribute.setName("derived");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, isEmpty());

        attribute.setValueSetConfiguredByProduct(true);

        ml = attribute.validate(ipsProject);

        assertThat(ml, isEmpty());
    }

    @Test
    public void testValidateProductRelevant_DerivedValueSet_RelevanceConfiguredByProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setValueSetType(ValueSetType.DERIVED);
        attribute.setName("derived");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, isEmpty());

        attribute.setRelevanceConfiguredByProduct(true);

        ml = attribute.validate(ipsProject);

        assertThat(ml, isEmpty());
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
        assertThat(attribute.isPolicyCmptTypeProperty(), is(true));
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

        assertThat(attribute.isPropertyFor(propertyValue), is(true));
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

        assertThat(ml.isEmpty(), is(false));
        assertThat(ml.getText().contains(Messages.PolicyCmptTypeAttribute_msg_defaultValueExtensibleEnumType),
                is(true));
    }

    @Test
    public void testGetAllowedValueSetTypes_allowEnumValueSetForExtensibleEnumDatatypes_dependingOnProductRelevance() {
        EnumType enumType = newEnumType(ipsProject, "ExtensibleEnum");
        enumType.setExtensible(true);

        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDatatype(enumType.getQualifiedName());

        assertThat(attribute.getAllowedValueSetTypes(ipsProject).contains(ValueSetType.ENUM), is(true));

        attribute.setValueSetConfiguredByProduct(false);

        assertThat(attribute.getAllowedValueSetTypes(ipsProject).contains(ValueSetType.ENUM), is(false));
    }

    @Test
    public void testGetProposalValueSetRuleName_noDuplicate() {
        attribute.setName("premium");

        assertThat(attribute.getProposalValueSetRuleName(), is("checkPremium"));
    }

    @Test
    public void testGetProposalValueSetRuleName_Duplicate() {
        attribute.setName("premium");
        IValidationRule newRule = pcType.newRule();
        newRule.setName("checkPremium");

        assertThat(attribute.getProposalValueSetRuleName(), is("checkPremiumValueSet"));
    }

    @Test
    public void testGetProposalValueSetRuleName_Duplicate2() {
        attribute.setName("premium");
        IValidationRule newRule = pcType.newRule();
        newRule.setName("checkPremium");
        IValidationRule newRule2 = pcType.newRule();
        newRule2.setName("checkPremiumValueSet");

        assertThat(attribute.getProposalValueSetRuleName(), is("checkPremiumValueSet2"));

        attribute.createValueSetRule();

        assertThat(attribute.getProposalValueSetRuleName(), is("checkPremiumValueSet3"));
    }

    @Test
    public void testGetProposalValueSetRuleName_DuplicateDeleted() {
        attribute.setName("premium");
        attribute.createValueSetRule();
        attribute.deleteValueSetRule();

        assertThat(attribute.getProposalValueSetRuleName(), is("checkPremium"));
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

        assertThat(messageList.size(), is(0));

        attribute.setValueSetConfiguredByProduct(false);
        messageList = attribute.validate(ipsProject);

        assertThat(messageList.size(), is(1));
        assertThat(messageList.getMessage(0).getCode(), is(IPolicyCmptTypeAttribute.MSGCODE_ILLEGAL_VALUESET_TYPE));
    }

    @Test
    public void testInitChangingOverTimeDefault_Attribute_ChangingOverTimeSettingIsUnchanged_IfProductCmptType_IsNull() {
        IPolicyCmptTypeAttribute attribute = pcType.newPolicyCmptTypeAttribute();

        assertThat(attribute.isChangingOverTime(), is(true));
    }

    @Test
    public void testInitChangingOverTimeDefault_Attribute_isChangingOverTime_IfProductCmptType_IsChangingOverTime() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(true);
        pcType.setProductCmptType("ProductType");

        IPolicyCmptTypeAttribute attribute = pcType.newPolicyCmptTypeAttribute();

        assertThat(attribute.isChangingOverTime(), is(true));
    }

    @Test
    public void testInitChangingOverTimeDefault_Attribute_isNotChangingOverTime_IfProductCmptType_IsNotChangingOverTime() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(false);
        pcType.setProductCmptType("ProductType");

        IPolicyCmptTypeAttribute attribute = pcType.newPolicyCmptTypeAttribute();

        assertThat(attribute.isChangingOverTime(), is(false));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsNull() {
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setName("attributeName");
        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertThat(ml,
                lacksMessageCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfAttributeIsNotProductRelevant() {
        newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        attribute.setValueSetConfiguredByProduct(false);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertThat(ml,
                lacksMessageCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfAttributeIsNotChangeble() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(false);
        pcType.setProductCmptType(productCmptType.getQualifiedName());
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setAttributeType(AttributeType.CONSTANT);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertThat(ml,
                lacksMessageCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
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
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsChangingOverTimeAndAttributeIsNotProductRelevant() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        productCmptType.setChangingOverTime(true);
        attribute.setValueSetConfiguredByProduct(false);
        attribute.setName("name");

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertThat(ml,
                lacksMessageCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsChangingOverTimeAndAttributeIsProductRelevant() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        attribute.setName("name");
        productCmptType.setChangingOverTime(true);
        attribute.setValueSetConfiguredByProduct(true);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertThat(ml,
                lacksMessageCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_DoesNotReturnMessage_IfProductCmptTypeIsNotChangingOverTimeAndAttributeIsNotProductRelevant() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        attribute.setName("name");
        productCmptType.setChangingOverTime(false);
        attribute.setValueSetConfiguredByProduct(false);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertThat(ml,
                lacksMessageCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testValidateChangingOverTime_ReturnMessage_IfProductCmptTypeIsNotChangingOverTimeAndAttributeIsProductRelevant() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        pcType.setProductCmptType("ProductType");
        attribute.setName("name");
        productCmptType.setChangingOverTime(false);
        attribute.setValueSetConfiguredByProduct(true);

        MessageList ml = attribute.validate(attribute.getIpsProject());

        assertThat(ml,
                hasMessageCode(ChangingOverTimePropertyValidator.MSGCODE_TYPE_DOES_NOT_ACCEPT_CHANGING_OVER_TIME));
    }

    @Test
    public void testChangingOverTime_default() {
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        productCmptType.setChangingOverTime(true);
        pcType.setProductCmptType("ProductType");
        attribute = pcType.newPolicyCmptTypeAttribute();
        attribute.setName("name");
        attribute.setValueSetConfiguredByProduct(true);

        assertThat(attribute.isChangingOverTime(), is(true));

        productCmptType.setChangingOverTime(false);
        attribute = pcType.newPolicyCmptTypeAttribute();

        assertThat(attribute.isChangingOverTime(), is(false));
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

    @Test
    public void testValidate_isMarkedDynamicAndRelevance() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setName("isDynamic");
        attribute.setDatatype("String");
        attribute.setValueSetType(ValueSetType.DERIVED);
        attribute.setRelevanceConfiguredByProduct(true);

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, isEmpty());
    }

    @Test
    public void testValidate_isMarkedDynamicAndValueSetConfiguredProduct() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setName("isDynamic");
        attribute.setDatatype("String");
        attribute.setValueSetType(ValueSetType.DERIVED);
        attribute.setValueSetConfiguredByProduct(true);

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, isEmpty());
    }

    @Test
    public void testValidate_isMarkedDynamicButNotProductRelevant() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setName("isDynamic");
        attribute.setDatatype("String");
        attribute.setValueSetType(ValueSetType.DERIVED);

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, isEmpty());
    }

    @Test
    public void testValidate_isMarkedDynamicAndValueSetNotDerived() throws Exception {
        pcType.setConfigurableByProductCmptType(true);
        attribute.setName("isDynamic");
        attribute.setDatatype("String");
        attribute.setValueSetType(ValueSetType.UNRESTRICTED);
        attribute.setValueSetConfiguredByProduct(true);

        MessageList ml = attribute.validate(ipsProject);

        assertThat(ml, isEmpty());
    }
}
