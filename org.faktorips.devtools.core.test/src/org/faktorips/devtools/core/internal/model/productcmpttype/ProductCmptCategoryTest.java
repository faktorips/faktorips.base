/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptCategoryTest extends AbstractIpsPluginTest {

    private static final String CATEGORY_NAME = "foo";

    private IIpsProject ipsProject;

    private IPolicyCmptType policyType;

    private IProductCmptType productType;

    private ProductCmptCategory category;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();

        policyType = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");
        productType = policyType.findProductCmptType(ipsProject);

        category = (ProductCmptCategory)productType.newCategory(CATEGORY_NAME);
    }

    @Test
    public void testConstructor_InitializePropertiesToProperDefaultsOnCreation() {
        IProductCmptCategory category = productType.newCategory();
        assertEquals("", category.getName());
        assertFalse(category.isDefaultForFormulaSignatureDefinitions());
        assertFalse(category.isDefaultForPolicyCmptTypeAttributes());
        assertFalse(category.isDefaultForProductCmptTypeAttributes());
        assertFalse(category.isDefaultForTableStructureUsages());
        assertFalse(category.isDefaultForValidationRules());
        assertTrue(category.isAtLeftPosition());
    }

    @Test
    public void testIsDefaultFor_Property() {
        IProductCmptCategory defaultPolicyAttributes = productType.newCategory();
        IProductCmptCategory defaultProductAttributes = productType.newCategory();
        IProductCmptCategory defaultFormulas = productType.newCategory();
        IProductCmptCategory defaultTableStructures = productType.newCategory();
        IProductCmptCategory defaultValidationRules = productType.newCategory();

        defaultPolicyAttributes.setDefaultForPolicyCmptTypeAttributes(true);
        defaultProductAttributes.setDefaultForProductCmptTypeAttributes(true);
        defaultFormulas.setDefaultForFormulaSignatureDefinitions(true);
        defaultTableStructures.setDefaultForTableStructureUsages(true);
        defaultValidationRules.setDefaultForValidationRules(true);

        assertTrue(defaultPolicyAttributes.isDefaultFor(policyType.newPolicyCmptTypeAttribute()));
        assertTrue(defaultProductAttributes.isDefaultFor(productType.newProductCmptTypeAttribute()));

        IProductCmptTypeMethod formulaSignature = productType.newFormulaSignature("");
        assertTrue(defaultFormulas.isDefaultFor(formulaSignature));
        assertTrue(defaultTableStructures.isDefaultFor(productType.newTableStructureUsage()));
        assertTrue(defaultValidationRules.isDefaultFor(policyType.newRule()));

        formulaSignature.setFormulaSignatureDefinition(false);
        assertFalse(defaultFormulas.isDefaultFor(formulaSignature));
    }

    @Test
    public void testIsDefaultFor_PropertyType() {
        IProductCmptCategory defaultPolicyAttributes = productType.newCategory();
        IProductCmptCategory defaultProductAttributes = productType.newCategory();
        IProductCmptCategory defaultFormulas = productType.newCategory();
        IProductCmptCategory defaultTableStructures = productType.newCategory();
        IProductCmptCategory defaultValidationRules = productType.newCategory();

        defaultPolicyAttributes.setDefaultForPolicyCmptTypeAttributes(true);
        defaultProductAttributes.setDefaultForProductCmptTypeAttributes(true);
        defaultFormulas.setDefaultForFormulaSignatureDefinitions(true);
        defaultTableStructures.setDefaultForTableStructureUsages(true);
        defaultValidationRules.setDefaultForValidationRules(true);

        assertTrue(defaultPolicyAttributes.isDefaultFor(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE));
        assertTrue(defaultProductAttributes.isDefaultFor(ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE));
        assertTrue(defaultFormulas.isDefaultFor(ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION));
        assertTrue(defaultTableStructures.isDefaultFor(ProductCmptPropertyType.TABLE_STRUCTURE_USAGE));
        assertTrue(defaultValidationRules.isDefaultFor(ProductCmptPropertyType.VALIDATION_RULE));
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
    public void testFindIsContainingProperty() throws CoreException {
        // Create a super product type and a category in it
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory superCategory = superProductType.newCategory("superCategory");

        // Create a property in each type and assign them to the category
        IProductCmptProperty superProperty = superProductType.newProductCmptTypeAttribute("superProperty");
        IProductCmptProperty property = productType.newProductCmptTypeAttribute("property");
        superProperty.setCategory(superCategory.getName());
        property.setCategory(superCategory.getName());

        // Given the super type as context type, only the super property is contained
        assertTrue(superCategory.findIsContainingProperty(superProperty, superProductType, ipsProject));
        assertFalse(superCategory.findIsContainingProperty(property, superProductType, ipsProject));

        // Given the child type as context type, both properties are contained
        assertTrue(superCategory.findIsContainingProperty(superProperty, productType, ipsProject));
        assertTrue(superCategory.findIsContainingProperty(property, productType, ipsProject));
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * The category of policy cmpt attribute changes
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The findIsContainingProperty method should instantly find the new category although the
     * category was not stored in the attribute
     */
    @Test
    public void testFindIsContainingProperty_pendingChanges() throws CoreException {
        IProductCmptCategory category = productType.newCategory("newCategory");
        IProductCmptCategory defCategory = productType.newCategory("defCategory");
        defCategory.setDefaultForPolicyCmptTypeAttributes(true);

        IPolicyCmptTypeAttribute attribute = policyType.newPolicyCmptTypeAttribute();
        attribute.setValueSetConfiguredByProduct(true);

        assertTrue(StringUtils.isEmpty(attribute.getCategory()));

        assertFalse(category.findIsContainingProperty(attribute, productType, ipsProject));
        assertTrue(defCategory.findIsContainingProperty(attribute, productType, ipsProject));

        category.insertProductCmptProperty(attribute, null, true);
        assertTrue(StringUtils.isEmpty(attribute.getCategory()));

        assertTrue(category.findIsContainingProperty(attribute, productType, ipsProject));
        assertFalse(defCategory.findIsContainingProperty(attribute, productType, ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} that has no {@link IProductCmptCategory} is checked for
     * containment in the corresponding default {@link IProductCmptCategory}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The operation should return true as properties that have no {@link IProductCmptCategory} are
     * implicitly assigned to the corresponding default {@link IProductCmptCategory}.
     */
    @Test
    public void testFindIsContainingProperty_DefaultCategoryContainsPropertiesThatHaveNoCategory() throws CoreException {
        IProductCmptCategory defaultAttributeCategory = productType.newCategory("defaultAttribute");
        defaultAttributeCategory.setDefaultForProductCmptTypeAttributes(true);

        IProductCmptProperty attributeProperty = productType.newProductCmptTypeAttribute("attribute");

        assertTrue(defaultAttributeCategory.findIsContainingProperty(attributeProperty, productType, ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} is assigned to an {@link IProductCmptCategory} other than the
     * corresponding default {@link IProductCmptCategory}, but this other
     * {@link IProductCmptCategory} cannot be found because it does not exist.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The operation should return true as for not found categories, properties are implicitly
     * assigned to the corresponding default {@link IProductCmptCategory}.
     */
    @Test
    public void testFindIsContainingProperty_CategoryNotFound() throws CoreException {
        IProductCmptCategory defaultAttributeCategory = productType.newCategory("defaultAttribute");
        defaultAttributeCategory.setDefaultForProductCmptTypeAttributes(true);

        IProductCmptProperty attributeProperty = productType.newProductCmptTypeAttribute("attribute");
        attributeProperty.setCategory("foobar");

        assertTrue(defaultAttributeCategory.findIsContainingProperty(attributeProperty, productType, ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} is assigned to an {@link IProductCmptCategory} other than the
     * corresponding default {@link IProductCmptCategory} and this other
     * {@link IProductCmptCategory} can only be found in the product component type's supertype
     * hierarchy.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The operation should return false as categories defined in the supertype hierarchy must be
     * considered.
     */
    @Test
    public void testFindIsContainingProperty_CategoryDefinedInSupertype() throws CoreException {
        IProductCmptCategory defaultAttributeCategory = productType.newCategory("defaultAttribute");
        defaultAttributeCategory.setDefaultForProductCmptTypeAttributes(true);

        IProductCmptType superProdcutType = createSuperProductType(productType, "Super");
        IProductCmptCategory superCategory = superProdcutType.newCategory("superCategory");

        IProductCmptProperty attributeProperty = productType.newProductCmptTypeAttribute("attribute");
        attributeProperty.setCategory(superCategory.getName());

        assertFalse(defaultAttributeCategory.findIsContainingProperty(attributeProperty, productType, ipsProject));
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * A subtype of a product component type references the same policy component type. FIPS-969
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The policy cmpt attribute should be found only one times
     */
    @Test
    public void testFindIsContainingProperty_supertypeWithSamePolicy() throws CoreException {
        IProductCmptCategory defaultAttributeCategory = productType.newCategory("defaultAttribute");
        defaultAttributeCategory.setDefaultForPolicyCmptTypeAttributes(true);

        IProductCmptType superProdcutType = newProductCmptType(ipsProject, "SuperProdCmptType");
        superProdcutType.setPolicyCmptType(policyType.getQualifiedName());
        productType.setSupertype(superProdcutType.getQualifiedName());

        IPolicyCmptTypeAttribute attribute = policyType.newPolicyCmptTypeAttribute();
        attribute.setValueSetConfiguredByProduct(true);

        List<IProductCmptProperty> propertiesInCategory = defaultAttributeCategory.findProductCmptProperties(
                productType, true, ipsProject);
        assertEquals(1, propertiesInCategory.size());
        assertTrue(propertiesInCategory.contains(attribute));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} has no {@link IProductCmptCategory} but an
     * {@link IProductCmptCategory} with an empty name exists.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should not be assigned to the {@link IProductCmptCategory}
     * with empty name.
     */
    @Test
    public void testFindIsContainingProperty_CategoryNameIsEmpty() throws CoreException {
        IProductCmptCategory defaultAttributeCategory = productType.newCategory("defaultAttribute");
        defaultAttributeCategory.setDefaultForProductCmptTypeAttributes(true);
        IProductCmptCategory emptyNameCategory = productType.newCategory();

        IProductCmptProperty noCategoryProperty = productType.newProductCmptTypeAttribute("noCategoryProperty");

        assertFalse(emptyNameCategory.findIsContainingProperty(noCategoryProperty, productType, ipsProject));
        assertTrue(defaultAttributeCategory.findIsContainingProperty(noCategoryProperty, productType, ipsProject));
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

    @Test
    public void testCreateElement() {
        Document document = mock(Document.class);
        category.createElement(document);
        verify(document).createElement(ProductCmptCategory.XML_TAG_NAME);
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        category.setDefaultForFormulaSignatureDefinitions(true);
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);
        category.setDefaultForValidationRules(true);
        category.setPosition(Position.RIGHT);

        Element xmlElement = category.toXml(createXmlDocument(ProductCmptCategory.XML_TAG_NAME));
        IProductCmptCategory loadedCategory = productType.newCategory();
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
    public void testValidate_NameIsEmpty() throws CoreException {
        category.setName("");

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList, IProductCmptCategory.MSGCODE_NAME_IS_EMPTY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void testValidate_NameIsUsedTwiceInType() throws CoreException {
        productType.newCategory(CATEGORY_NAME);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void testValidate_NameIsUsedTwiceInTypeHierarchy() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        superProductType.newCategory(CATEGORY_NAME);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void testValidate_DuplicateCategoriesMarkedAsDefaultForFormulaSignatureDefinitions() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newCategory("bar");

        category.setDefaultForFormulaSignatureDefinitions(true);
        category2.setDefaultForFormulaSignatureDefinitions(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_FORMULA_SIGNATURE_DEFINITIONS, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS, Message.WARNING);
    }

    @Test
    public void testValidate_DuplicateCategoriesMarkedAsDefaultForValidationRules() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newCategory("bar");

        category.setDefaultForValidationRules(true);
        category2.setDefaultForValidationRules(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_VALIDATION_RULES, Message.WARNING);
    }

    @Test
    public void testValidate_DuplicateCategoriesMarkedAsDefaultForTableStructureUsages() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newCategory("bar");

        category.setDefaultForTableStructureUsages(true);
        category2.setDefaultForTableStructureUsages(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_TABLE_STRUCTURE_USAGES, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES, Message.WARNING);
    }

    @Test
    public void testValidate_DuplicateCategoriesMarkedAsDefaultForPolicyCmptTypeAttributes() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newCategory("bar");

        category.setDefaultForPolicyCmptTypeAttributes(true);
        category2.setDefaultForPolicyCmptTypeAttributes(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES, Message.WARNING);
    }

    @Test
    public void testValidate_DuplicateCategoriesMarkedAsDefaultForProductCmptTypeAttributes() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newCategory("bar");

        category.setDefaultForProductCmptTypeAttributes(true);
        category2.setDefaultForProductCmptTypeAttributes(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertEquals(IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                validationMessageList.getFirstMessage(Message.WARNING).getCode());
        assertEquals(1, validationMessageList.size());
    }

    /**
     * <strong>Scenario:</strong><br>
     * There are properties assigned to an {@link IProductCmptCategory} by an
     * {@link IProductCmptType} and it's supertype. Properties assigned by each type are moved
     * individually.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Each move operation should return the appropriate new indexes and the ordering of the
     * complete property list should correspond to the performed move operations.
     */
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

        assertArrayEquals(new int[] { 0 }, category.moveProductCmptProperties(new int[] { 1 }, true, superProductType));
        assertArrayEquals(new int[] { 1, 0 }, category.moveProductCmptProperties(new int[] { 2, 1 }, true, productType));

        List<IProductCmptProperty> superProperties = category.findProductCmptProperties(superProductType, true,
                ipsProject);
        assertEquals(superProperty2, superProperties.get(0));
        assertEquals(superProperty1, superProperties.get(1));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, true, ipsProject);
        assertEquals(superProperty2, properties.get(0));
        assertEquals(superProperty1, properties.get(1));
        assertEquals(property2, properties.get(2));
        assertEquals(property3, properties.get(3));
        assertEquals(property1, properties.get(4));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The indexes to be moved are not valid in relation to the provided context type.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An {@link IndexOutOfBoundsException} should be thrown.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testMoveProductCmptProperties_InvalidIndexesGiven() throws CoreException {
        category.moveProductCmptProperties(new int[] { 1 }, true, productType);
    }

    /**
     * <strong>Scenario:</strong><br>
     * The provided array containing the indexes which identify the properties to be moved is empty.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An empty array should be returned and no move should be performed.
     */
    @Test
    public void testMoveProductCmptProperties_EmptyIndexArrayGiven() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("p1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("p2");
        property2.setCategory(CATEGORY_NAME);

        assertArrayEquals(new int[0], category.moveProductCmptProperties(new int[0], true, productType));
        assertEquals(property1, category.findProductCmptProperties(productType, false, ipsProject).get(0));
        assertEquals(property2, category.findProductCmptProperties(productType, false, ipsProject).get(1));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The first {@link IProductCmptProperty} is moved up, together with another
     * {@link IProductCmptProperty}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An array equal to the provided index array should be returned and no move should be
     * performed.
     */
    @Test
    public void testMoveProductCmptProperties_DontMoveUpIfOneElementIsAtUpperLimit() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("p1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("p2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("p3");
        property3.setCategory(CATEGORY_NAME);

        assertArrayEquals(new int[] { 0, 2 }, category.moveProductCmptProperties(new int[] { 0, 2 }, true, productType));
        assertEquals(property1, category.findProductCmptProperties(productType, false, ipsProject).get(0));
        assertEquals(property2, category.findProductCmptProperties(productType, false, ipsProject).get(1));
        assertEquals(property3, category.findProductCmptProperties(productType, false, ipsProject).get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The last {@link IProductCmptProperty} is moved down, together with another
     * {@link IProductCmptProperty}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An array equal to the provided index array should be returned and no move should be
     * performed.
     */
    @Test
    public void testMoveProductCmptProperties_DontMoveDownIfOneElementIsAtLowerLimit() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("p1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("p2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("p3");
        property3.setCategory(CATEGORY_NAME);

        assertArrayEquals(new int[] { 0, 2 },
                category.moveProductCmptProperties(new int[] { 0, 2 }, false, productType));
        assertEquals(property1, category.findProductCmptProperties(productType, false, ipsProject).get(0));
        assertEquals(property2, category.findProductCmptProperties(productType, false, ipsProject).get(1));
        assertEquals(property3, category.findProductCmptProperties(productType, false, ipsProject).get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The first {@link IProductCmptProperty} is moved up. However, another
     * {@link IProductCmptProperty} in another {@link IProductCmptCategory} exists in the
     * {@link IProductCmptType} above the {@link IProductCmptCategory} under test.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An array equal to the provided index array should be returned and no move should be
     * performed.
     */
    @Test
    public void testMoveProductCmptProperties_DontMoveUpIfElementAtUpperCategoryLimit() throws CoreException {
        IProductCmptCategory aboveCategory = productType.newCategory("aboveCategory");
        IProductCmptCategory testCategory = productType.newCategory("testCategory");

        IProductCmptProperty aboveProperty = productType.newProductCmptTypeAttribute("aboveProperty");
        aboveProperty.setCategory(aboveCategory.getName());
        IProductCmptProperty testProperty = productType.newProductCmptTypeAttribute("testProperty");
        testProperty.setCategory(testCategory.getName());

        assertArrayEquals(new int[] { 0 }, testCategory.moveProductCmptProperties(new int[] { 0 }, true, productType));
    }

    /**
     * <strong>Scenario:</strong><br>
     * The last {@link IProductCmptProperty} assigned to the {@link IProductCmptCategory} is moved
     * down. However, another {@link IProductCmptProperty} assigned to another
     * {@link IProductCmptCategory} exists below this last {@link IProductCmptProperty}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * An array equal to the provided index array should be returned and no move should be
     * performed.
     */
    @Test
    public void testMoveProductCmptProperties_DontMoveDownIfElementAtLowerCategoryLimit() throws CoreException {
        IProductCmptCategory testCategory = productType.newCategory("testCategory");
        IProductCmptCategory belowCategory = productType.newCategory("belowCategory");

        IProductCmptProperty testProperty = productType.newProductCmptTypeAttribute("testProperty");
        testProperty.setCategory(testCategory.getName());
        IProductCmptProperty belowProperty = productType.newProductCmptTypeAttribute("belowProperty");
        belowProperty.setCategory(belowCategory.getName());

        assertArrayEquals(new int[] { 0 }, testCategory.moveProductCmptProperties(new int[] { 0 }, false, productType));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Insertion of an {@link IProductCmptProperty} below another {@link IProductCmptProperty} with
     * a higher index.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should be inserted below the {@link IProductCmptProperty}
     * with higher index and true should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_InsertBelowFromLowerIndex() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("property1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("property2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("property3");
        property3.setCategory(CATEGORY_NAME);

        assertTrue(category.insertProductCmptProperty(property1, property3, false));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property2, properties.get(0));
        assertEquals(property3, properties.get(1));
        assertEquals(property1, properties.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Insertion of an {@link IProductCmptProperty} below another {@link IProductCmptProperty} with
     * a lower index.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should be inserted below the {@link IProductCmptProperty}
     * with lower index and true should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_InsertBelowFromHigherIndex() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("property1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("property2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("property3");
        property3.setCategory(CATEGORY_NAME);

        assertTrue(category.insertProductCmptProperty(property3, property1, false));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property1, properties.get(0));
        assertEquals(property3, properties.get(1));
        assertEquals(property2, properties.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Insertion of an {@link IProductCmptProperty} with null as target.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} to be inserted should be positioned at the very end of the
     * {@link IProductCmptCategory} and true should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_TargetPropertyNull() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("property1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("property2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("property3");
        property3.setCategory(CATEGORY_NAME);

        assertTrue(category.insertProductCmptProperty(property1, null, false));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property2, properties.get(0));
        assertEquals(property3, properties.get(1));
        assertEquals(property1, properties.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} of another {@link IProductCmptCategory} is inserted below
     * another {@link IProductCmptProperty} of the target {@link IProductCmptCategory} with higher
     * index.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should be inserted below the {@link IProductCmptProperty} of
     * the target {@link IProductCmptCategory} and true should be returned.
     */
    @Test
    public void testInsertProductCmptPropertyBelow_InsertBelowFromAnotherCategoryWithLowerIndex() throws CoreException {
        IProductCmptCategory foreignCategory = productType.newCategory("foreignCategory");

        IProductCmptProperty foreignProperty = productType.newProductCmptTypeAttribute("foreignProperty");
        foreignProperty.setCategory(foreignCategory.getName());

        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("property1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("property2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("property3");
        property3.setCategory(CATEGORY_NAME);

        assertTrue(category.insertProductCmptProperty(foreignProperty, property2, false));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property1, properties.get(0));
        assertEquals(property2, properties.get(1));
        assertEquals(foreignProperty, properties.get(2));
        assertEquals(property3, properties.get(3));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} belonging to an {@link IPolicyCmptType} is inserted.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The category stored in the {@link IProductCmptProperty} should not be changed.
     */
    @Test
    public void testInsertProductCmptProperty_DoNotChangeCategoryStoredInPolicyProperty() throws CoreException {
        IPolicyCmptTypeAttribute policyProperty = policyType.newPolicyCmptTypeAttribute("policyProperty");
        policyProperty.setValueSetConfiguredByProduct(true);
        policyProperty.setCategory("beforeCategory");

        category.insertProductCmptProperty(policyProperty, null, false);

        assertEquals("beforeCategory", policyProperty.getCategory());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} from the policy side is inserted into an
     * {@link IProductCmptCategory} defined in the supertype hierarchy.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The category stored in the {@link IProductCmptProperty} should be changed as soon as the
     * configuring {@link IProductCmptType} is saved.
     */
    @Test
    public void testInsertProductCmptProperty_DoChangeCategoryStoredInPolicyPropertyOnSaveOfConfiguringProductCmptType()
            throws CoreException {

        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category = superProductType.newCategory("testCategory");

        IPolicyCmptTypeAttribute policyProperty = policyType.newPolicyCmptTypeAttribute("policyProperty");
        policyProperty.setValueSetConfiguredByProduct(true);

        category.insertProductCmptProperty(policyProperty, null, false);

        /*
         * The category should not change if another type of the hierarchy is saved which does not
         * configure the policy component type.
         */
        superProductType.getIpsSrcFile().save(true, null);
        assertEquals("", policyProperty.getCategory());

        productType.getIpsSrcFile().save(true, null);
        assertEquals(category.getName(), policyProperty.getCategory());
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} is inserted above another {@link IProductCmptProperty} with
     * higher index.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should be inserted above the {@link IProductCmptProperty}
     * with higher index and true should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_InsertAboveFromLowerIndex() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("property1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("property2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("property3");
        property3.setCategory(CATEGORY_NAME);

        assertTrue(category.insertProductCmptProperty(property1, property3, true));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property2, properties.get(0));
        assertEquals(property1, properties.get(1));
        assertEquals(property3, properties.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} is inserted above another {@link IProductCmptProperty} with
     * lower index.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should be inserted above the {@link IProductCmptProperty}
     * with lower index and true should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_InsertAboveFromHigherIndex() throws CoreException {
        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("property1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("property2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("property3");
        property3.setCategory(CATEGORY_NAME);

        assertTrue(category.insertProductCmptProperty(property3, property1, true));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property3, properties.get(0));
        assertEquals(property1, properties.get(1));
        assertEquals(property2, properties.get(2));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} of another {@link IProductCmptCategory} is inserted above
     * another {@link IProductCmptProperty} of the target {@link IProductCmptCategory} with lower
     * index.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link IProductCmptProperty} should be inserted above the {@link IProductCmptProperty} of
     * the target {@link IProductCmptCategory} and true should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_InsertAboveFromAnotherCategoryWithHigherIndex() throws CoreException {
        IProductCmptCategory foreignCategory = productType.newCategory("foreignCategory");

        IProductCmptProperty property1 = productType.newProductCmptTypeAttribute("property1");
        property1.setCategory(CATEGORY_NAME);
        IProductCmptProperty property2 = productType.newProductCmptTypeAttribute("property2");
        property2.setCategory(CATEGORY_NAME);
        IProductCmptProperty property3 = productType.newProductCmptTypeAttribute("property3");
        property3.setCategory(CATEGORY_NAME);

        IProductCmptProperty foreignProperty = productType.newProductCmptTypeAttribute("foreignProperty");
        foreignProperty.setCategory(foreignCategory.getName());

        assertTrue(category.insertProductCmptProperty(foreignProperty, property2, true));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property1, properties.get(0));
        assertEquals(foreignProperty, properties.get(1));
        assertEquals(property2, properties.get(2));
        assertEquals(property3, properties.get(3));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} is inserted into an {@link IProductCmptCategory} but the
     * index of the {@link IProductCmptProperty} in the context {@link IProductCmptType} cannot be
     * determined. This can happen if the {@link IProductCmptProperty} originates from the policy
     * side but the {@link IPolicyCmptType} cannot be found.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Nothing should happen and false should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_IndexOfPropertyToBeInsertedCannotBeDetermined() throws CoreException {
        IProductCmptCategory category = productType.newCategory("myCategory");

        IProductCmptProperty property = productType.newProductCmptTypeAttribute("property");
        property.setCategory(category.getName());
        IPolicyCmptTypeAttribute policyProperty = policyType.newPolicyCmptTypeAttribute("policyProperty");
        policyProperty.setValueSetConfiguredByProduct(true);
        policyProperty.setCategory(category.getName());

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property, properties.get(0));
        assertEquals(policyProperty, properties.get(1));

        productType.setPolicyCmptType("");
        assertFalse(category.insertProductCmptProperty(policyProperty, property, true));
        productType.setPolicyCmptType(policyType.getQualifiedName());

        List<IProductCmptProperty> movedProperties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property, movedProperties.get(0));
        assertEquals(policyProperty, movedProperties.get(1));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} is inserted into an {@link IProductCmptCategory} but the
     * index of the target {@link IProductCmptProperty} in the context {@link IProductCmptType}
     * cannot be determined. This can happen if the {@link IProductCmptProperty} originates from the
     * policy side but the {@link IPolicyCmptType} cannot be found.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Nothing should happen and false should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_IndexOfTargetPropertyCannotBeDetermined() throws CoreException {
        IProductCmptCategory category = productType.newCategory("myCategory");

        IProductCmptProperty property = productType.newProductCmptTypeAttribute("property");
        property.setCategory(category.getName());
        IPolicyCmptTypeAttribute policyProperty = policyType.newPolicyCmptTypeAttribute("policyProperty");
        policyProperty.setValueSetConfiguredByProduct(true);
        policyProperty.setCategory(category.getName());

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property, properties.get(0));
        assertEquals(policyProperty, properties.get(1));

        productType.setPolicyCmptType("");
        assertFalse(category.insertProductCmptProperty(property, policyProperty, false));
        productType.setPolicyCmptType(policyType.getQualifiedName());

        List<IProductCmptProperty> movedProperties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property, movedProperties.get(0));
        assertEquals(policyProperty, movedProperties.get(1));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} should be inserted into an {@link IProductCmptCategory} but
     * the {@link IProductCmptType} the {@link IProductCmptProperty} belongs to cannot be found.
     * Therefore, the indices of the moved properties cannot be obtained.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Nothing should happen and false should be returned.
     */
    @Test
    public void testInsertProductCmptProperty_ContextTypeCannotBeFound() throws CoreException {
        IProductCmptCategory category = productType.newCategory("myCategory");

        IProductCmptProperty property = productType.newProductCmptTypeAttribute("property");
        property.setCategory(category.getName());
        IPolicyCmptTypeAttribute policyProperty = policyType.newPolicyCmptTypeAttribute("policyProperty");
        policyProperty.setValueSetConfiguredByProduct(true);
        policyProperty.setCategory(category.getName());

        policyType.setProductCmptType("");
        assertFalse(category.insertProductCmptProperty(policyProperty, property, true));

        List<IProductCmptProperty> properties = category.findProductCmptProperties(productType, false, ipsProject);
        assertEquals(property, properties.get(0));
        assertEquals(policyProperty, properties.get(1));
    }

    /**
     * <strong>Scenario:</strong><br>
     * An {@link IProductCmptProperty} that is assigned to an {@link IProductCmptCategory} is
     * inserted at beginning of another {@link IProductCmptCategory}.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only a single change event of type {@link ContentChangeEvent#TYPE_WHOLE_CONTENT_CHANGED}
     * should be fired.
     */
    @Test
    public void testInsertProductCmptProperty_FireOnlyASingleChangeEvent() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory sourceCategory = superProductType.newCategory("sourceCategory");
        IProductCmptCategory targetCategory = superProductType.newCategory("targetCategory");

        IProductCmptProperty sourceProperty = productType.newProductCmptTypeAttribute("sourceProperty");
        IProductCmptProperty p1 = productType.newProductCmptTypeAttribute("p1");
        IProductCmptProperty p2 = productType.newProductCmptTypeAttribute("p2");
        IProductCmptProperty p3 = productType.newProductCmptTypeAttribute("p3");
        sourceProperty.setCategory(sourceCategory.getName());
        p1.setCategory(targetCategory.getName());
        p2.setCategory(targetCategory.getName());
        p3.setCategory(targetCategory.getName());

        resetNumberContentChangeEvents();

        targetCategory.insertProductCmptProperty(sourceProperty, p1, true);

        assertWholeContentChangedEvent(productType.getIpsSrcFile());
        assertSingleContentChangeEvent();
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
