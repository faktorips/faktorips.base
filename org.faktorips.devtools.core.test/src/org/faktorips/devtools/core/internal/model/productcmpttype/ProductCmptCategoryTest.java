/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptCategoryTest extends AbstractIpsPluginTest {

    private static final String CATEGORY_NAME = "foo";

    private IIpsProject ipsProject;

    private IProductCmptType productType;

    private ProductCmptCategory category;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();

        IPolicyCmptType policyType = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");
        productType = policyType.findProductCmptType(ipsProject);
        category = (ProductCmptCategory)productType.newProductCmptCategory(CATEGORY_NAME);
    }

    @Test
    public void testInitializePropertiesToProperDefaultsOnCreation() {
        IProductCmptCategory category = productType.newProductCmptCategory();
        assertEquals("", category.getName());
        assertFalse(category.isDefaultForFormulaSignatureDefinitions());
        assertFalse(category.isDefaultForPolicyCmptTypeAttributes());
        assertFalse(category.isDefaultForProductCmptTypeAttributes());
        assertFalse(category.isDefaultForTableStructureUsages());
        assertFalse(category.isDefaultForValidationRules());
        assertTrue(category.isAtLeftPosition());
    }

    @Test
    public void testGetProductCmptType() {
        assertEquals(productType, category.getProductCmptType());
    }

    @Test
    public void testSetName() {
        category.setName("bar");

        assertEquals("bar", category.getName());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_NAME, CATEGORY_NAME, "bar");
    }

    @Test
    public void testSetDefaultForFormulaSignatureDefinitions() {
        category.setDefaultForFormulaSignatureDefinitions(true);
        assertTrue(category.isDefaultForFormulaSignatureDefinitions());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                false, true);

        category.setDefaultForFormulaSignatureDefinitions(false);
        assertFalse(category.isDefaultForFormulaSignatureDefinitions());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                true, false);
    }

    @Test
    public void testSetDefaultForPolicyCmptTypeAttributes() {
        category.setDefaultForPolicyCmptTypeAttributes(true);
        assertTrue(category.isDefaultForPolicyCmptTypeAttributes());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                false, true);

        category.setDefaultForPolicyCmptTypeAttributes(false);
        assertFalse(category.isDefaultForPolicyCmptTypeAttributes());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                true, false);
    }

    @Test
    public void testSetDefaultForProductCmptTypeAttributes() {
        category.setDefaultForProductCmptTypeAttributes(true);
        assertTrue(category.isDefaultForProductCmptTypeAttributes());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                false, true);

        category.setDefaultForProductCmptTypeAttributes(false);
        assertFalse(category.isDefaultForProductCmptTypeAttributes());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                true, false);
    }

    @Test
    public void testSetDefaultForTableStructureUsages() {
        category.setDefaultForTableStructureUsages(true);
        assertTrue(category.isDefaultForTableStructureUsages());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES, false,
                true);

        category.setDefaultForTableStructureUsages(false);
        assertFalse(category.isDefaultForTableStructureUsages());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES, true,
                false);
    }

    @Test
    public void testSetDefaultForValidationRules() {
        category.setDefaultForValidationRules(true);
        assertTrue(category.isDefaultForValidationRules());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_VALIDATION_RULES, false, true);

        category.setDefaultForValidationRules(false);
        assertFalse(category.isDefaultForValidationRules());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_DEFAULT_FOR_VALIDATION_RULES, true, false);
    }

    @Test
    public void testSetPosition() {
        category.setPosition(Position.RIGHT);
        assertEquals(Position.RIGHT, category.getPosition());
        assertTrue(category.isAtRightPosition());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_POSITION, Position.LEFT, Position.RIGHT);

        category.setPosition(Position.LEFT);
        assertEquals(Position.LEFT, category.getPosition());
        assertTrue(category.isAtLeftPosition());
        assertPropertyChangedEvent(category, IProductCmptCategory.PROPERTY_POSITION, Position.RIGHT, Position.LEFT);
    }

    @Test(expected = NullPointerException.class)
    public void testSetPositionNull() {
        category.setPosition(null);
    }

    @Test
    public void testCreateElement() {
        Document document = mock(Document.class);
        category.createElement(document);
        verify(document).createElement(IProductCmptCategory.XML_TAG_NAME);
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        category.setDefaultForFormulaSignatureDefinitions(true);
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);
        category.setDefaultForValidationRules(true);
        category.setPosition(Position.RIGHT);

        Element xmlElement = category.toXml(createXmlDocument(IProductCmptCategory.XML_TAG_NAME));
        IProductCmptCategory loadedCategory = productType.newProductCmptCategory();
        loadedCategory.initFromXml(xmlElement);

        assertEquals(CATEGORY_NAME, loadedCategory.getName());
        assertTrue(loadedCategory.isDefaultForFormulaSignatureDefinitions());
        assertTrue(loadedCategory.isDefaultForPolicyCmptTypeAttributes());
        assertTrue(loadedCategory.isDefaultForProductCmptTypeAttributes());
        assertTrue(loadedCategory.isDefaultForTableStructureUsages());
        assertTrue(loadedCategory.isDefaultForValidationRules());
        assertEquals(Position.RIGHT, loadedCategory.getPosition());
    }

    @Test
    public void testValidateNameIsEmpty() throws CoreException {
        category.setName("");

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList, IProductCmptCategory.MSGCODE_NAME_IS_EMPTY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void testValidateNameIsUsedTwiceInType() throws CoreException {
        productType.newProductCmptCategory(CATEGORY_NAME);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void testValidateNameIsUsedTwiceInTypeHierarchy() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        superProductType.newProductCmptCategory(CATEGORY_NAME);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void testValidateDuplicateCategoriesMarkedAsDefaultForFormulaSignatureDefinitions() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newProductCmptCategory("bar");

        category.setDefaultForFormulaSignatureDefinitions(true);
        category2.setDefaultForFormulaSignatureDefinitions(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_FORMULA_SIGNATURE_DEFINITIONS, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS, Message.WARNING);
    }

    @Test
    public void testValidateDuplicateCategoriesMarkedAsDefaultForValidationRules() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newProductCmptCategory("bar");

        category.setDefaultForValidationRules(true);
        category2.setDefaultForValidationRules(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_VALIDATION_RULES, Message.WARNING);
    }

    @Test
    public void testValidateDuplicateCategoriesMarkedAsDefaultForTableStructureUsages() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newProductCmptCategory("bar");

        category.setDefaultForTableStructureUsages(true);
        category2.setDefaultForTableStructureUsages(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_TABLE_STRUCTURE_USAGES, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES, Message.WARNING);
    }

    @Test
    public void testValidateDuplicateCategoriesMarkedAsDefaultForPolicyCmptTypeAttributes() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newProductCmptCategory("bar");

        category.setDefaultForPolicyCmptTypeAttributes(true);
        category2.setDefaultForPolicyCmptTypeAttributes(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, Message.WARNING);
    }

    @Test
    public void testValidateDuplicateCategoriesMarkedAsDefaultForProductCmptTypeAttributes() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newProductCmptCategory("bar");

        category.setDefaultForProductCmptTypeAttributes(true);
        category2.setDefaultForProductCmptTypeAttributes(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertEquals(IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                validationMessageList.getFirstMessage(Message.WARNING).getCode());
        assertEquals(1, validationMessageList.size());
    }

    @Test
    public void testFindProductCmptProperties() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptType superSuperProductType = createSuperProductType(superProductType, "SuperSuper");

        IProductCmptProperty superSuperProperty = superSuperProductType
                .newProductCmptTypeAttribute("superSuperProperty");
        IProductCmptProperty superProperty = superProductType.newProductCmptTypeAttribute("superProperty");
        IProductCmptProperty property = productType.newProductCmptTypeAttribute("property");

        IProductCmptCategory superSuperCategory = superSuperProductType.newProductCmptCategory("superSuperCategory");
        superSuperProperty.setCategory(superSuperCategory.getName());
        superProperty.setCategory(superSuperCategory.getName());
        property.setCategory(superSuperCategory.getName());

        List<IProductCmptProperty> superSuperProductTypeProperties = superSuperCategory.findProductCmptProperties(
                superSuperProductType, ipsProject);
        assertEquals(superSuperProperty, superSuperProductTypeProperties.get(0));
        assertEquals(1, superSuperProductTypeProperties.size());

        List<IProductCmptProperty> superProductTypeProperties = superSuperCategory.findProductCmptProperties(
                superProductType, ipsProject);
        assertEquals(superSuperProperty, superProductTypeProperties.get(0));
        assertEquals(superProperty, superProductTypeProperties.get(1));
        assertEquals(2, superProductTypeProperties.size());

        List<IProductCmptProperty> productTypeProperties = superSuperCategory.findProductCmptProperties(productType,
                ipsProject);
        assertEquals(superSuperProperty, productTypeProperties.get(0));
        assertEquals(superProperty, productTypeProperties.get(1));
        assertEquals(property, productTypeProperties.get(2));
        assertEquals(3, productTypeProperties.size());
    }

    @Test
    public void testMoveProductCmptProperties() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");

        IProductCmptProperty superProperty1 = superProductType.newProductCmptTypeAttribute("s1");
        superProperty1.setCategory(CATEGORY_NAME);
        IProductCmptProperty superProperty2 = superProductType.newProductCmptTypeAttribute("s2");
        superProperty2.setCategory(CATEGORY_NAME);

        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("a1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("a2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("a3");
        property3.setCategory(CATEGORY_NAME);

        boolean moved1 = category.moveProductCmptProperties(Arrays.asList(superProperty2), true);
        boolean moved2 = category.moveProductCmptProperties(Arrays.asList(property3, property2), true);
        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, ipsProject);

        assertTrue(moved1);
        assertTrue(moved2);
        assertEquals(superProperty2, properties.get(0));
        assertEquals(superProperty1, properties.get(1));
        assertEquals(property2, properties.get(2));
        assertEquals(property3, properties.get(3));
        assertEquals(property1, properties.get(4));
    }

    @Test
    public void testMoveProductCmptPropertiesEmptyPropertyListGiven() throws CoreException {
        assertFalse(category.moveProductCmptProperties(new ArrayList<IProductCmptProperty>(0), true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveProductCmptPropertiesDifferentProductCmptTypes() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptProperty property1 = superProductType.newProductCmptTypeAttribute("p1");
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("p2");

        category.moveProductCmptProperties(Arrays.asList(property1, property2), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveProductCmptPropertiesDifferentPolicyCmptTypes() throws CoreException {
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");

        IProductCmptProperty property1 = policyType1.newPolicyCmptTypeAttribute("p1");
        IProductCmptProperty property2 = policyType2.newPolicyCmptTypeAttribute("p2");

        category.moveProductCmptProperties(Arrays.asList(property1, property2), true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveProductCmptPropertiesDifferentPolicyAndProductCmptTypes() throws CoreException {
        IPolicyCmptType policyType = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");

        IProductCmptProperty property1 = policyType.newPolicyCmptTypeAttribute("p1");
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("p2");

        category.moveProductCmptProperties(Arrays.asList(property1, property2), true);
    }

    @Test
    public void testMoveProductCmptPropertiesDontMoveIfElementAtLimit() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("p1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("p2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("p3");
        property3.setCategory(CATEGORY_NAME);

        assertFalse(category.moveProductCmptProperties(Arrays.asList(property1, property2), true));
        assertEquals(property1, category.findProductCmptProperties(productType, ipsProject).get(0));
        assertEquals(property2, category.findProductCmptProperties(productType, ipsProject).get(1));
        assertEquals(property3, category.findProductCmptProperties(productType, ipsProject).get(2));

        assertFalse(category.moveProductCmptProperties(Arrays.asList(property2, property3), false));
        assertEquals(property1, category.findProductCmptProperties(productType, ipsProject).get(0));
        assertEquals(property2, category.findProductCmptProperties(productType, ipsProject).get(1));
        assertEquals(property3, category.findProductCmptProperties(productType, ipsProject).get(2));
    }

    private IProductCmptType createSuperProductType(IProductCmptType productType, String prefix) throws CoreException {
        IPolicyCmptType superPolicyType = newPolicyAndProductCmptType(ipsProject, prefix + "PolicyType", prefix
                + "ProductType");
        IProductCmptType superProductType = superPolicyType.findProductCmptType(ipsProject);
        productType.setSupertype(superProductType.getQualifiedName());
        productType.findPolicyCmptType(ipsProject).setSupertype(superPolicyType.getQualifiedName());
        return superProductType;
    }

}
