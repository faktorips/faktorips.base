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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProductCmptCategoryTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private static final String CATEGORY_NAME = "foo";

    private ContentChangeEvent lastEvent;

    private IIpsProject ipsProject;

    private IProductCmptType productType;

    private IProductCmptTypeAttribute property;

    private IProductCmptCategory category;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        lastEvent = null;
        ipsProject = newIpsProject();
        ipsProject.getIpsModel().addChangeListener(this);

        IPolicyCmptType policyType = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");
        productType = policyType.findProductCmptType(ipsProject);
        category = productType.newProductCmptCategory(CATEGORY_NAME);
        property = createProductCmptTypeAttributeProperty(productType, "property");
    }

    @Override
    protected void tearDownExtension() throws Exception {
        ipsProject.getIpsModel().removeChangeListener(this);
    }

    @Test
    public void shouldInitializePropertiesToProperDefaultsOnCreation() {
        IProductCmptCategory category = productType.newProductCmptCategory();
        assertEquals("", category.getName());
        assertFalse(category.isInherited());
        assertFalse(category.isDefaultForFormulaSignatureDefinitions());
        assertFalse(category.isDefaultForPolicyCmptTypeAttributes());
        assertFalse(category.isDefaultForProductCmptTypeAttributes());
        assertFalse(category.isDefaultForTableStructureUsages());
        assertFalse(category.isDefaultForValidationRules());
        assertTrue(category.isAtLeftPosition());
    }

    @Test
    public void shouldReturnParentProductCmptType() {
        assertEquals(productType, category.getProductCmptType());
    }

    @Test
    public void shouldAllowToSetName() {
        category.setName("bar");

        assertEquals("bar", category.getName());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToReferenceProductCmptTypeAttribute() {
        IProductCmptTypeAttribute attributeProperty = createProductCmptTypeAttributeProperty(productType,
                "attributeProperty");

        assertNotNull(category.newProductCmptPropertyReference(attributeProperty));
        assertTrue(category.isReferencedAndPersistedProductCmptProperty(attributeProperty));
    }

    @Test
    public void shouldAllowToReferencePolicyCmptTypeAttribute() throws CoreException {
        IPolicyCmptTypeAttribute attributeProperty = createPolicyCmptTypeAttributeProperty(productType,
                "attributeProperty");

        assertNotNull(category.newProductCmptPropertyReference(attributeProperty));
        assertTrue(category.isReferencedAndPersistedProductCmptProperty(attributeProperty));
    }

    @Test
    public void shouldAllowToReferenceProductCmptTypeMethod() {
        IProductCmptTypeMethod methodProperty = createFormulaSignatureDefinitionProperty(productType, "methodProperty");

        assertNotNull(category.newProductCmptPropertyReference(methodProperty));
        assertTrue(category.isReferencedAndPersistedProductCmptProperty(methodProperty));
    }

    @Test
    public void shouldAllowToReferenceTableStructureUsage() {
        ITableStructureUsage tableStructureProperty = createTableStructureUsageProperty(productType,
                "tableStructureProperty");

        assertNotNull(category.newProductCmptPropertyReference(tableStructureProperty));
        assertTrue(category.isReferencedAndPersistedProductCmptProperty(tableStructureProperty));
    }

    @Test
    public void shouldAllowToReferenceValidationRule() throws CoreException {
        IValidationRule validationRuleProperty = createValidationRuleProperty(productType, "validationRuleProperty");

        assertNotNull(category.newProductCmptPropertyReference(validationRuleProperty));
        assertTrue(category.isReferencedAndPersistedProductCmptProperty(validationRuleProperty));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingProductCmptTypeAttributeFromForeignProductCmptType()
            throws CoreException {

        IProductCmptType otherProductCmptType = newProductCmptType(ipsProject, "OtherProductCmptType");
        IProductCmptTypeAttribute foreignAttributeProperty = createProductCmptTypeAttributeProperty(
                otherProductCmptType, "foreignAttribute");
        category.newProductCmptPropertyReference(foreignAttributeProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingPolicyCmptTypeAttributeFromForeignPolicyCmptType()
            throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "ForeignPolicy");
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setProductRelevant(true);
        category.newProductCmptPropertyReference(attribute);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingProductCmptTypeMethodFromForeignProductCmptType()
            throws CoreException {

        IProductCmptType otherProductCmptType = newProductCmptType(ipsProject, "OtherProductCmptType");
        IProductCmptTypeMethod foreignMethodProperty = createFormulaSignatureDefinitionProperty(otherProductCmptType,
                "foreignMethod");
        category.newProductCmptPropertyReference(foreignMethodProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingValidationRuleFromForeignPolicyCmptType()
            throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "ForeignPolicy");
        IValidationRule foreignValidationRule = policyCmptType.newRule();
        category.newProductCmptPropertyReference(foreignValidationRule);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenReferencingTableStructureUsageFromForeignProductCmptType()
            throws CoreException {

        IProductCmptType otherProductCmptType = newProductCmptType(ipsProject, "OtherProductCmptType");
        ITableStructureUsage foreignTsuProperty = createTableStructureUsageProperty(otherProductCmptType, "foreignTsu");
        category.newProductCmptPropertyReference(foreignTsuProperty);
    }

    @Test
    public void shouldAllowToDeleteProductCmptPropertyReference() {
        category.newProductCmptPropertyReference(property);
        boolean removed = category.deleteProductCmptPropertyReference(property);

        assertTrue(removed);
        assertFalse(category.isReferencedAndPersistedProductCmptProperty(property));
    }

    @Test
    public void shouldOnlyDeleteOneReferencePerDeleteCall() {
        category.newProductCmptPropertyReference(property);
        category.newProductCmptPropertyReference(property);

        category.deleteProductCmptPropertyReference(property);

        assertTrue(category.isReferencedAndPersistedProductCmptProperty(property));
        assertEquals(1, category.getNumberOfProductCmptPropertyReferences());
    }

    @Test
    public void shouldReturnFalseWhenDeletingNotReferencedProductCmptProperty() {
        boolean removed = category.deleteProductCmptPropertyReference(property);
        assertFalse(removed);
    }

    @Test
    public void shouldReturnTheCorrectNumberOfProductCmptPropertyReferences() {
        category.newProductCmptPropertyReference(property);
        category.newProductCmptPropertyReference(property);
        category.newProductCmptPropertyReference(property);

        assertEquals(3, category.getNumberOfProductCmptPropertyReferences());
    }

    @Test
    public void shouldAllowToSetInheritedProperty() {
        category.setInherited(true);
        assertTrue(category.isInherited());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setInherited(false);
        assertFalse(category.isInherited());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForMethods() {
        category.setDefaultForFormulaSignatureDefinitions(true);
        assertTrue(category.isDefaultForFormulaSignatureDefinitions());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForFormulaSignatureDefinitions(false);
        assertFalse(category.isDefaultForFormulaSignatureDefinitions());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForPolicyCmptTypeAttributes() {
        category.setDefaultForPolicyCmptTypeAttributes(true);
        assertTrue(category.isDefaultForPolicyCmptTypeAttributes());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForPolicyCmptTypeAttributes(false);
        assertFalse(category.isDefaultForPolicyCmptTypeAttributes());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForProductCmptTypeAttributes() {
        category.setDefaultForProductCmptTypeAttributes(true);
        assertTrue(category.isDefaultForProductCmptTypeAttributes());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForProductCmptTypeAttributes(false);
        assertFalse(category.isDefaultForProductCmptTypeAttributes());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForTableStructureUsages() {
        category.setDefaultForTableStructureUsages(true);
        assertTrue(category.isDefaultForTableStructureUsages());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForTableStructureUsages(false);
        assertFalse(category.isDefaultForTableStructureUsages());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForValidationRules() {
        category.setDefaultForValidationRules(true);
        assertTrue(category.isDefaultForValidationRules());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setDefaultForValidationRules(false);
        assertFalse(category.isDefaultForValidationRules());
        assertPropertyChangedEvent();
    }

    @Test
    public void shouldAllowToSetPosition() {
        category.setPosition(Position.RIGHT);
        assertEquals(Position.RIGHT, category.getPosition());
        assertTrue(category.isAtRightPosition());
        assertPropertyChangedEvent();

        resetContentChangedEvent();

        category.setPosition(Position.LEFT);
        assertEquals(Position.LEFT, category.getPosition());
        assertTrue(category.isAtLeftPosition());
        assertPropertyChangedEvent();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenSettingNullAsPosition() {
        category.setPosition(null);
    }

    @Test
    public void shouldCreateTemporaryReferencesForNotAssignedProductCmptTypeAttributesIfIsCorrespondingDefaultCategory()
            throws CoreException {

        category.newProductCmptPropertyReference(property);

        IProductCmptCategory productAttributeCategory = createDefaultCategoryForProductCmptTypeAttributes();
        IProductCmptTypeAttribute attribute1 = createProductCmptTypeAttributeProperty(productType, "attribute1");
        IProductCmptTypeAttribute attribute2 = createProductCmptTypeAttributeProperty(productType, "attribute2");
        IProductCmptTypeAttribute attribute3 = createProductCmptTypeAttributeProperty(productType, "attribute3");

        List<IProductCmptPropertyReference> references = productAttributeCategory
                .findProductCmptPropertyReferences(ipsProject);
        assertEquals(attribute1, references.get(0).findReferencedProductCmptProperty(ipsProject));
        assertEquals(attribute2, references.get(1).findReferencedProductCmptProperty(ipsProject));
        assertEquals(attribute3, references.get(2).findReferencedProductCmptProperty(ipsProject));
        assertEquals(3, references.size());
    }

    @Test
    public void shouldNotCreateNewTemporaryReferenceObjectForUnchangedProductCmptTypeAttributes() throws CoreException {
        IProductCmptCategory productAttributeCategory = createDefaultCategoryForProductCmptTypeAttributes();
        createProductCmptTypeAttributeProperty(productType, "attribute");

        List<IProductCmptPropertyReference> references1 = productAttributeCategory
                .findProductCmptPropertyReferences(ipsProject);
        List<IProductCmptPropertyReference> references2 = productAttributeCategory
                .findProductCmptPropertyReferences(ipsProject);
        assertEquals(references1, references2);
    }

    @Test
    public void shouldCreateTemporaryReferencesForNotAssignedFormulaSignatureDefinitionsIfIsCorrespondingDefaultCategory()
            throws CoreException {

        IProductCmptTypeMethod formulaProperty = createFormulaSignatureDefinitionProperty(productType,
                "formulaProperty");
        category.newProductCmptPropertyReference(formulaProperty);

        IProductCmptCategory formulaCategory = createDefaultCategoryForFormulaSignatureDefinitions();
        IProductCmptTypeMethod formula1 = createFormulaSignatureDefinitionProperty(productType, "formula1");
        IProductCmptTypeMethod formula2 = createFormulaSignatureDefinitionProperty(productType, "formula2");
        IProductCmptTypeMethod formula3 = createFormulaSignatureDefinitionProperty(productType, "formula3");

        List<IProductCmptPropertyReference> references = formulaCategory.findProductCmptPropertyReferences(ipsProject);
        assertEquals(formula1, references.get(0).findReferencedProductCmptProperty(ipsProject));
        assertEquals(formula2, references.get(1).findReferencedProductCmptProperty(ipsProject));
        assertEquals(formula3, references.get(2).findReferencedProductCmptProperty(ipsProject));
        assertEquals(3, references.size());
    }

    @Test
    public void shouldNotCreateTemporaryReferencesForNotAssignedProductCmptTypeMethodsThatAreNoFormulaSignatureDefinitions()
            throws CoreException {

        IProductCmptTypeMethod notAFormulaMethod = createFormulaSignatureDefinitionProperty(productType, "notAFormula");
        notAFormulaMethod.setFormulaSignatureDefinition(false);
        IProductCmptCategory formulaCategory = createDefaultCategoryForFormulaSignatureDefinitions();

        List<IProductCmptPropertyReference> references = formulaCategory.findProductCmptPropertyReferences(ipsProject);
        assertTrue(references.isEmpty());
    }

    @Test
    public void shouldNotCreateNewTemporaryReferenceObjectForUnchangedFormulaSignatureDefinitions()
            throws CoreException {

        IProductCmptCategory formulaCategory = createDefaultCategoryForFormulaSignatureDefinitions();
        createFormulaSignatureDefinitionProperty(productType, "formula");

        List<IProductCmptPropertyReference> references1 = formulaCategory.findProductCmptPropertyReferences(ipsProject);
        List<IProductCmptPropertyReference> references2 = formulaCategory.findProductCmptPropertyReferences(ipsProject);
        assertEquals(references1, references2);
    }

    @Test
    public void shouldCreateTemporaryReferencesForNotAssignedTableStructureUsagesIfIsCorrespondingDefaultCategory()
            throws CoreException {

        ITableStructureUsage tsuProperty = createTableStructureUsageProperty(productType, "tsuProperty");
        category.newProductCmptPropertyReference(tsuProperty);

        IProductCmptCategory tsuCategory = createDefaultCategoryForTableStructureUsages();
        ITableStructureUsage tsu1 = createTableStructureUsageProperty(productType, "tsu1");
        ITableStructureUsage tsu2 = createTableStructureUsageProperty(productType, "tsu2");
        ITableStructureUsage tsu3 = createTableStructureUsageProperty(productType, "tsu3");

        List<IProductCmptPropertyReference> references = tsuCategory.findProductCmptPropertyReferences(ipsProject);
        assertEquals(tsu1, references.get(0).findReferencedProductCmptProperty(ipsProject));
        assertEquals(tsu2, references.get(1).findReferencedProductCmptProperty(ipsProject));
        assertEquals(tsu3, references.get(2).findReferencedProductCmptProperty(ipsProject));
        assertEquals(3, references.size());
    }

    @Test
    public void shouldNotCreateNewTemporaryReferenceObjectForUnchangedTableStructureUsages() throws CoreException {
        IProductCmptCategory tsuCategory = createDefaultCategoryForTableStructureUsages();
        createTableStructureUsageProperty(productType, "tsu");

        List<IProductCmptPropertyReference> references1 = tsuCategory.findProductCmptPropertyReferences(ipsProject);
        List<IProductCmptPropertyReference> references2 = tsuCategory.findProductCmptPropertyReferences(ipsProject);
        assertEquals(references1, references2);
    }

    @Test
    public void shouldCreateTemporaryReferencesForNotAssignedPolicyCmptTypeAttributesIfIsCorrespondingDefaultCategory()
            throws CoreException {

        IPolicyCmptTypeAttribute attributeProperty = createPolicyCmptTypeAttributeProperty(productType,
                "attributeProperty");
        category.newProductCmptPropertyReference(attributeProperty);

        IProductCmptCategory policyAttributeCategory = createDefaultCategoryForPolicyCmptTypeAttributes();
        IPolicyCmptTypeAttribute attribute1 = createPolicyCmptTypeAttributeProperty(productType, "attribute1");
        IPolicyCmptTypeAttribute attribute2 = createPolicyCmptTypeAttributeProperty(productType, "attribute2");
        IPolicyCmptTypeAttribute attribute3 = createPolicyCmptTypeAttributeProperty(productType, "attribute3");

        List<IProductCmptPropertyReference> references = policyAttributeCategory
                .findProductCmptPropertyReferences(ipsProject);
        assertEquals(attribute1, references.get(0).findReferencedProductCmptProperty(ipsProject));
        assertEquals(attribute2, references.get(1).findReferencedProductCmptProperty(ipsProject));
        assertEquals(attribute3, references.get(2).findReferencedProductCmptProperty(ipsProject));
        assertEquals(3, references.size());
    }

    @Test
    public void shouldNotCreateTemporaryReferencesForNotAssignedPolicyCmptTypeAttributesThatAreNotProductRelevant()
            throws CoreException {

        IPolicyCmptTypeAttribute notAProductRelevantPolicyAttribute = createPolicyCmptTypeAttributeProperty(
                productType, "notAProductRelevantPolicyAttribute");
        notAProductRelevantPolicyAttribute.setProductRelevant(false);
        IProductCmptCategory policyAttributeCategory = createDefaultCategoryForPolicyCmptTypeAttributes();

        List<IProductCmptPropertyReference> references = policyAttributeCategory
                .findProductCmptPropertyReferences(ipsProject);
        assertTrue(references.isEmpty());
    }

    @Test
    public void shouldNotCreateNewTemporaryReferenceObjectForUnchangedPolicyCmptTypeAttributes() throws CoreException {
        IProductCmptCategory policyAttributeCategory = createDefaultCategoryForPolicyCmptTypeAttributes();
        createPolicyCmptTypeAttributeProperty(productType, "attribute");

        List<IProductCmptPropertyReference> references1 = policyAttributeCategory
                .findProductCmptPropertyReferences(ipsProject);
        List<IProductCmptPropertyReference> references2 = policyAttributeCategory
                .findProductCmptPropertyReferences(ipsProject);
        assertEquals(references1, references2);
    }

    @Test
    public void shouldCreateTemporaryReferencesForNotAssignedValidationRulesIfIsCorrespondingDefaultCategory()
            throws CoreException {

        IValidationRule ruleProperty = createValidationRuleProperty(productType, "ruleProperty");
        category.newProductCmptPropertyReference(ruleProperty);

        IProductCmptCategory ruleCategory = createDefaultCategoryForValidationRules();
        IValidationRule rule1 = createValidationRuleProperty(productType, "rule1");
        IValidationRule rule2 = createValidationRuleProperty(productType, "rule2");
        IValidationRule rule3 = createValidationRuleProperty(productType, "rule3");

        List<IProductCmptPropertyReference> references = ruleCategory.findProductCmptPropertyReferences(ipsProject);
        assertEquals(rule1, references.get(0).findReferencedProductCmptProperty(ipsProject));
        assertEquals(rule2, references.get(1).findReferencedProductCmptProperty(ipsProject));
        assertEquals(rule3, references.get(2).findReferencedProductCmptProperty(ipsProject));
        assertEquals(3, references.size());
    }

    @Test
    public void shouldNotCreateTemporaryReferencesForNotAssignedValidationRulesThatAreNotProductRelevant()
            throws CoreException {

        IValidationRule notAProductRelevantValidationRule = createValidationRuleProperty(productType,
                "notAProductRelevantValidationRule");
        notAProductRelevantValidationRule.setConfigurableByProductComponent(false);
        IProductCmptCategory ruleCategory = createDefaultCategoryForValidationRules();

        List<IProductCmptPropertyReference> references = ruleCategory.findProductCmptPropertyReferences(ipsProject);
        assertTrue(references.isEmpty());
    }

    @Test
    public void shouldNotCreateNewTemporaryReferenceObjectForUnchangedValidationRules() throws CoreException {
        IProductCmptCategory ruleCategory = createDefaultCategoryForValidationRules();
        createValidationRuleProperty(productType, "rule");

        List<IProductCmptPropertyReference> references1 = ruleCategory.findProductCmptPropertyReferences(ipsProject);
        List<IProductCmptPropertyReference> references2 = ruleCategory.findProductCmptPropertyReferences(ipsProject);
        assertEquals(references1, references2);
    }

    @Test
    public void shouldRetrieveProductCmptPropertyReferences() throws CoreException {
        IProductCmptTypeAttribute property2 = createProductCmptTypeAttributeProperty(productType, "property2");
        IProductCmptPropertyReference reference1 = category.newProductCmptPropertyReference(property);
        IProductCmptPropertyReference reference2 = category.newProductCmptPropertyReference(property2);

        List<IProductCmptPropertyReference> references = category.findProductCmptPropertyReferences(ipsProject);
        assertEquals(reference1, references.get(0));
        assertEquals(reference2, references.get(1));
        assertEquals(2, references.size());
    }

    @Test
    public void shouldRetrieveProductCmptPropertyReferencesIncludingTheSupertypeHierarchy() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptType superSuperProductType = createSuperProductType(superProductType, "SuperSuper");

        IProductCmptCategory superSuperCategory = superSuperProductType.newProductCmptCategory(CATEGORY_NAME);
        IProductCmptCategory superCategory = superProductType.newProductCmptCategory(CATEGORY_NAME);
        superSuperCategory.setInherited(true);
        superCategory.setInherited(true);
        category.setInherited(true);

        IProductCmptTypeAttribute superSuperProperty = superSuperProductType
                .newProductCmptTypeAttribute("superSuperProperty");
        IProductCmptTypeAttribute superProperty = superProductType.newProductCmptTypeAttribute("superProperty");

        IProductCmptPropertyReference superSuperReference = superSuperCategory
                .newProductCmptPropertyReference(superSuperProperty);
        IProductCmptPropertyReference superReference = superCategory.newProductCmptPropertyReference(superProperty);
        IProductCmptPropertyReference reference = category.newProductCmptPropertyReference(property);

        List<IProductCmptPropertyReference> allReferences = category.findAllProductCmptPropertyReferences(ipsProject);
        assertEquals(superSuperReference, allReferences.get(0));
        assertEquals(superReference, allReferences.get(1));
        assertEquals(reference, allReferences.get(2));
        assertEquals(3, allReferences.size());
    }

    @Test
    public void shouldNotRetrieveReferencedProductCmptPropertiesFromSupertypeHierarchyIfNotInherited()
            throws CoreException {

        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory superCategory = superProductType.newProductCmptCategory(CATEGORY_NAME);
        IProductCmptTypeAttribute superProperty = superProductType.newProductCmptTypeAttribute("superProperty");

        superCategory.newProductCmptPropertyReference(superProperty);
        IProductCmptPropertyReference reference = category.newProductCmptPropertyReference(property);

        category.setInherited(false);

        List<IProductCmptPropertyReference> allReferences = category.findAllProductCmptPropertyReferences(ipsProject);
        assertEquals(reference, allReferences.get(0));
        assertEquals(1, allReferences.size());
    }

    @Test
    public void shouldReturnWhetherAGivenPropertyIsReferencedByAndPersistedInThisCategory() {
        IProductCmptTypeAttribute attribute = createProductCmptTypeAttributeProperty(productType, "attributeProperty");
        category.setInherited(false);
        category.setDefaultForProductCmptTypeAttributes(true);

        assertFalse(category.isReferencedAndPersistedProductCmptProperty(attribute));
        category.newProductCmptPropertyReference(attribute);
        assertTrue(category.isReferencedAndPersistedProductCmptProperty(attribute));
    }

    @Test
    public void shouldCreateXmlElement() {
        ProductCmptCategory categoryImpl = (ProductCmptCategory)category;
        Document document = mock(Document.class);

        categoryImpl.createElement(document);

        verify(document).createElement(IProductCmptCategory.XML_TAG_NAME);
    }

    @Test
    public void shouldBePersistedToXml() throws ParserConfigurationException, CoreException {
        property.delete();

        category.setDefaultForFormulaSignatureDefinitions(true);
        category.setDefaultForPolicyCmptTypeAttributes(true);
        category.setDefaultForProductCmptTypeAttributes(true);
        category.setDefaultForTableStructureUsages(true);
        category.setDefaultForValidationRules(true);
        category.setPosition(Position.RIGHT);

        IProductCmptTypeAttribute productAttribute = createProductCmptTypeAttributeProperty(productType,
                "productAttribute");
        IPolicyCmptTypeAttribute policyAttribute = createPolicyCmptTypeAttributeProperty(productType, "policyAttribute");
        IProductCmptTypeMethod formula = createFormulaSignatureDefinitionProperty(productType, "formula");
        IValidationRule validationRule = createValidationRuleProperty(productType, "validationRule");
        ITableStructureUsage structureUsage = createTableStructureUsageProperty(productType, "tableStructureUsaage");
        category.newProductCmptPropertyReference(productAttribute);
        category.newProductCmptPropertyReference(policyAttribute);
        category.newProductCmptPropertyReference(formula);
        category.newProductCmptPropertyReference(validationRule);
        category.newProductCmptPropertyReference(structureUsage);

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

        List<IProductCmptPropertyReference> references = loadedCategory.findProductCmptPropertyReferences(ipsProject);
        assertEquals(productAttribute, references.get(0).findReferencedProductCmptProperty(ipsProject));
        assertEquals(policyAttribute, references.get(1).findReferencedProductCmptProperty(ipsProject));
        assertEquals(formula, references.get(2).findReferencedProductCmptProperty(ipsProject));
        assertEquals(validationRule, references.get(3).findReferencedProductCmptProperty(ipsProject));
        assertEquals(structureUsage, references.get(4).findReferencedProductCmptProperty(ipsProject));
        assertEquals(5, references.size());
    }

    @Test
    public void shouldNotPersistDirectPropertiesThatCannotBeFoundToXml() throws ParserConfigurationException {
        IProductCmptTypeAttribute productAttribute = createProductCmptTypeAttributeProperty(productType,
                "productAttribute");
        category.newProductCmptPropertyReference(productAttribute);

        productAttribute.delete();

        Element xmlElement = category.toXml(createXmlDocument(IProductCmptCategory.XML_TAG_NAME));
        IProductCmptCategory loadedCategory = productType.newProductCmptCategory();
        loadedCategory.initFromXml(xmlElement);

        assertEquals(0, loadedCategory.getNumberOfProductCmptPropertyReferences());
    }

    @Test
    public void shouldPersistExternalPropertiesThatCannotBeFoundToXml() throws ParserConfigurationException,
            CoreException {

        IPolicyCmptTypeAttribute policyAttribute = createPolicyCmptTypeAttributeProperty(productType, "policyAttribute");
        category.newProductCmptPropertyReference(policyAttribute);

        policyAttribute.delete();

        Element xmlElement = category.toXml(createXmlDocument(IProductCmptCategory.XML_TAG_NAME));
        IProductCmptCategory loadedCategory = productType.newProductCmptCategory();
        loadedCategory.initFromXml(xmlElement);

        assertEquals(1, loadedCategory.getNumberOfProductCmptPropertyReferences());
    }

    @Test
    public void shouldAllowToMoveReferences() throws CoreException {
        IProductCmptTypeAttribute property1 = createProductCmptTypeAttributeProperty(productType, "property1");
        IProductCmptTypeAttribute property2 = createProductCmptTypeAttributeProperty(productType, "property2");
        IProductCmptTypeAttribute property3 = createProductCmptTypeAttributeProperty(productType, "property3");

        IProductCmptPropertyReference reference1 = category.newProductCmptPropertyReference(property1);
        IProductCmptPropertyReference reference2 = category.newProductCmptPropertyReference(property2);
        IProductCmptPropertyReference reference3 = category.newProductCmptPropertyReference(property3);

        category.moveProductCmptPropertyReferences(new int[] { 1, 2 }, true);
        List<IProductCmptPropertyReference> references = category.findProductCmptPropertyReferences(ipsProject);
        assertEquals(reference2, references.get(0));
        assertEquals(reference3, references.get(1));
        assertEquals(reference1, references.get(2));

        assertTrue(lastEvent.isAffected(reference1));
        assertTrue(lastEvent.isAffected(reference2));
        assertTrue(lastEvent.isAffected(reference3));
    }

    @Test
    public void shouldReturnReferencesAsChildren() throws CoreException {
        IProductCmptPropertyReference reference = category.newProductCmptPropertyReference(property);

        IIpsElement[] children = category.getChildren();
        assertEquals(reference, children[0]);
        assertEquals(1, children.length);
    }

    @Test
    public void shouldGenerateValidationErrorIfNameIsEmpty() throws CoreException {
        category.setName("");

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList, IProductCmptCategory.MSGCODE_NAME_IS_EMPTY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void shouldGenerateValidationErrorIfNameIsUsedTwice() throws CoreException {
        productType.newProductCmptCategory(CATEGORY_NAME);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void shouldGenerateValidationErrorIfNameIsUsedTwiceInTypeHierarchy() throws CoreException {
        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        superProductType.newProductCmptCategory(CATEGORY_NAME);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, category,
                IProductCmptCategory.PROPERTY_NAME, Message.ERROR);
    }

    @Test
    public void shouldNotGenerateValidationErrorIfNameIsAlreadyUsedInSupertypeHierarchyButCategoryIsInherited()
            throws CoreException {

        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        superProductType.newProductCmptCategory(CATEGORY_NAME);
        category.setInherited(true);

        assertTrue(category.isValid(ipsProject));
    }

    @Test
    public void shouldGenerateValidationErrorIfInheritedButNotFoundInSupertypeHierarchy() throws CoreException {
        createSuperProductType(productType, "Super");
        category.setInherited(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_INHERITED_BUT_NOT_FOUND_IN_SUPERTYPE_HIERARCHY, category,
                IProductCmptCategory.PROPERTY_INHERITED, Message.ERROR);
    }

    @Test
    public void shouldGenerateValidationErrorIfInheritedButSupertypeNotFound() throws CoreException {
        category.setInherited(true);
        productType.setSupertype("foo");

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList,
                IProductCmptCategory.MSGCODE_INHERITED_BUT_NOT_FOUND_IN_SUPERTYPE_HIERARCHY, category,
                IProductCmptCategory.PROPERTY_INHERITED, Message.ERROR);
    }

    @Test
    public void shouldGenerateValidationErrorIfInheritedButNoSupertype() throws CoreException {
        category.setInherited(true);
        productType.setSupertype("");

        MessageList validationMessageList = category.validate(ipsProject);
        assertOneValidationMessage(validationMessageList, IProductCmptCategory.MSGCODE_INHERITED_BUT_NO_SUPERTYPE,
                category, IProductCmptCategory.PROPERTY_INHERITED, Message.ERROR);
    }

    @Test
    public void shouldGenerateValidationWarningIfDuplicateCategoriesAreMarkedAsDefaultForFormulaSignatureDefinitions()
            throws CoreException {

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
    public void shouldGenerateValidationWarningIfDuplicateCategoriesAreMarkedAsDefaultForValidationRules()
            throws CoreException {

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
    public void shouldGenerateValidationWarningIfDuplicateCategoriesAreMarkedAsDefaultForTableStructureUsages()
            throws CoreException {

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
    public void shouldGenerateValidationWarningIfDuplicateCategoriesAreMarkedAsDefaultForPolicyCmptTypeAttributes()
            throws CoreException {

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
    public void shouldGenerateValidationWarningIfDuplicateCategoriesAreMarkedAsDefaultForProductCmptTypeAttributes()
            throws CoreException {

        IProductCmptType superProductType = createSuperProductType(productType, "Super");
        IProductCmptCategory category2 = superProductType.newProductCmptCategory("bar");

        category.setDefaultForProductCmptTypeAttributes(true);
        category2.setDefaultForProductCmptTypeAttributes(true);

        MessageList validationMessageList = category.validate(ipsProject);
        assertEquals(IProductCmptCategory.MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                validationMessageList.getFirstMessage(Message.WARNING).getCode());
        assertEquals(1, validationMessageList.size());
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

    private IProductCmptTypeAttribute createProductCmptTypeAttributeProperty(IProductCmptType type, String name) {
        return type.newProductCmptTypeAttribute(name);
    }

    private IProductCmptTypeMethod createFormulaSignatureDefinitionProperty(IProductCmptType type, String name) {
        IProductCmptTypeMethod method = type.newProductCmptTypeMethod();
        method.setName(name);
        method.setFormulaName(name);
        method.setFormulaSignatureDefinition(true);
        return method;
    }

    private ITableStructureUsage createTableStructureUsageProperty(IProductCmptType type, String name) {
        ITableStructureUsage usage = type.newTableStructureUsage();
        usage.setRoleName(name);
        return usage;
    }

    private IValidationRule createValidationRuleProperty(IProductCmptType type, String name) throws CoreException {
        IPolicyCmptType policyCmptType = type.findPolicyCmptType(type.getIpsProject());
        IValidationRule rule = policyCmptType.newRule();
        rule.setConfigurableByProductComponent(true);
        rule.setName(name);
        return rule;
    }

    private IPolicyCmptTypeAttribute createPolicyCmptTypeAttributeProperty(IProductCmptType type, String name)
            throws CoreException {

        IPolicyCmptType policyCmptType = type.findPolicyCmptType(type.getIpsProject());
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName(name);
        attribute.setProductRelevant(true);
        return attribute;
    }

    private void resetContentChangedEvent() {
        lastEvent = null;
    }

    private void assertPropertyChangedEvent() {
        assertEquals(category, lastEvent.getPart());
        assertEquals(ContentChangeEvent.TYPE_PROPERTY_CHANGED, lastEvent.getEventType());
    }

    private IProductCmptType createSuperProductType(IProductCmptType productType, String prefix) throws CoreException {
        IPolicyCmptType superPolicyType = newPolicyAndProductCmptType(ipsProject, prefix + "PolicyType", prefix
                + "ProductType");
        IProductCmptType superProductType = superPolicyType.findProductCmptType(ipsProject);
        productType.setSupertype(superProductType.getQualifiedName());
        productType.findPolicyCmptType(ipsProject).setSupertype(superPolicyType.getQualifiedName());
        return superProductType;
    }

    private IProductCmptCategory createDefaultCategoryForFormulaSignatureDefinitions() {
        IProductCmptCategory formulaCategory = productType.newProductCmptCategory("formulas");
        formulaCategory.setDefaultForFormulaSignatureDefinitions(true);
        return formulaCategory;
    }

    private IProductCmptCategory createDefaultCategoryForValidationRules() {
        IProductCmptCategory rulesCategory = productType.newProductCmptCategory("rules");
        rulesCategory.setDefaultForValidationRules(true);
        return rulesCategory;
    }

    private IProductCmptCategory createDefaultCategoryForPolicyCmptTypeAttributes() {
        IProductCmptCategory policyAttributesCategory = productType.newProductCmptCategory("policyAttributes");
        policyAttributesCategory.setDefaultForPolicyCmptTypeAttributes(true);
        return policyAttributesCategory;
    }

    private IProductCmptCategory createDefaultCategoryForProductCmptTypeAttributes() {
        IProductCmptCategory productAttributesCategory = productType.newProductCmptCategory("productAttributes");
        productAttributesCategory.setDefaultForProductCmptTypeAttributes(true);
        return productAttributesCategory;
    }

    private IProductCmptCategory createDefaultCategoryForTableStructureUsages() {
        IProductCmptCategory tsusCategory = productType.newProductCmptCategory("tsus");
        tsusCategory.setDefaultForTableStructureUsages(true);
        return tsusCategory;
    }

}
