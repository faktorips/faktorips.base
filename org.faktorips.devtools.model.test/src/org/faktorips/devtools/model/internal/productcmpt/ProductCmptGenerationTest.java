/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectProperties;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IExpressionDependencyDetail;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptGenerationTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IPolicyCmptTypeAttribute attribute;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsProject ipsProject;

    private IPolicyCmptType targetPolicyType;
    private IProductCmptType targetProductType;
    private IProductCmptTypeAssociation association;
    private IProductCmpt target;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("attribute");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);

        targetPolicyType = newPolicyAndProductCmptType(ipsProject, "TargetPolicyType", "TargetProductType");
        targetProductType = targetPolicyType.findProductCmptType(ipsProject);
        target = newProductCmpt(targetProductType, "TargetProduct");

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelationProductSide");
        association.setTargetRolePlural("testRelationsProductSide");
    }

    @Test
    public void testGetAttributeValue() {
        IAttributeValue value1 = generation.newAttributeValue();
        value1.setAttribute("a1");
        IAttributeValue value2 = generation.newAttributeValue();
        value2.setAttribute("a2");

        assertEquals(value1, generation.getAttributeValue("a1"));
        assertEquals(value2, generation.getAttributeValue("a2"));

        assertNull(generation.getAttributeValue("unknwon"));
        assertNull(generation.getAttributeValue(null));
    }

    @Test
    public void testGetFormula() {
        IFormula formula1 = generation.newFormula();
        formula1.setFormulaSignature("f1");
        IFormula formula2 = generation.newFormula();
        formula2.setFormulaSignature("f2");

        assertEquals(formula1, generation.getFormula("f1"));
        assertEquals(formula2, generation.getFormula("f2"));

        assertNull(generation.getFormula("unknwon"));
        assertNull(generation.getFormula(null));
    }

    @Test
    public void testGetPropertyValue() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("a1");
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("RateTable");
        IProductCmptTypeMethod signature = productCmptType.newFormulaSignature("calculation");
        IPolicyCmptTypeAttribute policyAttr = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute");
        policyAttr.setValueSetConfiguredByProduct(true);

        IAttributeValue value = generation.newAttributeValue();
        value.setAttribute("a1");
        IFormula formula = generation.newFormula();
        formula.setFormulaSignature("calculation");
        ITableContentUsage contentUsage = generation.newTableContentUsage();
        contentUsage.setStructureUsage("RateTable");
        IConfiguredDefault defaultValue = generation.newPropertyValue(policyAttr, IConfiguredDefault.class);

        assertEquals(value, generation.getPropertyValue(attribute, IAttributeValue.class));
        assertEquals(formula, generation.getPropertyValue(signature, IFormula.class));
        assertEquals(contentUsage, generation.getPropertyValue(structureUsage, ITableContentUsage.class));
        assertEquals(defaultValue, generation.getPropertyValue(policyAttr, IConfiguredDefault.class));
    }

    @Test
    public void testHasPropertyValue() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("productAttribute");

        assertFalse(generation.hasPropertyValue(attribute, PropertyValueType.ATTRIBUTE_VALUE));

        generation.newAttributeValue(attribute);
        assertTrue(generation.hasPropertyValue(attribute, PropertyValueType.ATTRIBUTE_VALUE));
    }

    @Test
    public void testNewFormula_FormulaSignature() {
        IProductCmptTypeMethod signature = productCmptType.newFormulaSignature("Calc");
        IFormula formula = generation.newFormula(signature);
        assertEquals("Calc", formula.getFormulaSignature());

        formula = generation.newFormula();
        assertEquals("", formula.getFormulaSignature());
    }

    @Test
    public void testNewTableContentUsage_TableStructure() {
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage = generation.newTableContentUsage(structureUsage);
        assertEquals("RateTable", contentUsage.getStructureUsage());

        contentUsage = generation.newTableContentUsage();
        assertEquals("", contentUsage.getStructureUsage());
    }

    @Test
    public void testNewAttributeValue_Attribute() {
        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("premium");
        attribute.setDefaultValue("123");
        IAttributeValue value = generation.newAttributeValue(attribute);
        assertEquals("123", value.getPropertyValue());
        assertEquals("premium", value.getAttribute());

        value = generation.newAttributeValue();
        assertNull(value.getPropertyValue());
        assertEquals("", value.getAttribute());
    }

    @Test
    public void testNewPropertyValue_configElement() {
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName("a1");
        attribute.setValueSetConfiguredByProduct(true);
        attribute.setDefaultValue("10");
        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attribute.getValueSet();
        range.setLowerBound("1");
        range.setUpperBound("42");

        IConfiguredDefault defaultValue = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        IConfiguredValueSet valueSet = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        assertEquals("a1", defaultValue.getPolicyCmptTypeAttribute());
        assertEquals("10", defaultValue.getValue());
        range = (IRangeValueSet)valueSet.getValueSet();
        assertEquals("1", range.getLowerBound());
        assertEquals("42", range.getUpperBound());
    }

    @Test
    public void testGetPropertyValues() {
        IAttributeValue value1 = generation.newAttributeValue();
        IFormula formula1 = generation.newFormula();
        IFormula formula2 = generation.newFormula();
        ITableContentUsage tcu1 = generation.newTableContentUsage();
        ITableContentUsage tcu2 = generation.newTableContentUsage();
        ITableContentUsage tcu3 = generation.newTableContentUsage();
        IConfiguredDefault defaultValue1 = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        IConfiguredDefault defaultValue2 = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        IConfiguredDefault defaultValue3 = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        IConfiguredDefault defaultValue4 = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        IConfiguredValueSet valueSet1 = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        IConfiguredValueSet valueSet2 = generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        IConfiguredValueSet valueSet3 = generation.newPropertyValue(attribute, IConfiguredValueSet.class);

        List<? extends IPropertyValue> values = generation.getPropertyValues(PropertyValueType.ATTRIBUTE_VALUE
                .getInterfaceClass());
        assertEquals(1, values.size());
        assertEquals(value1, values.get(0));

        values = generation.getPropertyValues(PropertyValueType.FORMULA.getInterfaceClass());
        assertEquals(2, values.size());
        assertEquals(formula1, values.get(0));
        assertEquals(formula2, values.get(1));

        values = generation.getPropertyValues(PropertyValueType.TABLE_CONTENT_USAGE.getInterfaceClass());
        assertEquals(3, values.size());
        assertEquals(tcu1, values.get(0));
        assertEquals(tcu2, values.get(1));
        assertEquals(tcu3, values.get(2));

        values = generation.getPropertyValues(PropertyValueType.CONFIGURED_DEFAULT.getInterfaceClass());
        assertEquals(4, values.size());
        assertEquals(defaultValue1, values.get(0));
        assertEquals(defaultValue2, values.get(1));
        assertEquals(defaultValue3, values.get(2));
        assertEquals(defaultValue4, values.get(3));

        values = generation.getPropertyValues(PropertyValueType.CONFIGURED_VALUESET.getInterfaceClass());
        assertEquals(3, values.size());
        assertEquals(valueSet1, values.get(0));
        assertEquals(valueSet2, values.get(1));
        assertEquals(valueSet3, values.get(2));

    }

    @Test
    public void testNewLink() {
        IProductCmptLink link = generation.newLink("coverage");
        assertEquals(generation, link.getParent());
        assertEquals(1, generation.getNumOfLinks());
        assertEquals(link, generation.getLinks()[0]);

        IProductCmptLink link2 = generation.newLink("covergae");
        assertEquals(generation, link2.getParent());
        assertEquals(2, generation.getNumOfLinks());
        assertEquals(link, generation.getLinks()[0]);
        assertEquals(link2, generation.getLinks()[1]);
    }

    @Test
    public void testToXmlElement() {
        generation.setValidFrom(new GregorianCalendar(2005, 0, 1));
        generation.newPropertyValue(attribute, IConfiguredDefault.class);
        generation.newPropertyValue(attribute, IConfiguredValueSet.class);
        generation.newLink("coverage");
        generation.newLink("coverage");
        generation.newLink("coverage");
        generation.newFormula();
        generation.newFormula();
        generation.newAttributeValue();
        generation.newTableContentUsage();
        newValidationRuleConfig();
        newValidationRuleConfig();

        Element element = generation.toXml(newDocument());

        IProductCmptGeneration copy = (IProductCmptGeneration)productCmpt.newGeneration();
        copy.initFromXml(element);
        assertEquals(2, copy.getNumOfConfigElements());
        assertEquals(3, copy.getNumOfLinks());
        assertEquals(2, copy.getNumOfFormulas());
        assertEquals(1, copy.getNumOfAttributeValues());
        assertEquals(2, copy.getNumOfValidationRules());
        assertEquals(1, copy.getNumOfTableContentUsages());
    }

    @Test
    public void testInitFromXml() {
        generation.initFromXml(getTestDocument().getDocumentElement());
        assertEquals(new GregorianCalendar(2005, 0, 1), generation.getValidFrom());

        IAttributeValue[] attrValues = generation.getAttributeValues();
        assertEquals(1, attrValues.length);

        IConfiguredDefault[] configDefaults = generation.getConfiguredDefaults();
        assertEquals(1, configDefaults.length);

        IProductCmptLink[] relations = generation.getLinks();
        assertEquals(1, relations.length);

        IFormula[] formulas = generation.getFormulas();
        assertEquals(2, formulas.length);

        List<IValidationRuleConfig> rules = generation.getValidationRuleConfigs();
        assertEquals(1, rules.size());

        ITableContentUsage[] tableContentUsages = generation.getTableContentUsages();
        assertEquals(1, tableContentUsages.length);
    }

    @Test
    public void testValidateDuplicateRelationTarget() throws Exception {
        MessageList ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_DUPLICATE_RELATION_TARGET));

        generation.newLink(association.getName()).setTarget(target.getQualifiedName());
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_DUPLICATE_RELATION_TARGET));

        generation.newLink(association).setTarget(target.getQualifiedName());

        ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_DUPLICATE_RELATION_TARGET));
    }

    @Test
    public void testValidateNotEnoughRelations() throws Exception {
        validateNotEnoughRelationsTest(generation);
    }

    private void validateNotEnoughRelationsTest(IProductCmptGeneration baseGeneration) throws Exception {
        association.setMinCardinality(1);
        association.setMaxCardinality(2);

        MessageList ml = baseGeneration.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS));

        baseGeneration.newLink(association.getTargetRoleSingular());
        ml = baseGeneration.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS));

        baseGeneration.newLink(association.getTargetRoleSingular());
        ml = baseGeneration.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS));

    }

    @Test
    public void testValidateTooManyRelations() throws Exception {
        validateTooManyRelationsTest(generation);
    }

    private void validateTooManyRelationsTest(IProductCmptGeneration baseGeneration) throws Exception {
        association.setMinCardinality(0);
        association.setMaxCardinality(1);

        MessageList ml = baseGeneration.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_TOO_MANY_RELATIONS));

        baseGeneration.newLink(association.getTargetRoleSingular());
        ml = baseGeneration.validate(ipsProject);
        assertNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_TOO_MANY_RELATIONS));

        baseGeneration.newLink(association.getTargetRoleSingular());
        ml = baseGeneration.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptLinkContainer.MSGCODE_TOO_MANY_RELATIONS));
    }

    @Test
    public void testValidateNotEnoughRelationsHierarchy() throws Exception {
        validateNotEnoughRelationsTest(getSubGenertation());
    }

    @Test
    public void testValidateTooManyRelationsHierarchy() throws Exception {
        validateTooManyRelationsTest(getSubGenertation());
    }

    private IProductCmptGeneration getSubGenertation() throws CoreRuntimeException {
        PolicyCmptType subPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicy", "SubProduct");
        subPolicyCmptType.setSupertype(policyCmptType.getName());
        IProductCmptType subProductCmptType = subPolicyCmptType.findProductCmptType(ipsProject);
        subProductCmptType.setSupertype(productCmptType.getName());
        ProductCmpt subProductCmpt = newProductCmpt(subProductCmptType, "SubTestProduct");
        IProductCmptGeneration subGeneration = subProductCmpt.getProductCmptGeneration(0);
        return subGeneration;
    }

    @Test
    public void testCanCreateValidRelation() throws Exception {
        assertFalse(generation.canCreateValidLink(null, null, ipsProject));
        assertFalse(generation.canCreateValidLink(productCmpt, null, ipsProject));

        assertTrue(generation.canCreateValidLink(target, association, ipsProject));
    }

    /**
     * test for bug #829
     */
    @Test
    public void testCanCreateValidRelation_RelationDefinedInSupertypeHierarchyOfSourceType() throws Exception {
        // create a subtype of the existing policy component type
        IPolicyCmptType subpolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicyType", "SubProductType");
        IProductCmptType subProductCmptType = subpolicyCmptType.findProductCmptType(ipsProject);
        subpolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        subProductCmptType.setSupertype(productCmptType.getQualifiedName());

        IProductCmpt productCmpt2 = newProductCmpt(subProductCmptType, "TestProduct2");
        IProductCmptGeneration generation2 = productCmpt2.getProductCmptGeneration(0);

        assertTrue(generation2.canCreateValidLink(target, association, ipsProject));
    }

    @Test
    public void testGetChildren() throws CoreRuntimeException {
        IConfiguredDefault defaultValue = generation.newPropertyValue(attribute, IConfiguredDefault.class);
        IProductCmptLink link = generation.newLink("targetRole");
        ITableContentUsage usage = generation.newTableContentUsage();
        IFormula formula = generation.newFormula();

        IValidationRule rule = mock(IValidationRule.class);
        when(rule.getPropertyName()).thenReturn("newRule");
        when(rule.isActivatedByDefault()).thenReturn(false);
        when(rule.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALIDATION_RULE);
        IValidationRuleConfig ruleConfig = generation.newValidationRuleConfig(rule);

        IIpsElement[] children = generation.getChildren();
        List<IIpsElement> childrenList = Arrays.asList(children);
        assertTrue(childrenList.contains(defaultValue));
        assertTrue(childrenList.contains(usage));
        assertTrue(childrenList.contains(formula));
        assertTrue(childrenList.contains(link));
        assertTrue(childrenList.contains(ruleConfig));
    }

    @Test
    public void testGetRelations() {
        IProductCmptLink r1 = generation.newLink("coverage");
        assertEquals(r1, generation.getLinks()[0]);

        IProductCmptLink r2 = generation.newLink("risk");
        assertEquals(r1, generation.getLinks()[0]);
        assertEquals(r2, generation.getLinks()[1]);
    }

    @Test
    public void testGetRelations_String() {
        IProductCmptLink r1 = generation.newLink("coverage");
        generation.newLink("risk");
        IProductCmptLink r3 = generation.newLink("coverage");

        IProductCmptLink[] relations = generation.getLinks("coverage");
        assertEquals(2, relations.length);
        assertEquals(r1, relations[0]);
        assertEquals(r3, relations[1]);

        relations = generation.getLinks("unknown");
        assertEquals(0, relations.length);
    }

    @Test
    public void testGetNumOfLinks() {
        assertEquals(0, generation.getNumOfLinks());

        generation.newLink("coverage");
        assertEquals(1, generation.getNumOfLinks());

        generation.newLink("risk");
        assertEquals(2, generation.getNumOfLinks());
    }

    @Test
    public void testValidateNoTemplate() throws Exception {
        generation.getProductCmpt().setProductCmptType("");
        MessageList ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IProductCmptGeneration.MSGCODE_NO_TEMPLATE));
    }

    @Test
    public void testValidateValidFrom() throws Exception {
        generation.getProductCmpt().setValidTo(new GregorianCalendar(2000, 10, 1));
        generation.setValidFrom(new GregorianCalendar(2000, 10, 2));

        MessageList ml = generation.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IIpsObjectGeneration.MSGCODE_INVALID_VALID_FROM));

        generation.setValidFrom(new GregorianCalendar(2000, 9, 1));
        ml = generation.validate(ipsProject);
        assertNull(ml.getMessageByCode(IIpsObjectGeneration.MSGCODE_INVALID_VALID_FROM));
    }

    @Test
    public void testValidateIfReferencedProductComponentsAreValidOnThisGenerationsValidFromDate() throws CoreRuntimeException,
            Exception {
        generation.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        IProductCmptLink link = generation.newLink(association);
        link.setTarget(target.getQualifiedName());
        link.setMinCardinality(0);
        link.setMaxCardinality(1);
        IProductCmptGeneration targetGeneration = (IProductCmptGeneration)target.getGeneration(0);
        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2008-01-01"));

        MessageList msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNotNull(msgList.getMessageByCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        // assert that there is no validation error if the optional constraint
        // "referencedProductComponentsAreValidOnThisGenerationsValidFromDate" is turned off
        IIpsProjectProperties oldProps = ipsProject.getProperties();
        IIpsProjectProperties newProps = new IpsProjectProperties(ipsProject, (IpsProjectProperties)oldProps);
        newProps.setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(false);
        ipsProject.setProperties(newProps);
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));
        ipsProject.getProperties()
                .setReferencedProductComponentsAreValidOnThisGenerationsValidFromDateRuleEnabled(true);
        ipsProject.setProperties(oldProps);

        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2007-01-01"));
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));

        targetGeneration.setValidFrom(DateUtil.parseIsoDateStringToGregorianCalendar("2006-01-01"));
        msgList = ((ProductCmptGeneration)generation).validate(ipsProject);
        assertNull(msgList.getMessageByCode(IProductCmptLinkContainer.MSGCODE_LINKS_WITH_WRONG_EFFECTIVE_DATE));
    }

    @Test
    public void testGetValidationRules() {
        List<IValidationRuleConfig> rules = generation.getValidationRuleConfigs();
        assertEquals(0, rules.size());

        newValidationRuleConfig();
        rules = generation.getValidationRuleConfigs();
        assertEquals(1, rules.size());

        newValidationRuleConfig();
        rules = generation.getValidationRuleConfigs();
        assertEquals(2, rules.size());
    }

    private IValidationRuleConfig newValidationRuleConfig() {
        IValidationRule rule = mock(IValidationRule.class);
        when(rule.getPropertyName()).thenReturn("newRule");
        when(rule.isActivatedByDefault()).thenReturn(false);
        when(rule.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALIDATION_RULE);
        return generation.newValidationRuleConfig(rule);
    }

    @Test
    public void testGetNumValidationRules() {
        assertEquals(0, generation.getNumOfValidationRules());

        newValidationRuleConfig();
        assertEquals(1, generation.getNumOfValidationRules());

        newValidationRuleConfig();
        assertEquals(2, generation.getNumOfValidationRules());
    }

    @Test
    public void testNewValidationRule() throws CoreRuntimeException {
        assertEquals(0, generation.getNumOfValidationRules());

        newValidationRuleConfig();
        assertEquals(1, generation.getChildren().length);

        newValidationRuleConfig();
        assertEquals(2, generation.getChildren().length);
    }

    @Test
    public void testGetValidationRuleByName() {
        IValidationRule rule;

        rule = policyCmptType.newRule();
        rule.setName("rule1");
        generation.newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("ruleTwo");
        generation.newValidationRuleConfig(rule);

        rule = policyCmptType.newRule();
        rule.setName("ruleThree");
        generation.newValidationRuleConfig(rule);

        assertEquals(3, generation.getValidationRuleConfigs().size());

        assertNotNull(generation.getValidationRuleConfig("rule1"));
        assertNotNull(generation.getValidationRuleConfig("ruleTwo"));
        assertNotNull(generation.getValidationRuleConfig("ruleThree"));
        assertNull(generation.getValidationRuleConfig("nonExistingRule"));
        assertNull(generation.getValidationRuleConfig(null));
    }

    @Test
    public void testIsContainerForChangingAssociation() {
        IProductCmptTypeAssociation changingAssoc = productCmptType.newProductCmptTypeAssociation();
        changingAssoc.setChangingOverTime(true);

        assertTrue(generation.isContainerFor(changingAssoc));
    }

    @Test
    public void testIsContainerForStaticAssociation() {
        IProductCmptTypeAssociation staticAssoc = productCmptType.newProductCmptTypeAssociation();
        staticAssoc.setChangingOverTime(false);

        assertFalse(generation.isContainerFor(staticAssoc));
    }

    @Test
    public void testGetLinksIncludingProductCmpt() throws Exception {
        IProductCmptGeneration generation1 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2010, 0, 1));
        IProductCmptGeneration generation2 = (IProductCmptGeneration)productCmpt.newGeneration(new GregorianCalendar(
                2011, 0, 1));
        ArrayList<IProductCmptLink> links = new ArrayList<>();
        links.add(productCmpt.newLink("asdff"));
        links.add(productCmpt.newLink("asdff2"));
        links.add(generation1.newLink("asd1"));
        links.add(generation1.newLink("asd2"));
        generation2.newLink("notExpected1");
        generation2.newLink("notExpected2");

        List<IProductCmptLink> linksIncludingGenerations = generation1.getLinksIncludingProductCmpt();
        assertEquals(links, linksIncludingGenerations);
    }

    @Test
    public void testAddDependenciesFromFormulaExpressions() throws Exception {
        ProductCmptGeneration generationSpy = spy((ProductCmptGeneration)generation);
        IDependency dependency = mock(IDependency.class);
        ExpressionDependencyDetail dependencyDetail1 = mock(ExpressionDependencyDetail.class);
        ExpressionDependencyDetail dependencyDetail2 = mock(ExpressionDependencyDetail.class);
        IFormula formula1 = mock(IFormula.class);
        IFormula formula2 = mock(IFormula.class);
        when(generationSpy.getFormulas()).thenReturn(new IFormula[] { formula1, formula2 });
        Map<IDependency, IExpressionDependencyDetail> dependencyMap1 = new HashMap<>();
        dependencyMap1.put(dependency, dependencyDetail1);
        Map<IDependency, IExpressionDependencyDetail> dependencyMap2 = new HashMap<>();
        dependencyMap2.put(dependency, dependencyDetail2);
        when(formula1.dependsOn()).thenReturn(dependencyMap1);
        when(formula2.dependsOn()).thenReturn(dependencyMap2);

        Set<IDependency> dependenciesResult = new HashSet<>();
        Map<IDependency, List<IDependencyDetail>> detailsResult = new HashMap<>();
        generationSpy.dependsOn(dependenciesResult, detailsResult);

        assertEquals(1, dependenciesResult.size());
        assertThat(dependenciesResult, hasItem(dependency));
        assertEquals(1, detailsResult.size());
        assertThat(detailsResult.keySet(), hasItem(dependency));
        List<? extends IDependencyDetail> detailList = detailsResult.get(dependency);
        assertEquals(2, detailList.size());
        assertEquals(dependencyDetail1, detailList.get(0));
        assertEquals(dependencyDetail2, detailList.get(1));
    }

    @Test
    public void testAddRelatedTableContentsQualifiedNameTypes() {
        ProductCmptGeneration generationSpy = spy((ProductCmptGeneration)generation);
        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage = generation.newTableContentUsage(structureUsage);
        assertEquals("RateTable", contentUsage.getStructureUsage());

        Set<IDependency> dependenciesResult = new HashSet<>();
        Map<IDependency, List<IDependencyDetail>> detailsResult = new HashMap<>();
        generationSpy.dependsOn(dependenciesResult, detailsResult);

        assertEquals(1, dependenciesResult.size());
        assertEquals(1, detailsResult.size());
    }

    @Test
    public void testGetPropertyValuesIncludingProductCmpt() throws Exception {
        productCmptType.newProductCmptTypeAttribute("a1");
        IAttributeValue valueA1 = generation.newAttributeValue();
        valueA1.setAttribute("a1");

        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("a2");
        attribute.setChangingOverTime(false);
        IAttributeValue valueA2 = productCmpt.newPropertyValue(attribute, IAttributeValue.class);
        valueA2.setAttribute("a2");

        List<IAttributeValue> propertyValues = generation.getPropertyValuesIncludingProductCmpt(IAttributeValue.class);
        assertTrue(propertyValues.contains(valueA1));
        assertTrue(propertyValues.contains(valueA2));

        List<IAttributeValue> propertyValuesGen = generation.getPropertyValues(IAttributeValue.class);
        assertTrue(propertyValuesGen.contains(valueA1));
        assertFalse(propertyValuesGen.contains(valueA2));
    }

    @Test
    public void testIsTemplate_default() {
        IProductCmptGeneration gen = productCmpt.getProductCmptGeneration(0);

        assertThat(gen.isProductTemplate(), is(false));
    }

    @Test
    public void testIsTemplate_true() throws CoreRuntimeException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration gen = template.getProductCmptGeneration(0);

        assertThat(gen.isProductTemplate(), is(true));
    }

    @Test
    public void testIsUsingTemplate() throws CoreRuntimeException {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");

        productCmpt.setTemplate(template.getQualifiedName());
        IProductCmptGeneration gen = productCmpt.getProductCmptGeneration(0);

        assertThat(gen.isUsingTemplate(), is(true));
    }

    @Test
    public void testIsUsingTemplate_noTemplate() {
        IProductCmptGeneration gen = productCmpt.getProductCmptGeneration(0);

        assertThat(gen.isUsingTemplate(), is(false));
    }

    @Test
    public void testIsPartOfTemplateHierarchy_prodCmpt() throws CoreRuntimeException {
        IProductCmpt product = newProductCmpt(ipsProject, "product");
        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration();
        product.setTemplate(null);

        assertThat(generation.isPartOfTemplateHierarchy(), is(false));

        product.setTemplate("someTemplate");
        assertThat(generation.isPartOfTemplateHierarchy(), is(true));
    }

    @Test
    public void testIsPartOfTemplateHierarchy_template() throws CoreRuntimeException {
        IProductCmpt product = newProductTemplate(ipsProject, "product");
        IProductCmptGeneration generation = (IProductCmptGeneration)product.newGeneration();
        product.setTemplate(null);

        assertThat(generation.isPartOfTemplateHierarchy(), is(true));

        product.setTemplate("parentTemplate");
        assertThat(generation.isPartOfTemplateHierarchy(), is(true));
    }

    @Test
    public void testAddPartThis_ConfigElement() throws CoreRuntimeException {
        ProductCmpt product = newProductCmpt(productCmptType, "product");
        ProductCmptGeneration generation = (ProductCmptGeneration)product.newGeneration();
        IConfiguredDefault configDefault = generation.newPropertyValue(attribute, IConfiguredDefault.class);

        assertThat(generation.addPartThis(configDefault), is(true));
    }

    @Test
    public void testRemovePartThis_ConfigElement() throws CoreRuntimeException {
        ProductCmpt product = newProductCmpt(productCmptType, "product");
        ProductCmptGeneration generation = (ProductCmptGeneration)product.newGeneration();
        IConfiguredDefault configDefault = generation.newPropertyValue(attribute, IConfiguredDefault.class);

        assertThat(generation.removePartThis(configDefault), is(true));
        assertNull(generation.getPropertyValue(attribute, IConfiguredDefault.class));
    }
}
